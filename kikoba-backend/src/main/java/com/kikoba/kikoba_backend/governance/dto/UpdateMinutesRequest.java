package com.kikoba.kikoba_backend.governance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMinutesRequest {

    @NotBlank(message = "Minutes content is required")
    @Size(max = 20000, message = "Minutes cannot exceed 20,000 characters")
    private String minutes;
}