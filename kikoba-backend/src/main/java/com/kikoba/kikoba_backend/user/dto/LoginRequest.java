package com.kikoba.kikoba_backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Phone number or email is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;
}