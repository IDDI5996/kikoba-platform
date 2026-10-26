package com.kikoba.kikoba_backend.governance.service;

import com.kikoba.kikoba_backend.governance.dto.AnnouncementResponse;
import com.kikoba.kikoba_backend.governance.dto.CreateAnnouncementRequest;

import java.util.List;
import java.util.UUID;

public interface AnnouncementService {

    AnnouncementResponse createAnnouncement(UUID userId, UUID groupId, CreateAnnouncementRequest request);
    AnnouncementResponse updateAnnouncement(UUID announcementId, UUID requesterId, CreateAnnouncementRequest request);
    AnnouncementResponse togglePin(UUID announcementId, UUID requesterId);
    void deleteAnnouncement(UUID announcementId, UUID requesterId);

    AnnouncementResponse getAnnouncementById(UUID announcementId, UUID requesterId);
    List<AnnouncementResponse> getGroupAnnouncements(UUID groupId, UUID requesterId);
}