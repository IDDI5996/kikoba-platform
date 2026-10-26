package com.kikoba.kikoba_backend.admin.repository;

import com.kikoba.kikoba_backend.admin.entity.AdminApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdminApprovalRepository extends JpaRepository<AdminApprovalRequest, UUID> {
    List<AdminApprovalRequest> findByStatus(AdminApprovalRequest.ApprovalStatus status);
}