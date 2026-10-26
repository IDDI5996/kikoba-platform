package com.kikoba.kikoba_backend.contribution.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class PaymentReferenceGenerator {

    private static final String PREFIX = "KIK";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom random = new SecureRandom();

    /**
     * Generates a unique reference like: KIK20260929123456AB3K
     */
    public String generate() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        StringBuilder suffix = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            suffix.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return PREFIX + timestamp + suffix;
    }
}