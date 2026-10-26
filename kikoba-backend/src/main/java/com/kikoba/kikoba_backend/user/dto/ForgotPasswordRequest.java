package com.kikoba.kikoba_backend.user.dto;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String phoneNumber;
}