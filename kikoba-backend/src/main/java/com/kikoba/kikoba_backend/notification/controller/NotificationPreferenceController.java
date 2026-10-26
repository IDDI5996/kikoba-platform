package com.kikoba.kikoba_backend.notification.controller;

import com.kikoba.kikoba_backend.notification.dto.NotificationPreferenceRequest;
import com.kikoba.kikoba_backend.notification.dto.NotificationPreferenceResponse;
import com.kikoba.kikoba_backend.notification.service.NotificationPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notification-preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final NotificationPreferenceService preferenceService;

    /**
     * Get my preferences
     * GET /api/notification-preferences
     */
    @GetMapping
    public ResponseEntity<List<NotificationPreferenceResponse>> getMyPreferences(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(preferenceService.getMyPreferences(userId));
    }

    /**
     * Update a preference
     * PUT /api/notification-preferences
     */
    @PutMapping
    public ResponseEntity<NotificationPreferenceResponse> updatePreference(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody NotificationPreferenceRequest request) {
        return ResponseEntity.ok(preferenceService.updatePreference(userId, request));
    }

    /**
     * Reset all preferences to defaults (enabled)
     * POST /api/notification-preferences/reset
     */
    @PostMapping("/reset")
    public ResponseEntity<List<NotificationPreferenceResponse>> resetToDefaults(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(preferenceService.resetToDefaults(userId));
    }
}