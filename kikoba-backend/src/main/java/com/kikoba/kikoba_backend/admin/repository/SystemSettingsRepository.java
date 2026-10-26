package com.kikoba.kikoba_backend.admin.repository;

import com.kikoba.kikoba_backend.admin.entity.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SystemSettingsRepository extends JpaRepository<SystemSettings, UUID> {}