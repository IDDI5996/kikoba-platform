package com.kikoba.kikoba_backend.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "system_settings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SystemSettings {
    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "maintenance_mode", nullable = false)
    private boolean maintenanceMode = false;

    @Column(name = "global_announcement", columnDefinition = "TEXT")
    private String globalAnnouncement;
}