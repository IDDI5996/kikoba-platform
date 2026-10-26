package com.kikoba.kikoba_backend.governance.mapper;

import com.kikoba.kikoba_backend.governance.dto.AnnouncementResponse;
import com.kikoba.kikoba_backend.governance.entity.Announcement;
import org.springframework.stereotype.Component;

@Component
public class AnnouncementMapper {

    public AnnouncementResponse toResponse(Announcement announcement) {
        if (announcement == null) return null;

        String createdByName = null;
        if (announcement.getCreatedBy() != null) {
            createdByName = announcement.getCreatedBy().getFirstName() + " "
                    + announcement.getCreatedBy().getLastName();
        }

        return AnnouncementResponse.builder()
                .id(announcement.getId())
                .groupId(announcement.getGroup().getId())
                .groupName(announcement.getGroup().getName())
                .title(announcement.getTitle())
                .body(announcement.getBody())
                .category(announcement.getCategory())
                .pinned(announcement.getPinned())
                .createdBy(announcement.getCreatedBy() != null ? announcement.getCreatedBy().getId() : null)
                .createdByName(createdByName)
                .createdAt(announcement.getCreatedAt())
                .build();
    }
}