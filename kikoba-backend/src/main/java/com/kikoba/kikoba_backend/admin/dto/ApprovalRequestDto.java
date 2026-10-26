package com.kikoba.kikoba_backend.admin.dto;

import lombok.Data;

@Data
public class ApprovalRequestDto {
    private String actionType;
    private String payload;
}