package com.kikoba.kikoba_backend.contribution.controller;

import com.kikoba.kikoba_backend.contribution.dto.*;
import com.kikoba.kikoba_backend.contribution.service.ContributionService;
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
public class ContributionController {

    private final ContributionService contributionService;

    /**
     * Make a contribution to a group
     * POST /api/groups/{groupId}/contributions
     */
    @PostMapping("/groups/{groupId}/contributions")
    public ResponseEntity<ContributionResponse> makeContribution(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId,
            @Valid @RequestBody MakeContributionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contributionService.makeContribution(userId, groupId, request));
    }

    /**
     * Get my contributions across all groups
     * GET /api/contributions/my
     */
    @GetMapping("/contributions/my")
    public ResponseEntity<List<ContributionResponse>> getMyContributions(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(contributionService.getMyContributions(userId));
    }

    /**
     * Get my contributions in a specific group
     * GET /api/groups/{groupId}/contributions/my
     */
    @GetMapping("/groups/{groupId}/contributions/my")
    public ResponseEntity<List<ContributionResponse>> getMyContributionsInGroup(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(contributionService.getMyContributionsInGroup(userId, groupId));
    }

    /**
     * Get all contributions in a group (Treasurer/Chairperson/Secretary)
     * GET /api/groups/{groupId}/contributions
     */
    @GetMapping("/groups/{groupId}/contributions")
    public ResponseEntity<List<ContributionResponse>> getGroupContributions(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(contributionService.getGroupContributions(groupId, userId));
    }

    /**
     * Get pending contributions in a group (Treasurer/Chairperson/Secretary)
     * GET /api/groups/{groupId}/contributions/pending
     */
    @GetMapping("/groups/{groupId}/contributions/pending")
    public ResponseEntity<List<ContributionResponse>> getPendingContributions(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(contributionService.getPendingGroupContributions(groupId, userId));
    }

    /**
     * Get a single contribution
     * GET /api/contributions/{id}
     */
    @GetMapping("/contributions/{id}")
    public ResponseEntity<ContributionResponse> getContributionById(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(contributionService.getContributionById(id, userId));
    }

    /**
     * Confirm a pending contribution (Treasurer/Chairperson)
     * PUT /api/contributions/{id}/confirm
     */
    @PutMapping("/contributions/{id}/confirm")
    public ResponseEntity<ContributionResponse> confirmContribution(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @RequestBody(required = false) ConfirmContributionRequest request) {
        return ResponseEntity.ok(contributionService.confirmContribution(id, userId, request));
    }

    /**
     * Cancel a contribution (Contributor or Manager)
     * DELETE /api/contributions/{id}
     */
    @DeleteMapping("/contributions/{id}")
    public ResponseEntity<ContributionResponse> cancelContribution(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(contributionService.cancelContribution(id, userId));
    }

    /**
     * Get my summary in a group
     * GET /api/groups/{groupId}/contributions/summary/me
     */
    @GetMapping("/groups/{groupId}/contributions/summary/me")
    public ResponseEntity<ContributionSummaryResponse> getMySummary(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(contributionService.getMySummary(userId, groupId));
    }

    /**
     * Get group-level summary (Treasurer/Chairperson)
     * GET /api/groups/{groupId}/contributions/summary
     */
    @GetMapping("/groups/{groupId}/contributions/summary")
    public ResponseEntity<ContributionSummaryResponse> getGroupSummary(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(contributionService.getGroupSummary(groupId, userId));
    }
}