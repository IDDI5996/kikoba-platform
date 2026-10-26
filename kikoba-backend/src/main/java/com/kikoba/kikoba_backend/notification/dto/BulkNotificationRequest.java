package com.kikoba.kikoba_backend.notification.dto;

import com.kikoba.kikoba_backend.notification.enums.NotificationChannel;
import com.kikoba.kikoba_backend.notification.enums.NotificationPriority;
import com.kikoba.kikoba_backend.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkNotificationRequest {

    @NotEmpty(message = "At least one recipient is required")
    private List<UUID> userIds;

    @NotNull(message = "Type is required")
    private NotificationType type;

    @NotNull(message = "Channel is required")
    private NotificationChannel channel;

    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;

    @NotBlank(message = "Title is required")
    @Size(max = 255)
    private String title;

    @NotBlank(message = "Body is required")
    @Size(max = 5000)
    private String body;

    @Size(max = 500)
    private String actionUrl;

    @Size(max = 1000)
    private String metadata;

    private String referenceType;

    private UUID referenceId;
}