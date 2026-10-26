package com.kikoba.kikoba_backend.share.service.impl;

import com.kikoba.kikoba_backend.exception.BadRequestException;
import com.kikoba.kikoba_backend.exception.ResourceNotFoundException;
import com.kikoba.kikoba_backend.exception.UnauthorizedException;
import com.kikoba.kikoba_backend.group.entity.Group;
import com.kikoba.kikoba_backend.group.entity.GroupMember;
import com.kikoba.kikoba_backend.group.enums.GroupRole;
import com.kikoba.kikoba_backend.group.repository.GroupMemberRepository;
import com.kikoba.kikoba_backend.share.dto.PurchaseSharesRequest;
import com.kikoba.kikoba_backend.share.dto.ShareResponse;
import com.kikoba.kikoba_backend.share.dto.ShareSummaryResponse;
import com.kikoba.kikoba_backend.share.entity.Share;
import com.kikoba.kikoba_backend.share.enums.ShareTransactionType;
import com.kikoba.kikoba_backend.share.mapper.ShareMapper;
import com.kikoba.kikoba_backend.share.repository.ShareRepository;
import com.kikoba.kikoba_backend.share.service.ShareService;
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
public class ShareServiceImpl implements ShareService {

    private final ShareRepository shareRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ShareMapper shareMapper;

    @Override
    @Transactional
    public ShareResponse purchaseShares(UUID userId, UUID groupId, PurchaseSharesRequest request) {
        log.info("User {} purchasing {} shares in group {}", userId, request.getNumberOfShares(), groupId);

        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (!member.getIsActive()) {
            throw new BadRequestException("Your membership is inactive");
        }

        Group group = member.getGroup();
        BigDecimal pricePerShare = group.getSharePrice() != null
                ? group.getSharePrice()
                : new BigDecimal("100.00");

        BigDecimal totalAmount = pricePerShare
                .multiply(BigDecimal.valueOf(request.getNumberOfShares()));

        Share share = Share.builder()
                .groupMember(member)
                .groupId(group.getId())
                .numberOfShares(request.getNumberOfShares())
                .pricePerShare(pricePerShare)
                .totalAmount(totalAmount)
                .transactionType(ShareTransactionType.PURCHASE)
                .transactionReference(request.getTransactionReference())
                .notes(request.getNotes())
                .confirmedAt(LocalDateTime.now())
                .build();

        Share saved = shareRepository.save(share);

        // Update member's share balance
        int currentShares = member.getShareBalance().intValue();
        member.setShareBalance(BigDecimal.valueOf(currentShares + request.getNumberOfShares()));
        groupMemberRepository.save(member);

        log.info("Share {} created. Member new balance: {}", saved.getId(), member.getShareBalance());

        return shareMapper.toResponse(saved);
    }

    @Override
    public ShareResponse getShareById(UUID shareId, UUID requesterId) {
        Share share = shareRepository.findByIdWithDetails(shareId)
                .orElseThrow(() -> new ResourceNotFoundException("Share not found"));

        Group group = share.getGroupMember().getGroup();

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        boolean isOwn = share.getGroupMember().getId().equals(requester.getId());
        boolean isManager = requester.getRole() == GroupRole.TREASURER
                || requester.getRole() == GroupRole.CHAIRPERSON
                || requester.getRole() == GroupRole.SECRETARY;

        if (!isOwn && !isManager) {
            throw new UnauthorizedException("You cannot view this share");
        }

        return shareMapper.toResponse(share);
    }

    @Override
    public List<ShareResponse> getMyShares(UUID userId) {
        return shareRepository.findAllByUserIdWithDetails(userId).stream()
                .map(shareMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShareResponse> getMySharesInGroup(UUID userId, UUID groupId) {
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        return shareRepository.findAllByMemberIdWithDetails(member.getId()).stream()
                .map(shareMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShareResponse> getGroupShares(UUID groupId, UUID requesterId) {
        verifyManagerAccess(groupId, requesterId);

        return shareRepository.findAllByGroupIdWithDetails(groupId).stream()
                .map(shareMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ShareSummaryResponse getMySummary(UUID userId, UUID groupId) {
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        List<Share> myShares = shareRepository.findAllByMemberIdWithDetails(member.getId());
        int totalShares = myShares.stream().mapToInt(Share::getNumberOfShares).sum();

        BigDecimal price = member.getGroup().getSharePrice() != null
                ? member.getGroup().getSharePrice()
                : new BigDecimal("100.00");

        return ShareSummaryResponse.builder()
                .userId(userId)
                .groupId(groupId)
                .groupName(member.getGroup().getName())
                .totalShares(totalShares)
                .currentSharePrice(price)
                .totalValue(price.multiply(BigDecimal.valueOf(totalShares)))
                .totalTransactions(myShares.size())
                .build();
    }

    @Override
    public ShareSummaryResponse getGroupSummary(UUID groupId, UUID requesterId) {
        verifyManagerAccess(groupId, requesterId);

        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        List<GroupMember> allMembers = groupMemberRepository.findByGroupIdAndIsActiveTrue(groupId);
        int totalShares = allMembers.stream()
                .mapToInt(m -> m.getShareBalance().intValue())
                .sum();

        BigDecimal price = member.getGroup().getSharePrice() != null
                ? member.getGroup().getSharePrice()
                : new BigDecimal("100.00");

        long totalTx = shareRepository.findAllByGroupIdWithDetails(groupId).size();

        return ShareSummaryResponse.builder()
                .userId(requesterId)
                .groupId(groupId)
                .groupName(member.getGroup().getName())
                .totalShares(totalShares)
                .currentSharePrice(price)
                .totalValue(price.multiply(BigDecimal.valueOf(totalShares)))
                .totalTransactions(totalTx)
                .build();
    }

    private void verifyManagerAccess(UUID groupId, UUID requesterId) {
        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(groupId, requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.TREASURER
                && requester.getRole() != GroupRole.CHAIRPERSON
                && requester.getRole() != GroupRole.SECRETARY) {
            throw new UnauthorizedException("Only managers can perform this action");
        }
    }
}