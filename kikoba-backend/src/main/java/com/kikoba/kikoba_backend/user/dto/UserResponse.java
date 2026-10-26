package com.kikoba.kikoba_backend.user.dto;

import com.kikoba.kikoba_backend.admin.enums.AdminRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private UUID id;
    private String phoneNumber;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean kycVerified;
    private Boolean mfaEnabled;
    private String preferredLanguage;
    private Boolean isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private String profilePictureUrl;
    private AdminRole adminRole;
}