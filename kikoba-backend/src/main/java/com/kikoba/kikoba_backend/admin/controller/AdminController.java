package com.kikoba.kikoba_backend.admin.controller;

import com.kikoba.kikoba_backend.admin.dto.AdminSettingsDto;
import com.kikoba.kikoba_backend.admin.dto.AdminStatsDto;
import com.kikoba.kikoba_backend.admin.dto.AdminUserDto;
import com.kikoba.kikoba_backend.admin.dto.ApprovalRequestDto;
import com.kikoba.kikoba_backend.admin.entity.AdminApprovalRequest;
import com.kikoba.kikoba_backend.admin.entity.AdminAuditLog;
import com.kikoba.kikoba_backend.admin.enums.AdminRole;
import com.kikoba.kikoba_backend.admin.service.AdminService;
import com.kikoba.kikoba_backend.user.entity.User;
import com.kikoba.kikoba_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserRepository userRepo;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_AUDITOR')")
    public ResponseEntity<AdminStatsDto> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    // --- APPROVALS ---
    @PostMapping("/approvals/request")
    @PreAuthorize("hasAuthority('ROLE_FINANCE_ADMIN')")
    public ResponseEntity<AdminApprovalRequest> createRequest(
            @AuthenticationPrincipal UUID adminId,
            @RequestBody ApprovalRequestDto dto) {
        User admin = userRepo.findById(adminId).orElseThrow();
        return ResponseEntity.ok(adminService.createApprovalRequest(admin, dto.getActionType(), dto.getPayload()));
    }

    @PostMapping("/approvals/{id}/approve")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> approveRequest(
            @AuthenticationPrincipal UUID adminId,
            @PathVariable UUID id) {
        User checker = userRepo.findById(adminId).orElseThrow();
        adminService.approveRequest(checker, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/approvals/pending")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_AUDITOR')")
    public ResponseEntity<List<AdminApprovalRequest>> getPendingApprovals() {
        return ResponseEntity.ok(adminService.getPendingApprovals());
    }

    // --- USER MANAGEMENT ---
    @GetMapping("/users")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_SUPPORT_ADMIN')")
    public ResponseEntity<Page<AdminUserDto>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getAllUsers(search, page, size));
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> updateUserRole(
            @AuthenticationPrincipal UUID adminId,
            @PathVariable UUID id,
            @RequestParam AdminRole role) {
        User admin = userRepo.findById(adminId).orElseThrow();
        adminService.updateUserRole(admin, id, role);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_SUPPORT_ADMIN')")
    public ResponseEntity<?> toggleUserStatus(
            @AuthenticationPrincipal UUID adminId,
            @PathVariable UUID id) {
        User admin = userRepo.findById(adminId).orElseThrow();
        adminService.toggleUserStatus(admin, id);
        return ResponseEntity.ok().build();
    }

    // --- SYSTEM SETTINGS ---
    @GetMapping("/settings")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<AdminSettingsDto> getSettings() {
        return ResponseEntity.ok(adminService.getSystemSettings());
    }

    @PutMapping("/settings")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> updateSettings(
            @AuthenticationPrincipal UUID adminId,
            @RequestBody AdminSettingsDto dto) {
        User admin = userRepo.findById(adminId).orElseThrow();
        adminService.updateSystemSettings(admin, dto);
        return ResponseEntity.ok().build();
    }

    // --- AUDIT LOGS ---
    @GetMapping("/audit-logs")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_AUDITOR')")
    public ResponseEntity<Page<AdminAuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAuditLogs(page, size));
    }
}