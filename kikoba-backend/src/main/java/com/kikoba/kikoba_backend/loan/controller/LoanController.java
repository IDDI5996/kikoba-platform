package com.kikoba.kikoba_backend.loan.controller;

import com.kikoba.kikoba_backend.loan.dto.*;
import com.kikoba.kikoba_backend.loan.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    /**
     * Check loan eligibility
     * GET /api/groups/{groupId}/loans/eligibility
     */
    @GetMapping("/groups/{groupId}/loans/eligibility")
    public ResponseEntity<LoanEligibilityResponse> checkEligibility(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(loanService.checkEligibility(userId, groupId));
    }

    /**
     * Apply for a loan
     * POST /api/groups/{groupId}/loans
     */
    @PostMapping("/groups/{groupId}/loans")
    public ResponseEntity<LoanResponse> applyForLoan(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId,
            @Valid @RequestBody LoanApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(loanService.applyForLoan(userId, groupId, request));
    }

    /**
     * Get my loans across all groups
     * GET /api/loans/my
     */
    @GetMapping("/loans/my")
    public ResponseEntity<List<LoanResponse>> getMyLoans(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(loanService.getMyLoans(userId));
    }

    /**
     * Get my loans in a specific group
     * GET /api/groups/{groupId}/loans/my
     */
    @GetMapping("/groups/{groupId}/loans/my")
    public ResponseEntity<List<LoanResponse>> getMyLoansInGroup(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(loanService.getMyLoansInGroup(userId, groupId));
    }

    /**
     * Get a single loan
     * GET /api/loans/{id}
     */
    @GetMapping("/loans/{id}")
    public ResponseEntity<LoanResponse> getLoanById(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(loanService.getLoanById(id, userId));
    }

    /**
     * Get all group loans (Manager only)
     * GET /api/groups/{groupId}/loans
     */
    @GetMapping("/groups/{groupId}/loans")
    public ResponseEntity<List<LoanResponse>> getGroupLoans(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(loanService.getGroupLoans(groupId, userId));
    }

    /**
     * Get pending loans (Manager only)
     * GET /api/groups/{groupId}/loans/pending
     */
    @GetMapping("/groups/{groupId}/loans/pending")
    public ResponseEntity<List<LoanResponse>> getPendingLoans(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(loanService.getPendingGroupLoans(groupId, userId));
    }

    /**
     * Approve a loan (Chairperson/Treasurer)
     * PUT /api/loans/{id}/approve
     */
    @PutMapping("/loans/{id}/approve")
    public ResponseEntity<LoanResponse> approveLoan(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @RequestBody(required = false) LoanApprovalRequest request) {
        return ResponseEntity.ok(loanService.approveLoan(id, userId, request));
    }

    /**
     * Reject a loan (Chairperson/Treasurer)
     * PUT /api/loans/{id}/reject
     */
    @PutMapping("/loans/{id}/reject")
    public ResponseEntity<LoanResponse> rejectLoan(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody LoanRejectionRequest request) {
        return ResponseEntity.ok(loanService.rejectLoan(id, userId, request));
    }

    /**
     * Disburse an approved loan (Chairperson/Treasurer)
     * PUT /api/loans/{id}/disburse
     */
    @PutMapping("/loans/{id}/disburse")
    public ResponseEntity<LoanResponse> disburseLoan(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(loanService.disburseLoan(id, userId));
    }

    /**
     * Cancel a loan (Applicant or Manager)
     * DELETE /api/loans/{id}
     */
    @DeleteMapping("/loans/{id}")
    public ResponseEntity<LoanResponse> cancelLoan(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(loanService.cancelLoan(id, userId));
    }

    /**
     * Get the repayment schedule for a loan
     * GET /api/loans/{id}/schedule
     */
    @GetMapping("/loans/{id}/schedule")
    public ResponseEntity<List<LoanRepaymentResponse>> getSchedule(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(loanService.getLoanSchedule(id, userId));
    }

    /**
     * Make a loan repayment
     * POST /api/loans/{id}/repay
     */
    @PostMapping("/loans/{id}/repay")
    public ResponseEntity<LoanRepaymentResponse> makeRepayment(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody MakeLoanRepaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(loanService.makeRepayment(id, userId, request));
    }

    /**
     * Confirm a repayment (Treasurer/Chairperson)
     * PUT /api/loan-repayments/{id}/confirm
     */
    @PutMapping("/loan-repayments/{id}/confirm")
    public ResponseEntity<LoanRepaymentResponse> confirmRepayment(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @RequestParam(required = false) String reference) {
        return ResponseEntity.ok(loanService.confirmRepayment(id, userId, reference));
    }

    /**
     * Get group loan summary (Manager only)
     * GET /api/groups/{groupId}/loans/summary
     */
    @GetMapping("/groups/{groupId}/loans/summary")
    public ResponseEntity<LoanSummaryResponse> getGroupSummary(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(loanService.getGroupSummary(groupId, userId));
    }

    /**
     * Get the group's loan capacity (available liquid funds)
     * GET /api/groups/{groupId}/loans/capacity
     */
    @GetMapping("/groups/{groupId}/loans/capacity")
    public ResponseEntity<LoanCapacityResponse> getGroupCapacity(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(loanService.getGroupCapacity(userId, groupId));
    }
}