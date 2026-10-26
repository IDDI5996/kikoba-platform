package com.kikoba.kikoba_backend.notification.service.impl;

import com.kikoba.kikoba_backend.exception.ResourceNotFoundException;
import com.kikoba.kikoba_backend.notification.dto.NotificationPreferenceRequest;
import com.kikoba.kikoba_backend.notification.dto.NotificationPreferenceResponse;
import com.kikoba.kikoba_backend.notification.entity.NotificationPreference;
import com.kikoba.kikoba_backend.notification.mapper.NotificationMapper;
import com.kikoba.kikoba_backend.notification.repository.NotificationPreferenceRepository;
import com.kikoba.kikoba_backend.notification.service.NotificationPreferenceService;
import com.kikoba.kikoba_backend.user.entity.User;
import com.kikoba.kikoba_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public List<NotificationPreferenceResponse> getMyPreferences(UUID userId) {
        return preferenceRepository.findByUserId(userId).stream()
                .map(notificationMapper::toPreferenceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NotificationPreferenceResponse updatePreference(UUID userId, NotificationPreferenceRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        NotificationPreference pref = preferenceRepository
                .findByUserIdAndNotificationTypeAndChannel(
                        userId, request.getNotificationType().name(), request.getChannel().name())
                .orElseGet(() -> NotificationPreference.builder()
                        .user(user)
                        .notificationType(request.getNotificationType().name())
                        .channel(request.getChannel().name())
                        .enabled(true)
                        .build());

        pref.setEnabled(request.getEnabled());
        NotificationPreference saved = preferenceRepository.save(pref);

        log.info("User {} set preference {}/{} = {}",
                userId, request.getNotificationType(), request.getChannel(), request.getEnabled());

        return notificationMapper.toPreferenceResponse(saved);
    }

    @Override
    @Transactional
    public List<NotificationPreferenceResponse> resetToDefaults(UUID userId) {
        List<NotificationPreference> prefs = preferenceRepository.findByUserId(userId);
        for (NotificationPreference pref : prefs) {
            pref.setEnabled(true);
            preferenceRepository.save(pref);
        }

        log.info("Reset {} notification preferences for user {}", prefs.size(), userId);
        return getMyPreferences(userId);
    }
}