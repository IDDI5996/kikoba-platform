package com.kikoba.kikoba_backend.share.controller;

import com.kikoba.kikoba_backend.share.dto.PurchaseSharesRequest;
import com.kikoba.kikoba_backend.share.dto.ShareResponse;
import com.kikoba.kikoba_backend.share.dto.ShareSummaryResponse;
import com.kikoba.kikoba_backend.share.service.ShareService;
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
public class ShareController {

    private final ShareService shareService;

    /**
     * Purchase shares in a group
     * POST /api/groups/{groupId}/shares
     */
    @PostMapping("/groups/{groupId}/shares")
    public ResponseEntity<ShareResponse> purchaseShares(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId,
            @Valid @RequestBody PurchaseSharesRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shareService.purchaseShares(userId, groupId, request));
    }

    /**
     * Get my shares across all groups
     * GET /api/shares/my
     */
    @GetMapping("/shares/my")
    public ResponseEntity<List<ShareResponse>> getMyShares(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(shareService.getMyShares(userId));
    }

    /**
     * Get my shares in a group
     * GET /api/groups/{groupId}/shares/my
     */
    @GetMapping("/groups/{groupId}/shares/my")
    public ResponseEntity<List<ShareResponse>> getMySharesInGroup(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(shareService.getMySharesInGroup(userId, groupId));
    }

    /**
     * Get all shares in a group (Manager)
     * GET /api/groups/{groupId}/shares
     */
    @GetMapping("/groups/{groupId}/shares")
    public ResponseEntity<List<ShareResponse>> getGroupShares(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(shareService.getGroupShares(groupId, userId));
    }

    /**
     * Get a single share
     * GET /api/shares/{id}
     */
    @GetMapping("/shares/{id}")
    public ResponseEntity<ShareResponse> getShareById(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(shareService.getShareById(id, userId));
    }

    /**
     * Get my share summary in a group
     * GET /api/groups/{groupId}/shares/summary/me
     */
    @GetMapping("/groups/{groupId}/shares/summary/me")
    public ResponseEntity<ShareSummaryResponse> getMySummary(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(shareService.getMySummary(userId, groupId));
    }

    /**
     * Get group-level share summary (Manager)
     * GET /api/groups/{groupId}/shares/summary
     */
    @GetMapping("/groups/{groupId}/shares/summary")
    public ResponseEntity<ShareSummaryResponse> getGroupSummary(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(shareService.getGroupSummary(groupId, userId));
    }
}