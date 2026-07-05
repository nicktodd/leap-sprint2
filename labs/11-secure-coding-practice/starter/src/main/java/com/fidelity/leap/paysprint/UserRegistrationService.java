package com.fidelity.leap.paysprint;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class UserRegistrationService {

    private UserRepository userRepository;

    public UserRegistrationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(String email, String rawPassword) {
        // VULNERABILITY: MD5 is a fast, unsalted, general-purpose hash,
        // not a password hash. Trivially brute-forced if the database leaks.
        String hashed = md5(rawPassword);
        return userRepository.save(new User(null, email, hashed));
    }

    private String md5(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
