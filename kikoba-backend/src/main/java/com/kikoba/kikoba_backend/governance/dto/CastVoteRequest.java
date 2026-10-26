package com.kikoba.kikoba_backend.governance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CastVoteRequest {

    @NotBlank(message = "Choice is required")
    @Size(max = 255)
    private String choice;
}