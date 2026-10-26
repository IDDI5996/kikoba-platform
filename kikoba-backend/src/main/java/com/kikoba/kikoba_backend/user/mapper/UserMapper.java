package com.kikoba.kikoba_backend.user.mapper;

import com.kikoba.kikoba_backend.user.dto.UserResponse;
import com.kikoba.kikoba_backend.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;

        return UserResponse.builder()
                .id(user.getId())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .kycVerified(user.getKycVerified())
                .mfaEnabled(user.getMfaEnabled())
                .preferredLanguage(user.getPreferredLanguage())
                .isActive(user.getIsActive())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .profilePictureUrl(user.getProfilePictureUrl())
                .adminRole(user.getAdminRole())
                .build();
    }
}