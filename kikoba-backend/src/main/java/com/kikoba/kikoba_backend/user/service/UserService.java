package com.kikoba.kikoba_backend.user.service;

import com.kikoba.kikoba_backend.user.dto.RegisterRequest;
import com.kikoba.kikoba_backend.user.dto.UpdateProfileRequest;
import com.kikoba.kikoba_backend.user.dto.UserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UserService {
    UserResponse registerUser(RegisterRequest request);
    UserResponse getUserById(UUID id);
    UserResponse getUserByPhone(String phoneNumber);
    UserResponse updateProfile(UUID userId, UpdateProfileRequest request);
    UserResponse enableMfa(UUID userId);
    UserResponse disableMfa(UUID userId);
    UserResponse updateAvatar(UUID userId, MultipartFile file);
    void verifyKyc(UUID userId, String documentUrl);
    void deactivateUser(UUID userId);
    void changePassword(UUID userId, String currentPassword, String newPassword);
}