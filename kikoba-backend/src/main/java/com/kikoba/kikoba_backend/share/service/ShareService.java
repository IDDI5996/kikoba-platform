package com.kikoba.kikoba_backend.share.service;

import com.kikoba.kikoba_backend.share.dto.PurchaseSharesRequest;
import com.kikoba.kikoba_backend.share.dto.ShareResponse;
import com.kikoba.kikoba_backend.share.dto.ShareSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface ShareService {
    ShareResponse purchaseShares(UUID userId, UUID groupId, PurchaseSharesRequest request);
    ShareResponse getShareById(UUID shareId, UUID requesterId);
    List<ShareResponse> getMyShares(UUID userId);
    List<ShareResponse> getMySharesInGroup(UUID userId, UUID groupId);
    List<ShareResponse> getGroupShares(UUID groupId, UUID requesterId);
    ShareSummaryResponse getMySummary(UUID userId, UUID groupId);
    ShareSummaryResponse getGroupSummary(UUID groupId, UUID requesterId);
}