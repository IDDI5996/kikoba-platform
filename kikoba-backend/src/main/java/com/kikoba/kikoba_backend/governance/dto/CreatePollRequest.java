package com.kikoba.kikoba_backend.governance.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePollRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 255)
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotNull(message = "Options are required")
    @Size(min = 2, max = 10, message = "Poll must have 2-10 options")
    private List<@NotBlank @Size(max = 100) String> options;

    @Builder.Default
    private Boolean isAnonymous = false;

    @Builder.Default
    private Boolean allowMultipleChoices = false;

    private LocalDateTime closesAt;
}