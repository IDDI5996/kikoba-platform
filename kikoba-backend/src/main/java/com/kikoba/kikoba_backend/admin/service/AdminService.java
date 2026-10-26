package com.kikoba.kikoba_backend.admin.service;

import com.kikoba.kikoba_backend.admin.dto.AdminSettingsDto;
import com.kikoba.kikoba_backend.admin.dto.AdminStatsDto;
import com.kikoba.kikoba_backend.admin.dto.AdminUserDto;
import com.kikoba.kikoba_backend.admin.entity.AdminApprovalRequest;
import com.kikoba.kikoba_backend.admin.entity.AdminAuditLog;
import com.kikoba.kikoba_backend.admin.entity.SystemSettings;
import com.kikoba.kikoba_backend.admin.enums.AdminRole;
import com.kikoba.kikoba_backend.admin.repository.AdminApprovalRepository;
import com.kikoba.kikoba_backend.admin.repository.AdminAuditLogRepository;
import com.kikoba.kikoba_backend.admin.repository.SystemSettingsRepository;
import com.kikoba.kikoba_backend.exception.BadRequestException;
import com.kikoba.kikoba_backend.exception.ResourceNotFoundException;
import com.kikoba.kikoba_backend.group.repository.GroupRepository;
import com.kikoba.kikoba_backend.user.entity.User;
import com.kikoba.kikoba_backend.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminAuditLogRepository auditLogRepo;
    private final AdminApprovalRepository approvalRepo;
    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private final SystemSettingsRepository settingsRepo;
    private final HttpServletRequest request;

    public void logAction(User admin, String action, String details) {
        AdminAuditLog log = AdminAuditLog.builder()
                .admin(admin)
                .action(action)
                .details(details)
                .ipAddress(request.getRemoteAddr())
                .build();
        auditLogRepo.save(log);
    }

    // ==========================================
    // MAKER-CHECKER
    // ==========================================
    @Transactional
    public AdminApprovalRequest createApprovalRequest(User requester, String actionType, String payload) {
        AdminApprovalRequest approval = AdminApprovalRequest.builder()
                .requester(requester)
                .actionType(actionType)
                .payload(payload)
                .status(AdminApprovalRequest.ApprovalStatus.PENDING)
                .build();

        logAction(requester, "CREATE_APPROVAL_REQUEST", "Action: " + actionType);
        return approvalRepo.save(approval);
    }

    @Transactional
    public void approveRequest(User checker, UUID requestId) {
        AdminApprovalRequest approval = approvalRepo.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (approval.getStatus() != AdminApprovalRequest.ApprovalStatus.PENDING) {
            throw new BadRequestException("Request is already processed");
        }

        if (approval.getRequester().getId().equals(checker.getId())) {
            throw new BadRequestException("You cannot approve your own request. A different admin must check it.");
        }

        approval.setChecker(checker);
        approval.setStatus(AdminApprovalRequest.ApprovalStatus.APPROVED);
        approvalRepo.save(approval);

        logAction(checker, "APPROVE_REQUEST", "Approved request ID: " + requestId);
    }

    public List<AdminApprovalRequest> getPendingApprovals() {
        return approvalRepo.findByStatus(AdminApprovalRequest.ApprovalStatus.PENDING);
    }

    // ==========================================
    // STATS
    // ==========================================
    public AdminStatsDto getStats() {
        return AdminStatsDto.builder()
                .totalUsers(userRepo.count())
                .totalGroups(groupRepo.count())
                .totalTransactions(0)
                .build();
    }

    // ==========================================
    // USER MANAGEMENT
    // ==========================================
    public Page<AdminUserDto> getAllUsers(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<User> users;
        if (search != null && !search.isBlank()) {
            users = userRepo.findByPhoneNumberContainingOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                    search, search, search, pageable);
        } else {
            users = userRepo.findAll(pageable);
        }

        return users.map(u -> AdminUserDto.builder()
                .id(u.getId())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .phoneNumber(u.getPhoneNumber())
                .email(u.getEmail())
                .isActive(u.getIsActive())
                .adminRole(u.getAdminRole())
                .createdAt(u.getCreatedAt())
                .build());
    }

    @Transactional
    public void updateUserRole(User admin, UUID targetUserId, AdminRole newRole) {
        User targetUser = userRepo.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        targetUser.setAdminRole(newRole);
        userRepo.save(targetUser);

        logAction(admin, "UPDATE_USER_ROLE", "Changed role of " + targetUser.getPhoneNumber() + " to " + newRole);
    }

    @Transactional
    public void toggleUserStatus(User admin, UUID targetUserId) {
        User targetUser = userRepo.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        targetUser.setIsActive(!targetUser.getIsActive());
        userRepo.save(targetUser);

        String action = targetUser.getIsActive() ? "ACTIVATE_USER" : "SUSPEND_USER";
        logAction(admin, action, "Target: " + targetUser.getPhoneNumber());
    }

    // ==========================================
    // SYSTEM SETTINGS
    // ==========================================
    public AdminSettingsDto getSystemSettings() {
        SystemSettings settings = settingsRepo.findAll().stream().findFirst()
                .orElseGet(() -> settingsRepo.save(new SystemSettings()));

        AdminSettingsDto dto = new AdminSettingsDto();
        dto.setMaintenanceMode(settings.isMaintenanceMode());
        dto.setGlobalAnnouncement(settings.getGlobalAnnouncement());
        return dto;
    }

    @Transactional
    public void updateSystemSettings(User admin, AdminSettingsDto dto) {
        SystemSettings settings = settingsRepo.findAll().stream().findFirst()
                .orElseGet(() -> settingsRepo.save(new SystemSettings()));

        settings.setMaintenanceMode(dto.isMaintenanceMode());
        settings.setGlobalAnnouncement(dto.getGlobalAnnouncement());
        settingsRepo.save(settings);

        logAction(admin, "UPDATE_SETTINGS", "Maintenance Mode: " + dto.isMaintenanceMode());
    }

    // ==========================================
    // AUDIT LOGS
    // ==========================================
    public Page<AdminAuditLog> getAuditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return auditLogRepo.findAll(pageable);
    }
}