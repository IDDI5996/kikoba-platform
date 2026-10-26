package com.kikoba.kikoba_backend.dividend.service.impl;

import com.kikoba.kikoba_backend.dividend.dto.CalculateDividendRequest;
import com.kikoba.kikoba_backend.dividend.dto.DividendPayoutResponse;
import com.kikoba.kikoba_backend.dividend.dto.DividendResponse;
import com.kikoba.kikoba_backend.dividend.entity.Dividend;
import com.kikoba.kikoba_backend.dividend.entity.DividendPayout;
import com.kikoba.kikoba_backend.dividend.enums.DividendStatus;
import com.kikoba.kikoba_backend.dividend.enums.PayoutStatus;
import com.kikoba.kikoba_backend.dividend.mapper.DividendMapper;
import com.kikoba.kikoba_backend.dividend.repository.DividendPayoutRepository;
import com.kikoba.kikoba_backend.dividend.repository.DividendRepository;
import com.kikoba.kikoba_backend.dividend.service.DividendService;
import com.kikoba.kikoba_backend.dividend.util.DividendCalculator;
import com.kikoba.kikoba_backend.exception.BadRequestException;
import com.kikoba.kikoba_backend.exception.ResourceAlreadyExistsException;
import com.kikoba.kikoba_backend.exception.ResourceNotFoundException;
import com.kikoba.kikoba_backend.exception.UnauthorizedException;
import com.kikoba.kikoba_backend.group.entity.Group;
import com.kikoba.kikoba_backend.group.entity.GroupMember;
import com.kikoba.kikoba_backend.group.enums.GroupRole;
import com.kikoba.kikoba_backend.group.repository.GroupMemberRepository;
import com.kikoba.kikoba_backend.loan.repository.LoanRepository;
import com.kikoba.kikoba_backend.loan.repository.LoanRepaymentRepository;
import com.kikoba.kikoba_backend.notification.enums.NotificationPriority;
import com.kikoba.kikoba_backend.notification.enums.NotificationType;
import com.kikoba.kikoba_backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DividendServiceImpl implements DividendService {

    private final DividendRepository dividendRepository;
    private final DividendPayoutRepository dividendPayoutRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final LoanRepository loanRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final DividendMapper dividendMapper;
    private final DividendCalculator dividendCalculator;
    private final NotificationService notificationService;

    // ============================================
    // CALCULATE DIVIDEND
    // ============================================
    @Override
    @Transactional
    public DividendResponse calculateDividend(UUID groupId, UUID requesterId, CalculateDividendRequest request) {
        log.info("User {} calculating dividend for group {} cycle {}",
                requesterId, groupId, request.getCycleNumber());

        // 1. Verify requester is a manager
        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(groupId, requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.CHAIRPERSON && requester.getRole() != GroupRole.TREASURER) {
            throw new UnauthorizedException("Only Chairperson or Treasurer can calculate dividends");
        }

        // 2. Check for existing dividend in this cycle
        if (dividendRepository.existsByGroupIdAndCycleNumber(groupId, request.getCycleNumber())) {
            throw new ResourceAlreadyExistsException(
                    "A dividend for cycle " + request.getCycleNumber() + " already exists");
        }

        Group group = requester.getGroup();

        // 3. Calculate total interest earned from loans
        BigDecimal interestEarned = loanRepository.sumInterestEarnedByGroupId(groupId);
        if (interestEarned == null) interestEarned = BigDecimal.ZERO;

        // 4. Calculate penalties collected (from completed loan repayments)
        BigDecimal penaltiesCollected = calculatePenaltiesCollected(groupId);

        // 5. Calculate total shares
        List<GroupMember> members = groupMemberRepository.findByGroupIdAndIsActiveTrue(groupId);
        int totalShares = members.stream()
                .mapToInt(m -> m.getShareBalance().intValue())
                .sum();

        if (totalShares <= 0) {
            throw new BadRequestException("Cannot calculate dividend: no shares have been purchased");
        }

        // 6. Calculate total profit
        BigDecimal profit = dividendCalculator.calculateTotalProfit(
                interestEarned, penaltiesCollected, request.getTotalExpenses());

        if (profit.signum() <= 0) {
            throw new BadRequestException(
                    "Cannot calculate dividend: no profit available. Profit: " + profit);
        }

        // 7. Calculate dividend per share
        BigDecimal dividendPerShare = dividendCalculator.calculateDividendPerShare(profit, totalShares);

        // 8. Create Dividend entity
        Dividend dividend = Dividend.builder()
                .group(group)
                .cycleNumber(request.getCycleNumber())
                .totalInterestEarned(interestEarned)
                .totalPenaltiesCollected(penaltiesCollected)
                .totalExpenses(request.getTotalExpenses())
                .totalProfit(profit)
                .totalShares(totalShares)
                .dividendPerShare(dividendPerShare)
                .status(DividendStatus.CALCULATED)
                .calculatedBy(requester.getUser())
                .calculatedAt(LocalDateTime.now())
                .notes(request.getNotes())
                .build();

        Dividend saved = dividendRepository.save(dividend);

        log.info("Dividend {} calculated: Profit={}, PerShare={}", saved.getId(), profit, dividendPerShare);

        return dividendMapper.toResponse(
                dividendRepository.findByIdWithDetails(saved.getId()).orElseThrow());
    }

    // ============================================
    // APPROVE DIVIDEND
    // ============================================
    @Override
    @Transactional
    public DividendResponse approveDividend(UUID dividendId, UUID requesterId) {
        log.info("User {} approving dividend {}", requesterId, dividendId);

        Dividend dividend = dividendRepository.findByIdWithDetails(dividendId)
                .orElseThrow(() -> new ResourceNotFoundException("Dividend not found"));

        Group group = dividend.getGroup();

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.CHAIRPERSON && requester.getRole() != GroupRole.TREASURER) {
            throw new UnauthorizedException("Only Chairperson or Treasurer can approve dividends");
        }

        // Self-approval block (Golden Rule #15)
        if (dividend.getCalculatedBy() != null
                && dividend.getCalculatedBy().getId().equals(requesterId)) {
            throw new BadRequestException("You cannot approve a dividend you calculated. Another manager must approve.");
        }

        if (dividend.getStatus() != DividendStatus.CALCULATED) {
            throw new BadRequestException("Only CALCULATED dividends can be approved. Current: " + dividend.getStatus());
        }

        dividend.setStatus(DividendStatus.APPROVED);
        dividend.setApprovedBy(requester.getUser());
        dividend.setApprovedAt(LocalDateTime.now());

        Dividend saved = dividendRepository.save(dividend);

        log.info("Dividend {} approved", dividendId);
        return dividendMapper.toResponse(dividendRepository.findByIdWithDetails(saved.getId()).orElseThrow());
    }

    // ============================================
    // DISTRIBUTE DIVIDEND
    // ============================================
    @Override
    @Transactional
    public DividendResponse distributeDividend(UUID dividendId, UUID requesterId) {
        log.info("User {} distributing dividend {}", requesterId, dividendId);

        Dividend dividend = dividendRepository.findByIdWithDetails(dividendId)
                .orElseThrow(() -> new ResourceNotFoundException("Dividend not found"));

        Group group = dividend.getGroup();

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.CHAIRPERSON && requester.getRole() != GroupRole.TREASURER) {
            throw new UnauthorizedException("Only Chairperson or Treasurer can distribute dividends");
        }

        if (dividend.getStatus() != DividendStatus.APPROVED) {
            throw new BadRequestException("Only APPROVED dividends can be distributed. Current: " + dividend.getStatus());
        }

        // Generate payouts for all members with shares
        List<GroupMember> members = groupMemberRepository.findByGroupIdAndIsActiveTrue(group.getId());

        int payoutsCreated = 0;
        BigDecimal totalDistributed = BigDecimal.ZERO;

        for (GroupMember member : members) {
            int memberShares = member.getShareBalance().intValue();
            if (memberShares <= 0) continue;

            BigDecimal payout = dividendCalculator.calculateMemberPayout(
                    memberShares, dividend.getDividendPerShare());

            DividendPayout payoutEntity = DividendPayout.builder()
                    .dividend(dividend)
                    .groupMember(member)
                    .sharesHeld(memberShares)
                    .amount(payout)
                    .status(PayoutStatus.PENDING)
                    .build();

            dividendPayoutRepository.save(payoutEntity);
            totalDistributed = totalDistributed.add(payout);
            payoutsCreated++;
        }

        if (payoutsCreated == 0) {
            throw new BadRequestException("No members with shares to pay dividends to");
        }

        // Update dividend status
        dividend.setStatus(DividendStatus.DISTRIBUTED);
        dividend.setDistributedBy(requester.getUser());
        dividend.setDistributedAt(LocalDateTime.now());

        Dividend saved = dividendRepository.save(dividend);

        log.info("Dividend {} distributed. {} payouts, total {}",
                dividendId, payoutsCreated, totalDistributed);

        // Notify each member of their payout
        for (GroupMember member : members) {
            int memberShares = member.getShareBalance().intValue();
            if (memberShares <= 0) continue;

            BigDecimal payout = dividendCalculator.calculateMemberPayout(
                    memberShares, dividend.getDividendPerShare());

            notificationService.notifyUserAsync(
                    member.getUser().getId(),
                    NotificationType.DIVIDEND_DISTRIBUTED,
                    "Dividend Distributed ",
                    "Cycle " + dividend.getCycleNumber() + " dividend has been distributed. "
                            + "Your payout: " + payout + " TZS for " + memberShares + " shares.",
                    "/dividends/" + dividend.getId(),
                    "Dividend",
                    dividend.getId(),
                    NotificationPriority.HIGH
            );
        }

        return dividendMapper.toResponse(dividendRepository.findByIdWithDetails(saved.getId()).orElseThrow());
    }

    // ============================================
    // CANCEL DIVIDEND
    // ============================================
    @Override
    @Transactional
    public DividendResponse cancelDividend(UUID dividendId, UUID requesterId, String reason) {
        Dividend dividend = dividendRepository.findByIdWithDetails(dividendId)
                .orElseThrow(() -> new ResourceNotFoundException("Dividend not found"));

        Group group = dividend.getGroup();

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.CHAIRPERSON) {
            throw new UnauthorizedException("Only the Chairperson can cancel a dividend");
        }

        if (dividend.getStatus() == DividendStatus.DISTRIBUTED) {
            throw new BadRequestException("Cannot cancel a distributed dividend");
        }

        dividend.setStatus(DividendStatus.CANCELLED);
        dividend.setNotes((dividend.getNotes() != null ? dividend.getNotes() + "\n" : "")
                + "CANCELLED: " + reason);

        Dividend saved = dividendRepository.save(dividend);
        return dividendMapper.toResponse(saved);
    }

    // ============================================
    // QUERY
    // ============================================
    @Override
    public DividendResponse getDividendById(UUID dividendId, UUID requesterId) {
        Dividend dividend = dividendRepository.findByIdWithDetails(dividendId)
                .orElseThrow(() -> new ResourceNotFoundException("Dividend not found"));

        Group group = dividend.getGroup();

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        return dividendMapper.toResponse(dividend);
    }

    @Override
    public List<DividendResponse> getGroupDividends(UUID groupId, UUID requesterId) {
        verifyMemberAccess(groupId, requesterId);

        return dividendRepository.findAllByGroupIdWithDetails(groupId).stream()
                .map(dividendMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DividendPayoutResponse> getDividendPayouts(UUID dividendId, UUID requesterId) {
        Dividend dividend = dividendRepository.findByIdWithDetails(dividendId)
                .orElseThrow(() -> new ResourceNotFoundException("Dividend not found"));

        Group group = dividend.getGroup();

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        // Only managers can view all payouts of a dividend
        if (requester.getRole() != GroupRole.CHAIRPERSON
                && requester.getRole() != GroupRole.TREASURER
                && requester.getRole() != GroupRole.SECRETARY) {
            throw new UnauthorizedException("Only managers can view all dividend payouts");
        }

        return dividendPayoutRepository.findAllByDividendIdWithDetails(dividendId).stream()
                .map(dividendMapper::toPayoutResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DividendPayoutResponse> getMyDividends(UUID userId) {
        return dividendPayoutRepository.findAllByUserIdWithDetails(userId).stream()
                .map(dividendMapper::toPayoutResponse)
                .collect(Collectors.toList());
    }

    // ============================================
    // CONFIRM PAYOUT
    // ============================================
    @Override
    @Transactional
    public DividendPayoutResponse confirmPayout(UUID payoutId, UUID requesterId, String reference) {
        DividendPayout payout = dividendPayoutRepository.findByIdWithDetails(payoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Payout not found"));

        Group group = payout.getDividend().getGroup();

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.TREASURER && requester.getRole() != GroupRole.CHAIRPERSON) {
            throw new UnauthorizedException("Only Treasurer or Chairperson can confirm payouts");
        }

        if (payout.getStatus() == PayoutStatus.PAID) {
            throw new BadRequestException("Payout already confirmed");
        }

        payout.setStatus(PayoutStatus.PAID);
        payout.setPaidAt(LocalDateTime.now());
        payout.setConfirmedBy(requesterId);
        if (reference != null && !reference.isBlank()) {
            payout.setPaymentReference(reference);
        }

        DividendPayout saved = dividendPayoutRepository.save(payout);

        notificationService.notifyUserAsync(
                payout.getGroupMember().getUser().getId(),
                NotificationType.DIVIDEND_PAYOUT_RECEIVED,
                "Dividend Payout Confirmed",
                "Your dividend payout of " + payout.getAmount() + " TZS has been confirmed.",
                "/dividends/my",
                "DividendPayout",
                payout.getId(),
                NotificationPriority.NORMAL
        );

        return dividendMapper.toPayoutResponse(saved);
    }

    // ============================================
    // HELPERS
    // ============================================

    /**
     * Calculate total penalties collected from all loans in a group.
     *
     * Penalties are tracked on the {@link com.kikoba.kikoba_backend.loan.entity.LoanRepayment}
     * entity via the {@code penaltyPaid} field, which accumulates over time as
     * members pay late fees on overdue installments.
     *
     * This method aggregates them across all loans belonging to the group.
     *
     * @param groupId The group whose penalties we want to sum
     * @return Total penalties collected (never null, always ≥ 0)
     */
    private BigDecimal calculatePenaltiesCollected(UUID groupId) {
        log.debug("Calculating penalties collected for group {}", groupId);

        BigDecimal total = loanRepaymentRepository.sumPenaltiesCollectedByGroupId(groupId);

        // Defensive: query uses COALESCE but still guard against null
        if (total == null) {
            total = BigDecimal.ZERO;
        }

        log.debug("Total penalties collected for group {}: {}", groupId, total);
        return total;
    }

    private void verifyMemberAccess(UUID groupId, UUID requesterId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, requesterId)) {
            throw new UnauthorizedException("You are not a member of this group");
        }
    }
}