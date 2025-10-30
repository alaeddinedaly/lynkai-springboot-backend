package com.lynkai.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class HashEncoder {

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    // Encode a raw password
    public String encode(String raw) {
        return bcrypt.encode(raw);
    }

    // Verify raw password against hashed password
    public boolean matches(String raw, String hashed) {
        return bcrypt.matches(raw, hashed);
    }
}
