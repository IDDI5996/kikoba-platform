package com.kikoba.kikoba_backend.governance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMeetingRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 255)
    private String title;

    @Size(max = 5000, message = "Agenda cannot exceed 5000 characters")
    private String agenda;

    @NotNull(message = "Scheduled date/time is required")
    @Future(message = "Meeting must be scheduled in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime scheduledAt;

    @Size(max = 255)
    private String location;
}