package com.kikoba.kikoba_backend.admin.dto;

import lombok.Data;

@Data
public class AdminSettingsDto {
    private boolean maintenanceMode;
    private String globalAnnouncement;
}