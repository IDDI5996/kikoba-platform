package com.kikoba.kikoba_backend.group.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinGroupRequest {

    @NotBlank(message = "Invitation code is required")
    private String invitationCode;
}