package com.kikoba.kikoba_backend.governance.dto;

import com.kikoba.kikoba_backend.governance.enums.AnnouncementCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAnnouncementRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 255)
    private String title;

    @NotBlank(message = "Body is required")
    @Size(min = 5, max = 10000)
    private String body;

    @Builder.Default
    private AnnouncementCategory category = AnnouncementCategory.GENERAL;

    @Builder.Default
    private Boolean pinned = false;
}