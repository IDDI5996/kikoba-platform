package com.kikoba.kikoba_backend.notification.service;

import com.kikoba.kikoba_backend.notification.dto.NotificationPreferenceRequest;
import com.kikoba.kikoba_backend.notification.dto.NotificationPreferenceResponse;

import java.util.List;
import java.util.UUID;

public interface NotificationPreferenceService {

    List<NotificationPreferenceResponse> getMyPreferences(UUID userId);
    NotificationPreferenceResponse updatePreference(UUID userId, NotificationPreferenceRequest request);
    List<NotificationPreferenceResponse> resetToDefaults(UUID userId);
}