package com.kikoba.kikoba_backend.dividend.controller;

import com.kikoba.kikoba_backend.dividend.dto.CalculateDividendRequest;
import com.kikoba.kikoba_backend.dividend.dto.DividendPayoutResponse;
import com.kikoba.kikoba_backend.dividend.dto.DividendResponse;
import com.kikoba.kikoba_backend.dividend.service.DividendService;
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
public class DividendController {

    private final DividendService dividendService;

    /**
     * Calculate a new dividend for a cycle
     * POST /api/groups/{groupId}/dividends/calculate
     */
    @PostMapping("/groups/{groupId}/dividends/calculate")
    public ResponseEntity<DividendResponse> calculateDividend(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId,
            @Valid @RequestBody CalculateDividendRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dividendService.calculateDividend(groupId, userId, request));
    }

    /**
     * Approve a calculated dividend
     * PUT /api/dividends/{id}/approve
     */
    @PutMapping("/dividends/{id}/approve")
    public ResponseEntity<DividendResponse> approveDividend(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(dividendService.approveDividend(id, userId));
    }

    /**
     * Distribute an approved dividend (generates payouts for all members)
     * PUT /api/dividends/{id}/distribute
     */
    @PutMapping("/dividends/{id}/distribute")
    public ResponseEntity<DividendResponse> distributeDividend(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(dividendService.distributeDividend(id, userId));
    }

    /**
     * Cancel a dividend (before distribution)
     * DELETE /api/dividends/{id}
     */
    @DeleteMapping("/dividends/{id}")
    public ResponseEntity<DividendResponse> cancelDividend(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @RequestParam String reason) {
        return ResponseEntity.ok(dividendService.cancelDividend(id, userId, reason));
    }

    /**
     * Get a single dividend
     * GET /api/dividends/{id}
     */
    @GetMapping("/dividends/{id}")
    public ResponseEntity<DividendResponse> getDividendById(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(dividendService.getDividendById(id, userId));
    }

    /**
     * Get all dividends for a group
     * GET /api/groups/{groupId}/dividends
     */
    @GetMapping("/groups/{groupId}/dividends")
    public ResponseEntity<List<DividendResponse>> getGroupDividends(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(dividendService.getGroupDividends(groupId, userId));
    }

    /**
     * Get all payouts for a dividend (Manager)
     * GET /api/dividends/{id}/payouts
     */
    @GetMapping("/dividends/{id}/payouts")
    public ResponseEntity<List<DividendPayoutResponse>> getDividendPayouts(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(dividendService.getDividendPayouts(id, userId));
    }

    /**
     * Get my dividend payouts across all groups
     * GET /api/dividends/my
     */
    @GetMapping("/dividends/my")
    public ResponseEntity<List<DividendPayoutResponse>> getMyDividends(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(dividendService.getMyDividends(userId));
    }

    /**
     * Confirm a payout was received (Treasurer/Chairperson)
     * PUT /api/dividend-payouts/{id}/confirm
     */
    @PutMapping("/dividend-payouts/{id}/confirm")
    public ResponseEntity<DividendPayoutResponse> confirmPayout(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @RequestParam(required = false) String reference) {
        return ResponseEntity.ok(dividendService.confirmPayout(id, userId, reference));
    }
}