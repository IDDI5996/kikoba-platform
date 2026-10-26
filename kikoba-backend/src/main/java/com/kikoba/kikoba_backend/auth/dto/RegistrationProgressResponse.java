package com.kikoba.kikoba_backend.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationProgressResponse {
    private String status;              // "PHONE_VERIFICATION_SENT" | "PHONE_VERIFIED" | "EMAIL_VERIFICATION_SENT" | "COMPLETE"
    private String nextStep;            // "VERIFY_PHONE" | "VERIFY_EMAIL" | "LOGIN"
    private String maskedPhone;
    private String maskedEmail;         // null if no email provided
    private boolean emailRequired;      // true if user entered an email
    private int codeExpiresInSeconds;
}