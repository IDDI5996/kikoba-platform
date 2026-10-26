package com.kikoba.kikoba_backend.admin.dto;

import com.kikoba.kikoba_backend.admin.enums.AdminRole;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class AdminUserDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private Boolean isActive;
    private AdminRole adminRole;
    private LocalDateTime createdAt;
}