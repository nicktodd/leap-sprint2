package com.neueda.leap.paysprint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    // FIX (A07 — Authentication Failures): simple in-process rate limiter.
    // Tracks failed attempt count per email address. After MAX_ATTEMPTS failures
    // the account is locked until an admin resets it (or a timeout, in a
    // production implementation backed by Redis/DB rather than in-process state).
    // Production systems should use a library (Bucket4j, Resilience4j) or an
    // API gateway for this, but the pattern is correct.
    private static final int MAX_ATTEMPTS = 5;
    private final ConcurrentMap<String, AtomicInteger> failedAttempts = new ConcurrentHashMap<>();

    // Uses BCrypt matching to align with the fixed UserRegistrationService.
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @Autowired
    private UserRepository userRepository;

    private ConcurrentMap<String, Long> sessionStore = new ConcurrentHashMap<>();

    @PostMapping("/api/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        String email = request.getEmail();

        // Check lockout before hitting the database.
        AtomicInteger attempts = failedAttempts.getOrDefault(email, new AtomicInteger(0));
        if (attempts.get() >= MAX_ATTEMPTS) {
            // FIX (A09 — Security Logging): log lockout events so the security
            // team can detect and investigate brute-force attacks.
            log.warn("Login blocked for {} — account locked after {} failed attempts", email, MAX_ATTEMPTS);
            return new LoginResponse(null, "Account locked — contact support");
        }

        User user = userRepository.findByEmail(email);

        if (user != null && passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // Successful login: clear the failure counter.
            failedAttempts.remove(email);
            String token = UUID.randomUUID().toString();
            sessionStore.put(token, user.getId());
            return new LoginResponse(token);
        }

        // FIX (A07): increment and persist the failure counter.
        failedAttempts.computeIfAbsent(email, k -> new AtomicInteger(0)).incrementAndGet();

        // FIX (A09 — Security Logging & Alerting Failures):
        // Log every failed login attempt with enough context to detect a
        // brute-force attack. Do NOT log the password. The original code
        // caught AuthenticationException and swallowed it with no log at all.
        log.warn("Failed login attempt for email={} attempts={}", email,
                failedAttempts.get(email).get());

        return new LoginResponse(null, "Invalid email or password");
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public LoginResponse handleAuthFailure(AuthenticationFailedException ex) {
        // FIX (A09): log the exception rather than silently discarding it.
        log.warn("AuthenticationFailedException: {}", ex.getMessage());
        return new LoginResponse(null, "Invalid email or password");
    }
}
