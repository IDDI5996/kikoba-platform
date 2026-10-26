package com.kikoba.kikoba_backend.contribution.service.impl;

import com.kikoba.kikoba_backend.contribution.dto.*;
import com.kikoba.kikoba_backend.contribution.entity.Contribution;
import com.kikoba.kikoba_backend.contribution.enums.ContributionStatus;
import com.kikoba.kikoba_backend.contribution.mapper.ContributionMapper;
import com.kikoba.kikoba_backend.contribution.repository.ContributionRepository;
import com.kikoba.kikoba_backend.contribution.service.ContributionService;
import com.kikoba.kikoba_backend.contribution.util.PaymentReferenceGenerator;
import com.kikoba.kikoba_backend.exception.BadRequestException;
import com.kikoba.kikoba_backend.exception.ResourceNotFoundException;
import com.kikoba.kikoba_backend.exception.UnauthorizedException;
import com.kikoba.kikoba_backend.group.entity.Group;
import com.kikoba.kikoba_backend.group.entity.GroupMember;
import com.kikoba.kikoba_backend.group.enums.GroupRole;
import com.kikoba.kikoba_backend.group.repository.GroupMemberRepository;
import com.kikoba.kikoba_backend.notification.enums.NotificationType;
import com.kikoba.kikoba_backend.notification.service.NotificationService;
import com.kikoba.kikoba_backend.notification.enums.NotificationPriority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ContributionServiceImpl implements ContributionService {

    private final ContributionRepository contributionRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ContributionMapper contributionMapper;
    private final PaymentReferenceGenerator referenceGenerator;
    private final NotificationService notificationService;

    // ============================================
    // MAKE CONTRIBUTION
    // ============================================
    @Override
    @Transactional
    public ContributionResponse makeContribution(UUID userId, UUID groupId, MakeContributionRequest request) {
        log.info("User {} making contribution of {} to group {}", userId, request.getAmount(), groupId);

        // 1. Verify user is an active member of the group
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (!member.getIsActive()) {
            throw new BadRequestException("Your membership is inactive. Contact your group leader.");
        }

        // 2. Validate amount against group min/max
        Group group = member.getGroup();
        validateAmount(group, request.getAmount());

        // 3. Generate transaction reference if not provided
        String reference = request.getTransactionReference();
        if (reference == null || reference.isBlank()) {
            reference = referenceGenerator.generate();
            log.info("Generated reference: {}", reference);
        } else {
            // Check uniqueness if provided
            if (contributionRepository.existsByTransactionReference(reference)) {
                throw new BadRequestException("Transaction reference already exists");
            }
        }

        // 4. Create the contribution
        Contribution contribution = Contribution.builder()
                .groupMember(member)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .transactionReference(reference)
                .status(ContributionStatus.PENDING)
                .contributionPeriod(request.getContributionPeriod() != null
                        ? request.getContributionPeriod()
                        : LocalDate.now().withDayOfMonth(1))
                .notes(request.getNotes())
                .receiptUrl(request.getReceiptUrl())
                .build();

        Contribution saved = contributionRepository.save(contribution);

        log.info("Contribution {} created with status PENDING", saved.getId());

        // 5. Reload with all relationships for DTO
        Contribution loaded = contributionRepository.findByIdWithDetails(saved.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Contribution not found after save"));

        // 6. Notify treasurer/chairperson of pending contribution
        notificationService.notifyUserAsync(
                member.getUser().getId(),
                NotificationType.CONTRIBUTION_RECEIVED,
                "Contribution Submitted",
                "Your contribution of " + contribution.getAmount() + " TZS has been submitted and is pending confirmation.",
                "/contributions/" + saved.getId(),
                "Contribution",
                saved.getId(),
                NotificationPriority.NORMAL
        );

        return contributionMapper.toResponse(loaded);
    }

    // ============================================
    // CONFIRM CONTRIBUTION (Treasurer/Chairperson)
    // ============================================
    @Override
    @Transactional
    public ContributionResponse confirmContribution(UUID contributionId, UUID requesterId,
                                                    ConfirmContributionRequest request) {
        log.info("User {} confirming contribution {}", requesterId, contributionId);

        Contribution contribution = contributionRepository.findByIdWithDetails(contributionId)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution not found"));

        GroupMember member = contribution.getGroupMember();
        Group group = member.getGroup();

        // 1. Verify requester is a member with appropriate role
        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.TREASURER && requester.getRole() != GroupRole.CHAIRPERSON) {
            throw new UnauthorizedException("Only Treasurer or Chairperson can confirm contributions");
        }

        // 2. Check status
        if (contribution.getStatus() == ContributionStatus.COMPLETED) {
            throw new BadRequestException("Contribution is already completed");
        }

        if (contribution.getStatus() == ContributionStatus.CANCELLED) {
            throw new BadRequestException("Cannot confirm a cancelled contribution");
        }

        // 3. Update the contribution
        contribution.setStatus(ContributionStatus.COMPLETED);
        contribution.setConfirmedAt(LocalDateTime.now());
        contribution.setConfirmedBy(requesterId);

        if (request != null) {
            if (request.getTransactionReference() != null && !request.getTransactionReference().isBlank()) {
                contribution.setTransactionReference(request.getTransactionReference());
            }
            if (request.getNotes() != null && !request.getNotes().isBlank()) {
                contribution.setNotes(request.getNotes());
            }
        }

        Contribution saved = contributionRepository.save(contribution);

        // 4. Update the GroupMember's total_contributed
        BigDecimal newTotal = member.getTotalContributed().add(contribution.getAmount());
        member.setTotalContributed(newTotal);
        groupMemberRepository.save(member);

        log.info("Contribution {} confirmed. Member new total: {}", contributionId, newTotal);

        // 5. Reload and return
        Contribution reloaded = contributionRepository.findByIdWithDetails(saved.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Contribution not found after confirm"));

        // 6. Notify the contributor
        notificationService.notifyUserAsync(
                member.getUser().getId(),
                NotificationType.CONTRIBUTION_CONFIRMED,
                "Contribution Confirmed",
                "Your contribution of " + contribution.getAmount() + " TZS has been confirmed.",
                "/contributions/" + saved.getId(),
                "Contribution",
                saved.getId(),
                NotificationPriority.NORMAL
        );

        return contributionMapper.toResponse(reloaded);
    }

    // ============================================
    // CANCEL CONTRIBUTION
    // ============================================
    @Override
    @Transactional
    public ContributionResponse cancelContribution(UUID contributionId, UUID requesterId) {
        log.info("User {} cancelling contribution {}", requesterId, contributionId);

        Contribution contribution = contributionRepository.findByIdWithDetails(contributionId)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution not found"));

        Group group = contribution.getGroupMember().getGroup();

        // Verify requester is Treasurer/Chairperson or the contributor
        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        boolean isOwnContribution = contribution.getGroupMember().getId().equals(requester.getId());
        boolean isManager = requester.getRole() == GroupRole.TREASURER
                || requester.getRole() == GroupRole.CHAIRPERSON;

        if (!isOwnContribution && !isManager) {
            throw new UnauthorizedException("You cannot cancel this contribution");
        }

        if (contribution.getStatus() == ContributionStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel a completed contribution. Contact your Treasurer.");
        }

        if (contribution.getStatus() == ContributionStatus.CANCELLED) {
            throw new BadRequestException("Contribution is already cancelled");
        }

        contribution.setStatus(ContributionStatus.CANCELLED);
        Contribution saved = contributionRepository.save(contribution);

        log.info("Contribution {} cancelled", contributionId);

        Contribution reloaded = contributionRepository.findByIdWithDetails(saved.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Contribution not found"));

        return contributionMapper.toResponse(reloaded);
    }

    // ============================================
    // GET SINGLE CONTRIBUTION
    // ============================================
    @Override
    public ContributionResponse getContributionById(UUID contributionId, UUID requesterId) {
        Contribution contribution = contributionRepository.findByIdWithDetails(contributionId)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution not found"));

        Group group = contribution.getGroupMember().getGroup();

        // Verify requester is a member
        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        // Members can see only their own; Managers can see all
        boolean isOwn = contribution.getGroupMember().getId().equals(requester.getId());
        boolean isManager = requester.getRole() == GroupRole.TREASURER
                || requester.getRole() == GroupRole.CHAIRPERSON
                || requester.getRole() == GroupRole.SECRETARY;

        if (!isOwn && !isManager) {
            throw new UnauthorizedException("You cannot view this contribution");
        }

        return contributionMapper.toResponse(contribution);
    }

    // ============================================
    // GET MY CONTRIBUTIONS
    // ============================================
    @Override
    public List<ContributionResponse> getMyContributions(UUID userId) {
        return contributionRepository.findAllByUserIdWithDetails(userId).stream()
                .map(contributionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ContributionResponse> getMyContributionsInGroup(UUID userId, UUID groupId) {
        // Verify user is member
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new UnauthorizedException("You are not a member of this group");
        }

        return contributionRepository.findAllByUserIdAndGroupId(userId, groupId).stream()
                .map(contributionMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ============================================
    // GET GROUP CONTRIBUTIONS (Treasurer/Chairperson)
    // ============================================
    @Override
    public List<ContributionResponse> getGroupContributions(UUID groupId, UUID requesterId) {
        verifyManagerAccess(groupId, requesterId);

        return contributionRepository.findAllByGroupIdWithDetails(groupId).stream()
                .map(contributionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ContributionResponse> getPendingGroupContributions(UUID groupId, UUID requesterId) {
        verifyManagerAccess(groupId, requesterId);

        return contributionRepository.findAllByGroupIdAndStatus(groupId, ContributionStatus.PENDING).stream()
                .map(contributionMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ============================================
    // SUMMARIES
    // ============================================
    @Override
    public ContributionSummaryResponse getMySummary(UUID userId, UUID groupId) {
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        BigDecimal total = contributionRepository.sumCompletedByMemberId(member.getId());
        long count = contributionRepository.countByGroupMemberIdAndStatus(member.getId(), ContributionStatus.COMPLETED);

        LocalDate currentPeriod = LocalDate.now().withDayOfMonth(1);
        BigDecimal currentPeriodTotal = contributionRepository
                .sumCompletedByGroupAndPeriod(groupId, currentPeriod);

        return ContributionSummaryResponse.builder()
                .groupId(groupId)
                .groupName(member.getGroup().getName())
                .totalContributed(total)
                .totalTransactions(count)
                .currentPeriodTotal(currentPeriodTotal)
                .currentPeriod(currentPeriod)
                .build();
    }

    @Override
    public ContributionSummaryResponse getGroupSummary(UUID groupId, UUID requesterId) {
        verifyManagerAccess(groupId, requesterId);

        Group group = groupMemberRepository.findByGroupIdAndUserId(groupId, requesterId)
                .map(GroupMember::getGroup)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        BigDecimal total = contributionRepository.sumCompletedByGroupId(groupId);
        long count = contributionRepository.findAllByGroupIdWithDetails(groupId).stream()
                .filter(c -> c.getStatus() == ContributionStatus.COMPLETED)
                .count();

        LocalDate currentPeriod = LocalDate.now().withDayOfMonth(1);
        BigDecimal currentPeriodTotal = contributionRepository
                .sumCompletedByGroupAndPeriod(groupId, currentPeriod);

        return ContributionSummaryResponse.builder()
                .groupId(groupId)
                .groupName(group.getName())
                .totalContributed(total)
                .totalTransactions(count)
                .currentPeriodTotal(currentPeriodTotal)
                .currentPeriod(currentPeriod)
                .build();
    }

    // ============================================
    // HELPERS
    // ============================================
    private void validateAmount(Group group, BigDecimal amount) {
        if (group.getMinContribution() != null && amount.compareTo(group.getMinContribution()) < 0) {
            throw new BadRequestException("Amount is below the group minimum of " + group.getMinContribution());
        }
        if (group.getMaxContribution() != null && amount.compareTo(group.getMaxContribution()) > 0) {
            throw new BadRequestException("Amount exceeds the group maximum of " + group.getMaxContribution());
        }
    }

    private void verifyManagerAccess(UUID groupId, UUID requesterId) {
        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(groupId, requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.TREASURER
                && requester.getRole() != GroupRole.CHAIRPERSON
                && requester.getRole() != GroupRole.SECRETARY) {
            throw new UnauthorizedException("Only Treasurer, Chairperson or Secretary can perform this action");
        }
    }
}