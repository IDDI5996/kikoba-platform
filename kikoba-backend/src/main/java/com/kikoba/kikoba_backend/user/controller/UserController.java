package com.kikoba.kikoba_backend.user.controller;

import com.kikoba.kikoba_backend.user.dto.UpdateProfileRequest;
import com.kikoba.kikoba_backend.user.dto.UserResponse;
import com.kikoba.kikoba_backend.user.service.UserService;
import com.kikoba.kikoba_backend.user.dto.ChangePasswordRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map; // <-- ADDED MISSING IMPORT
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @PostMapping("/me/kyc")
    public ResponseEntity<Void> submitKyc(
            @AuthenticationPrincipal UUID userId,
            @RequestParam String documentUrl) {
        userService.verifyKyc(userId, documentUrl);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/me/avatar", consumes = "multipart/form-data")
    public ResponseEntity<UserResponse> uploadAvatar(
            @AuthenticationPrincipal UUID userId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userService.updateAvatar(userId, file));
    }

    @PostMapping("/me/mfa/enable")
    public ResponseEntity<UserResponse> enableMfa(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(userService.enableMfa(userId));
    }

    @PostMapping("/me/mfa/disable")
    public ResponseEntity<UserResponse> disableMfa(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(userService.disableMfa(userId));
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }
}