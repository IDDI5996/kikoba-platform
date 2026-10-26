package com.kikoba.kikoba_backend.loan.service;

import com.kikoba.kikoba_backend.loan.dto.*;

import java.util.List;
import java.util.UUID;

public interface LoanService {

    // Eligibility
    LoanEligibilityResponse checkEligibility(UUID userId, UUID groupId);

    // Application lifecycle
    LoanResponse applyForLoan(UUID userId, UUID groupId, LoanApplicationRequest request);
    LoanResponse approveLoan(UUID loanId, UUID requesterId, LoanApprovalRequest request);
    LoanResponse rejectLoan(UUID loanId, UUID requesterId, LoanRejectionRequest request);
    LoanResponse disburseLoan(UUID loanId, UUID requesterId);
    LoanResponse cancelLoan(UUID loanId, UUID requesterId);

    // Query
    LoanResponse getLoanById(UUID loanId, UUID requesterId);
    List<LoanResponse> getMyLoans(UUID userId);
    List<LoanResponse> getMyLoansInGroup(UUID userId, UUID groupId);
    List<LoanResponse> getGroupLoans(UUID groupId, UUID requesterId);
    List<LoanResponse> getPendingGroupLoans(UUID groupId, UUID requesterId);
    List<LoanRepaymentResponse> getLoanSchedule(UUID loanId, UUID requesterId);

    // Repayment
    LoanRepaymentResponse makeRepayment(UUID loanId, UUID userId, MakeLoanRepaymentRequest request);
    LoanRepaymentResponse confirmRepayment(UUID repaymentId, UUID requesterId, String reference);
    LoanResponse repayLoanEarly(UUID loanId, UUID userId, MakeLoanRepaymentRequest request);

    // Reports
    LoanSummaryResponse getGroupSummary(UUID groupId, UUID requesterId);

    LoanCapacityResponse getGroupCapacity(UUID userId, UUID groupId);
}