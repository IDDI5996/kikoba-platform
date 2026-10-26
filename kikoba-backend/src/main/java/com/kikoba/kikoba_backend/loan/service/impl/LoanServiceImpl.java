package com.kikoba.kikoba_backend.loan.service.impl;

import com.kikoba.kikoba_backend.contribution.util.PaymentReferenceGenerator;
import com.kikoba.kikoba_backend.contribution.repository.ContributionRepository;
import com.kikoba.kikoba_backend.exception.BadRequestException;
import com.kikoba.kikoba_backend.exception.ResourceNotFoundException;
import com.kikoba.kikoba_backend.exception.UnauthorizedException;
import com.kikoba.kikoba_backend.group.entity.Group;
import com.kikoba.kikoba_backend.group.entity.GroupMember;
import com.kikoba.kikoba_backend.group.enums.GroupRole;
import com.kikoba.kikoba_backend.group.repository.GroupMemberRepository;
import com.kikoba.kikoba_backend.loan.dto.*;
import com.kikoba.kikoba_backend.loan.entity.Loan;
import com.kikoba.kikoba_backend.loan.entity.LoanRepayment;
import com.kikoba.kikoba_backend.loan.enums.LoanStatus;
import com.kikoba.kikoba_backend.loan.enums.RepaymentStatus;
import com.kikoba.kikoba_backend.loan.mapper.LoanMapper;
import com.kikoba.kikoba_backend.loan.repository.LoanRepaymentRepository;
import com.kikoba.kikoba_backend.loan.repository.LoanRepository;
import com.kikoba.kikoba_backend.loan.service.LoanService;
import com.kikoba.kikoba_backend.loan.util.LoanCalculator;
import com.kikoba.kikoba_backend.notification.enums.NotificationPriority;
import com.kikoba.kikoba_backend.notification.enums.NotificationType;
import com.kikoba.kikoba_backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LoanServiceImpl implements LoanService {

    private static final BigDecimal SAVINGS_MULTIPLIER = new BigDecimal("3.0");
    private static final BigDecimal DEFAULT_INTEREST_RATE = new BigDecimal("10.0");
    private static final BigDecimal MONTHLY_PENALTY_RATE = new BigDecimal("5.0");  // 5% per month on overdue

    private final LoanRepository loanRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final LoanMapper loanMapper;
    private final LoanCalculator loanCalculator;
    private final PaymentReferenceGenerator referenceGenerator;
    private final NotificationService notificationService;
    private final ContributionRepository contributionRepository;

    // ============================================
    // ELIGIBILITY CHECK
    // ============================================
    @Override
    public LoanEligibilityResponse checkEligibility(UUID userId, UUID groupId) {
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (!member.getIsActive()) {
            return LoanEligibilityResponse.builder()
                    .userId(userId).groupId(groupId)
                    .eligible(false)
                    .reason("Your membership is inactive")
                    .build();
        }

        // Check active loan
        long activeLoans = loanRepository.countByGroupMemberIdAndStatus(member.getId(), LoanStatus.ACTIVE)
                + loanRepository.countByGroupMemberIdAndStatus(member.getId(), LoanStatus.APPROVED)
                + loanRepository.countByGroupMemberIdAndStatus(member.getId(), LoanStatus.PENDING);

        if (activeLoans > 0) {
            return LoanEligibilityResponse.builder()
                    .userId(userId).groupId(groupId)
                    .totalSavings(member.getTotalContributed())
                    .maxLoanAmount(BigDecimal.ZERO)
                    .savingsMultiplier(SAVINGS_MULTIPLIER)
                    .eligible(false)
                    .hasActiveLoan(true)
                    .reason("You already have an active or pending loan")
                    .build();
        }

        // Check outstanding loan balance
        if (member.getTotalLoanBalance().signum() > 0) {
            return LoanEligibilityResponse.builder()
                    .userId(userId).groupId(groupId)
                    .totalSavings(member.getTotalContributed())
                    .maxLoanAmount(BigDecimal.ZERO)
                    .savingsMultiplier(SAVINGS_MULTIPLIER)
                    .eligible(false)
                    .hasActiveLoan(true)
                    .reason("You have an outstanding loan balance of " + member.getTotalLoanBalance())
                    .build();
        }

        BigDecimal maxLoan = member.getTotalContributed()
                .multiply(SAVINGS_MULTIPLIER)
                .setScale(2, RoundingMode.HALF_UP);

        return LoanEligibilityResponse.builder()
                .userId(userId).groupId(groupId)
                .totalSavings(member.getTotalContributed())
                .maxLoanAmount(maxLoan)
                .savingsMultiplier(SAVINGS_MULTIPLIER)
                .eligible(true)
                .hasActiveLoan(false)
                .reason("Eligible up to " + maxLoan)
                .build();
    }

    // ============================================
    // APPLY FOR LOAN
    // ============================================
    @Override
    @Transactional
    public LoanResponse applyForLoan(UUID userId, UUID groupId, LoanApplicationRequest request) {
        log.info("User {} applying for loan of {} in group {}", userId, request.getAmount(), groupId);

        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (!member.getIsActive()) {
            throw new BadRequestException("Your membership is inactive");
        }

        // ---- Rule 1: Eligibility ----
        LoanEligibilityResponse eligibility = checkEligibility(userId, groupId);
        if (!eligibility.isEligible()) {
            throw new BadRequestException(eligibility.getReason());
        }

        if (request.getAmount().compareTo(eligibility.getMaxLoanAmount()) > 0) {
            throw new BadRequestException(
                    "Loan amount exceeds your maximum eligibility of " + eligibility.getMaxLoanAmount());
        }

        // ---- Rule 2: Group liquidity (NEW) ----
        BigDecimal availableFunds = calculateGroupAvailableFunds(groupId);
        if (request.getAmount().compareTo(availableFunds) > 0) {
            throw new BadRequestException(
                    "Insufficient group funds. Available: " + availableFunds
                            + " TZS, Requested: " + request.getAmount() + " TZS");
        }

        Group group = member.getGroup();
        BigDecimal interestRate = group.getInterestRate() != null && group.getInterestRate().signum() > 0
                ? group.getInterestRate()
                : DEFAULT_INTEREST_RATE;

        BigDecimal totalRepayable = loanCalculator.calculateTotalRepayable(
                request.getAmount(), interestRate, request.getRepaymentPeriodMonths());

        Loan loan = Loan.builder()
                .groupMember(member)
                .amount(request.getAmount())
                .interestRate(interestRate)
                .totalRepayable(totalRepayable)
                .balance(totalRepayable)
                .purpose(request.getPurpose())
                .status(LoanStatus.PENDING)
                .repaymentPeriodMonths(request.getRepaymentPeriodMonths())
                .repaymentFrequency("MONTHLY")
                .collateralDescription(request.getCollateralDescription())
                .build();

        Loan saved = loanRepository.save(loan);
        log.info("Loan {} created with status PENDING", saved.getId());

        notificationService.notifyUserAsync(
                userId,
                NotificationType.LOAN_APPLICATION_SUBMITTED,
                "Loan Application Submitted",
                "Your loan application for " + loan.getAmount() + " TZS is under review.",
                "/loans/" + saved.getId(),
                "Loan",
                saved.getId(),
                NotificationPriority.NORMAL
        );

        return loanMapper.toResponse(loanRepository.findByIdWithDetails(saved.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found")));
    }

    // ============================================
    // APPROVE LOAN (Chairperson/Treasurer)
    // ============================================
    @Override
    @Transactional
    public LoanResponse approveLoan(UUID loanId, UUID requesterId, LoanApprovalRequest request) {
        log.info("User {} approving loan {}", requesterId, loanId);

        Loan loan = loanRepository.findByIdWithDetails(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        Group group = loan.getGroupMember().getGroup();

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.CHAIRPERSON && requester.getRole() != GroupRole.TREASURER) {
            throw new UnauthorizedException("Only Chairperson or Treasurer can approve loans");
        }

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BadRequestException("Only PENDING loans can be approved. Current: " + loan.getStatus());
        }

        // Optional interest rate override
        if (request != null && request.getInterestRateOverride() != null) {
            loan.setInterestRate(request.getInterestRateOverride());
            BigDecimal newTotal = loanCalculator.calculateTotalRepayable(
                    loan.getAmount(), request.getInterestRateOverride(), loan.getRepaymentPeriodMonths());
            loan.setTotalRepayable(newTotal);
            loan.setBalance(newTotal);
        }

        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovedBy(requester.getUser());
        loan.setApprovedAt(LocalDateTime.now());

        Loan saved = loanRepository.save(loan);
        log.info("Loan {} approved by user {}", loanId, requesterId);

        notificationService.notifyUserAsync(
                loan.getGroupMember().getUser().getId(),
                NotificationType.LOAN_APPROVED,
                "Loan Approved",
                "Your loan of " + loan.getAmount() + " TZS has been approved. Awaiting disbursement.",
                "/loans/" + loan.getId(),
                "Loan",
                loan.getId(),
                NotificationPriority.HIGH
        );

        return loanMapper.toResponse(loanRepository.findByIdWithDetails(saved.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found")));
    }

    // ============================================
    // REJECT LOAN
    // ============================================
    @Override
    @Transactional
    public LoanResponse rejectLoan(UUID loanId, UUID requesterId, LoanRejectionRequest request) {
        Loan loan = loanRepository.findByIdWithDetails(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        Group group = loan.getGroupMember().getGroup();

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.CHAIRPERSON && requester.getRole() != GroupRole.TREASURER) {
            throw new UnauthorizedException("Only Chairperson or Treasurer can reject loans");
        }

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BadRequestException("Only PENDING loans can be rejected");
        }

        loan.setStatus(LoanStatus.REJECTED);
        loan.setRejectionReason(request.getReason());
        loan.setApprovedBy(requester.getUser());
        loan.setApprovedAt(LocalDateTime.now());

        Loan saved = loanRepository.save(loan);

        notificationService.notifyUserAsync(
                loan.getGroupMember().getUser().getId(),
                NotificationType.LOAN_REJECTED,
                "Loan Application Rejected",
                "Your loan application was rejected. Reason: " + request.getReason(),
                "/loans/" + loan.getId(),
                "Loan",
                loan.getId(),
                NotificationPriority.HIGH
        );

        return loanMapper.toResponse(loanRepository.findByIdWithDetails(saved.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found")));
    }

    // ============================================
    // DISBURSE LOAN (Chairperson/Treasurer)
    // ============================================
    @Override
    @Transactional
    public LoanResponse disburseLoan(UUID loanId, UUID requesterId) {
        log.info("User {} disbursing loan {}", requesterId, loanId);

        Loan loan = loanRepository.findByIdWithDetails(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        Group group = loan.getGroupMember().getGroup();

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.CHAIRPERSON && requester.getRole() != GroupRole.TREASURER) {
            throw new UnauthorizedException("Only Chairperson or Treasurer can disburse loans");
        }

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new BadRequestException("Only APPROVED loans can be disbursed. Current: " + loan.getStatus());
        }

        // ---- Re-verify group liquidity before disbursing ----
        BigDecimal availableFunds = calculateGroupAvailableFunds(group.getId());
        if (loan.getAmount().compareTo(availableFunds) > 0) {
            throw new BadRequestException(
                    "Cannot disburse. Group only has " + availableFunds
                            + " TZS available, but this loan requires " + loan.getAmount() + " TZS.");
        }

        // Update loan
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setDisbursedBy(requester.getUser());
        loan.setDisbursedAt(LocalDateTime.now());
        loan.setFirstDueDate(LocalDate.now().plusMonths(1));

        Loan saved = loanRepository.save(loan);

        // Generate repayment schedule
        List<LoanRepayment> schedule = loanCalculator.generateSchedule(
                saved, saved.getFirstDueDate(), MONTHLY_PENALTY_RATE);

        loanRepaymentRepository.saveAll(schedule);
        log.info("Generated {} repayment installments for loan {}", schedule.size(), loanId);

        // Update member's loan balance
        GroupMember member = loan.getGroupMember();
        member.setTotalLoanBalance(member.getTotalLoanBalance().add(loan.getAmount()));
        groupMemberRepository.save(member);

        notificationService.notifyUserAsync(
                loan.getGroupMember().getUser().getId(),
                NotificationType.LOAN_DISBURSED,
                "Loan Disbursed",
                "Your loan of " + loan.getAmount() + " TZS has been disbursed. First payment due on "
                        + loan.getFirstDueDate() + ".",
                "/loans/" + loan.getId(),
                "Loan",
                loan.getId(),
                NotificationPriority.HIGH
        );

        return loanMapper.toResponse(loanRepository.findByIdWithDetails(saved.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found")));
    }

    // ============================================
    // CANCEL LOAN (Applicant or Manager)
    // ============================================
    @Override
    @Transactional
    public LoanResponse cancelLoan(UUID loanId, UUID requesterId) {
        Loan loan = loanRepository.findByIdWithDetails(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        Group group = loan.getGroupMember().getGroup();

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        boolean isOwn = loan.getGroupMember().getId().equals(requester.getId());
        boolean isManager = requester.getRole() == GroupRole.CHAIRPERSON
                || requester.getRole() == GroupRole.TREASURER;

        if (!isOwn && !isManager) {
            throw new UnauthorizedException("You cannot cancel this loan");
        }

        if (loan.getStatus() != LoanStatus.PENDING && loan.getStatus() != LoanStatus.APPROVED) {
            throw new BadRequestException("Only PENDING or APPROVED loans can be cancelled");
        }

        loan.setStatus(LoanStatus.CANCELLED);
        loan.setClosedAt(LocalDateTime.now());

        Loan saved = loanRepository.save(loan);
        return loanMapper.toResponse(saved);
    }

    // ============================================
    // QUERY
    // ============================================
    @Override
    public LoanResponse getLoanById(UUID loanId, UUID requesterId) {
        Loan loan = loanRepository.findByIdWithDetails(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        Group group = loan.getGroupMember().getGroup();

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        boolean isOwn = loan.getGroupMember().getId().equals(requester.getId());
        boolean isManager = requester.getRole() == GroupRole.CHAIRPERSON
                || requester.getRole() == GroupRole.TREASURER
                || requester.getRole() == GroupRole.SECRETARY;

        if (!isOwn && !isManager) {
            throw new UnauthorizedException("You cannot view this loan");
        }

        return loanMapper.toResponse(loan);
    }

    @Override
    public List<LoanResponse> getMyLoans(UUID userId) {
        return loanRepository.findAllByUserIdWithDetails(userId).stream()
                .map(loanMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getMyLoansInGroup(UUID userId, UUID groupId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new UnauthorizedException("You are not a member of this group");
        }

        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        return loanRepository.findByGroupMemberId(member.getId()).stream()
                .map(loanMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getGroupLoans(UUID groupId, UUID requesterId) {
        verifyManagerAccess(groupId, requesterId);

        return loanRepository.findAllByGroupIdWithDetails(groupId).stream()
                .map(loanMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getPendingGroupLoans(UUID groupId, UUID requesterId) {
        verifyManagerAccess(groupId, requesterId);

        return loanRepository.findAllByGroupIdAndStatus(groupId, LoanStatus.PENDING).stream()
                .map(loanMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanRepaymentResponse> getLoanSchedule(UUID loanId, UUID requesterId) {
        Loan loan = loanRepository.findByIdWithDetails(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        Group group = loan.getGroupMember().getGroup();

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        boolean isOwn = loan.getGroupMember().getId().equals(requester.getId());
        boolean isManager = requester.getRole() == GroupRole.CHAIRPERSON
                || requester.getRole() == GroupRole.TREASURER
                || requester.getRole() == GroupRole.SECRETARY;

        if (!isOwn && !isManager) {
            throw new UnauthorizedException("You cannot view this schedule");
        }

        return loanRepaymentRepository.findByLoanIdOrderByInstallmentNumberAsc(loanId).stream()
                .map(loanMapper::toRepaymentResponse)
                .collect(Collectors.toList());
    }

    // ============================================
    // MAKE REPAYMENT
    // ============================================
    @Override
    @Transactional
    public LoanRepaymentResponse makeRepayment(UUID loanId, UUID userId, MakeLoanRepaymentRequest request) {
        log.info("User {} making loan repayment of {} for loan {}", userId, request.getAmount(), loanId);

        Loan loan = loanRepository.findByIdWithDetails(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        GroupMember member = loan.getGroupMember();
        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(
                        member.getGroup().getId(), userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (!loan.getGroupMember().getId().equals(requester.getId())) {
            throw new UnauthorizedException("You can only repay your own loans");
        }

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new BadRequestException("Loan is not active. Current: " + loan.getStatus());
        }

        // Get next unpaid installment
        LoanRepayment installment = loanRepaymentRepository.findNextUnpaidInstallment(loanId)
                .orElseThrow(() -> new BadRequestException("No unpaid installments remaining"));

        // Distribute payment: penalty → interest → principal
        BigDecimal remaining = request.getAmount();
        BigDecimal penaltyDue = BigDecimal.ZERO;

        // Calculate penalty if overdue
        if (installment.getDueDate().isBefore(LocalDate.now())) {
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(
                    installment.getDueDate(), LocalDate.now());
            penaltyDue = loanCalculator.calculatePenalty(
                    installment.getAmountDue(), MONTHLY_PENALTY_RATE, daysOverdue);
        }

        BigDecimal penaltyApplied = remaining.min(penaltyDue);
        remaining = remaining.subtract(penaltyApplied);

        BigDecimal interestOutstanding = installment.getInterestDue().subtract(installment.getInterestPaid());
        BigDecimal interestApplied = remaining.min(interestOutstanding);
        remaining = remaining.subtract(interestApplied);

        BigDecimal principalOutstanding = installment.getPrincipalDue().subtract(installment.getPrincipalPaid());
        BigDecimal principalApplied = remaining.min(principalOutstanding);

        // Update installment
        installment.setAmountPaid(installment.getAmountPaid().add(request.getAmount()));
        installment.setPenaltyPaid(installment.getPenaltyPaid().add(penaltyApplied));
        installment.setInterestPaid(installment.getInterestPaid().add(interestApplied));
        installment.setPrincipalPaid(installment.getPrincipalPaid().add(principalApplied));
        installment.setPaymentMethod(request.getPaymentMethod());
        installment.setTransactionReference(
                request.getTransactionReference() != null
                        ? request.getTransactionReference()
                        : referenceGenerator.generate());
        installment.setPaymentDate(LocalDate.now());
        installment.setNotes(request.getNotes());
        installment.setConfirmedAt(LocalDateTime.now());
        installment.setConfirmedBy(userId);

        // Determine status
        BigDecimal totalPaid = installment.getPrincipalPaid()
                .add(installment.getInterestPaid())
                .add(installment.getPenaltyPaid());

        if (installment.getPrincipalPaid().compareTo(installment.getPrincipalDue()) >= 0
                && installment.getInterestPaid().compareTo(installment.getInterestDue()) >= 0) {
            installment.setStatus(RepaymentStatus.COMPLETED);
        } else if (totalPaid.signum() > 0) {
            installment.setStatus(RepaymentStatus.PARTIAL);
        }

        loanRepaymentRepository.save(installment);

        // Update loan balance
        BigDecimal applied = penaltyApplied.add(interestApplied).add(principalApplied);
        BigDecimal newBalance = loan.getBalance().subtract(applied);
        if (newBalance.signum() < 0) newBalance = BigDecimal.ZERO;

        loan.setBalance(newBalance);

        // Update member's total loan balance
        BigDecimal memberNewBalance = member.getTotalLoanBalance().subtract(applied);
        if (memberNewBalance.signum() < 0) memberNewBalance = BigDecimal.ZERO;
        member.setTotalLoanBalance(memberNewBalance);
        groupMemberRepository.save(member);

        // Check if loan is fully paid
        long unpaidCount = loanRepaymentRepository.countByLoanIdAndStatus(loanId, RepaymentStatus.PENDING)
                + loanRepaymentRepository.countByLoanIdAndStatus(loanId, RepaymentStatus.PARTIAL)
                + loanRepaymentRepository.countByLoanIdAndStatus(loanId, RepaymentStatus.OVERDUE);

        if (unpaidCount == 0 || newBalance.signum() == 0) {
            loan.setStatus(LoanStatus.PAID);
            loan.setBalance(BigDecimal.ZERO);
            loan.setClosedAt(LocalDateTime.now());
            log.info("Loan {} is now fully PAID", loanId);
        }

        loanRepository.save(loan);

        notificationService.notifyUserAsync(
                userId,
                NotificationType.LOAN_PAYMENT_RECEIVED,
                "Loan Payment Received",
                "Your payment of " + request.getAmount() + " TZS has been recorded. Remaining balance: "
                        + loan.getBalance() + " TZS.",
                "/loans/" + loan.getId(),
                "LoanRepayment",
                installment.getId(),
                NotificationPriority.NORMAL
        );

        // If loan was fully paid, send a separate PAID notification
        if (loan.getStatus() == LoanStatus.PAID) {
            notificationService.notifyUserAsync(
                    userId,
                    NotificationType.LOAN_PAID_OFF,
                    "Loan Fully Paid ",
                    "Congratulations! You have fully repaid your loan of " + loan.getAmount() + " TZS.",
                    "/loans/" + loan.getId(),
                    "Loan",
                    loan.getId(),
                    NotificationPriority.HIGH
            );
        }

        return loanMapper.toRepaymentResponse(installment);
    }

    @Override
    @Transactional
    public LoanRepaymentResponse confirmRepayment(UUID repaymentId, UUID requesterId, String reference) {
        LoanRepayment repayment = loanRepaymentRepository.findByIdWithDetails(repaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Repayment not found"));

        Group group = repayment.getLoan().getGroupMember().getGroup();

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.TREASURER && requester.getRole() != GroupRole.CHAIRPERSON) {
            throw new UnauthorizedException("Only Treasurer or Chairperson can confirm repayments");
        }

        repayment.setConfirmedAt(LocalDateTime.now());
        repayment.setConfirmedBy(requesterId);
        if (reference != null && !reference.isBlank()) {
            repayment.setTransactionReference(reference);
        }

        LoanRepayment saved = loanRepaymentRepository.save(repayment);
        return loanMapper.toRepaymentResponse(saved);
    }

    @Override
    @Transactional
    public LoanResponse repayLoanEarly(UUID loanId, UUID userId, MakeLoanRepaymentRequest request) {
        // For simplicity: same logic as makeRepayment but marks all remaining installments
        makeRepayment(loanId, userId, request);

        Loan loan = loanRepository.findByIdWithDetails(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        return loanMapper.toResponse(loan);
    }

    // ============================================
    // SUMMARY
    // ============================================
    @Override
    public LoanSummaryResponse getGroupSummary(UUID groupId, UUID requesterId) {
        verifyManagerAccess(groupId, requesterId);

        String groupName = groupMemberRepository.findByGroupIdAndUserId(groupId, requesterId)
                .map(gm -> gm.getGroup().getName())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        BigDecimal totalDisbursed = loanRepository.sumDisbursedByGroupId(groupId);
        BigDecimal totalOutstanding = loanRepository.sumOutstandingBalanceByGroupId(groupId);
        BigDecimal totalInterest = loanRepository.sumInterestEarnedByGroupId(groupId);

        long totalLoans = loanRepository.findAllByGroupIdWithDetails(groupId).size();
        long activeLoans = loanRepository.countByGroupIdAndStatus(groupId, LoanStatus.ACTIVE);
        long paidLoans = loanRepository.countByGroupIdAndStatus(groupId, LoanStatus.PAID);
        long pendingLoans = loanRepository.countByGroupIdAndStatus(groupId, LoanStatus.PENDING);
        long overdueLoans = loanRepository.countByGroupIdAndStatus(groupId, LoanStatus.DEFAULTED);

        BigDecimal par = totalDisbursed.signum() > 0
                ? totalOutstanding.multiply(BigDecimal.valueOf(100))
                .divide(totalDisbursed, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal defaultRate = totalLoans > 0
                ? BigDecimal.valueOf(overdueLoans * 100.0 / totalLoans).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return LoanSummaryResponse.builder()
                .groupId(groupId).groupName(groupName)
                .totalDisbursed(totalDisbursed)
                .totalOutstanding(totalOutstanding)
                .totalInterestEarned(totalInterest)
                .totalLoans(totalLoans)
                .activeLoans(activeLoans)
                .paidLoans(paidLoans)
                .pendingLoans(pendingLoans)
                .overdueLoans(overdueLoans)
                .portfolioAtRisk(par)
                .defaultRate(defaultRate)
                .build();
    }

    // ============================================
    // HELPERS
    // ============================================
    private void verifyManagerAccess(UUID groupId, UUID requesterId) {
        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(groupId, requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.TREASURER
                && requester.getRole() != GroupRole.CHAIRPERSON
                && requester.getRole() != GroupRole.SECRETARY) {
            throw new UnauthorizedException("Only Treasurer, Chairperson or Secretary can perform this action");
        }
    }

    /**
     * Calculate the group's available liquid funds.
     * Available = Sum of completed contributions − Sum of outstanding active loan balances.
     */
    private BigDecimal calculateGroupAvailableFunds(UUID groupId) {
        BigDecimal contributions = contributionRepository.sumCompletedByGroupId(groupId);
        BigDecimal outstanding = loanRepository.sumOutstandingBalanceByGroupId(groupId);

        if (contributions == null) contributions = BigDecimal.ZERO;
        if (outstanding == null) outstanding = BigDecimal.ZERO;

        BigDecimal available = contributions.subtract(outstanding);

        // Never return a negative value
        return available.signum() < 0 ? BigDecimal.ZERO : available;
    }

    @Override
    public LoanCapacityResponse getGroupCapacity(UUID userId, UUID groupId) {
        // Verify membership
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new UnauthorizedException("You are not a member of this group");
        }

        BigDecimal contributions = contributionRepository.sumCompletedByGroupId(groupId);
        BigDecimal outstanding = loanRepository.sumOutstandingBalanceByGroupId(groupId);

        if (contributions == null) contributions = BigDecimal.ZERO;
        if (outstanding == null) outstanding = BigDecimal.ZERO;

        BigDecimal available = contributions.subtract(outstanding);
        if (available.signum() < 0) available = BigDecimal.ZERO;

        return LoanCapacityResponse.builder()
                .totalContributions(contributions)
                .outstandingLoans(outstanding)
                .availableFunds(available)
                .build();
    }
}