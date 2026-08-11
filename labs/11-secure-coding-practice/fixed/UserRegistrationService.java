package com.neueda.leap.paysprint;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UserRegistrationService {

    // FIX (A04 — Cryptographic Failures):
    // BCryptPasswordEncoder with strength 12 replaces the MD5 hash.
    // BCrypt is purpose-built for passwords: it is deliberately slow (cost factor),
    // includes a per-password random salt automatically, and produces a
    // self-contained hash string that encodes the salt and cost factor.
    // MD5 is a fast, unsalted general-purpose hash — trivially reversed with
    // rainbow tables or GPU brute-force (billions of hashes per second).
    //
    // Note on Copilot's first suggestion: Copilot initially suggested SHA-256,
    // which is also a fast, general-purpose hash. Faster is worse for passwords.
    // BCrypt, SCrypt, or Argon2 are the correct choices; SHA-* is not.
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    private UserRepository userRepository;

    public UserRegistrationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(String email, String rawPassword) {
        String hashed = passwordEncoder.encode(rawPassword);
        return userRepository.save(new User(null, email, hashed));
    }
}
