package com.kikoba.kikoba_backend.governance.dto;

import com.kikoba.kikoba_backend.governance.enums.AnnouncementCategory;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnouncementResponse {
    private UUID id;
    private UUID groupId;
    private String groupName;
    private String title;
    private String body;
    private AnnouncementCategory category;
    private Boolean pinned;
    private UUID createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
}