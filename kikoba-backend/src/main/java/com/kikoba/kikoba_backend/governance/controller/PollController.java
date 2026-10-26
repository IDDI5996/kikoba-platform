package com.kikoba.kikoba_backend.governance.controller;

import com.kikoba.kikoba_backend.governance.dto.CastVoteRequest;
import com.kikoba.kikoba_backend.governance.dto.CreatePollRequest;
import com.kikoba.kikoba_backend.governance.dto.PollResponse;
import com.kikoba.kikoba_backend.governance.dto.VoteResponse;
import com.kikoba.kikoba_backend.governance.service.PollService;
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
public class PollController {

    private final PollService pollService;

    /**
     * Create a new poll
     * POST /api/groups/{groupId}/polls
     */
    @PostMapping("/groups/{groupId}/polls")
    public ResponseEntity<PollResponse> createPoll(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId,
            @Valid @RequestBody CreatePollRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pollService.createPoll(userId, groupId, request));
    }

    /**
     * Close a poll
     * PUT /api/polls/{id}/close
     */
    @PutMapping("/polls/{id}/close")
    public ResponseEntity<PollResponse> closePoll(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(pollService.closePoll(id, userId));
    }

    /**
     * Cancel a poll
     * PUT /api/polls/{id}/cancel
     */
    @PutMapping("/polls/{id}/cancel")
    public ResponseEntity<PollResponse> cancelPoll(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(pollService.cancelPoll(id, userId));
    }

    /**
     * Get a poll by ID
     * GET /api/polls/{id}
     */
    @GetMapping("/polls/{id}")
    public ResponseEntity<PollResponse> getPollById(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(pollService.getPollById(id, userId));
    }

    /**
     * Get all group polls
     * GET /api/groups/{groupId}/polls
     */
    @GetMapping("/groups/{groupId}/polls")
    public ResponseEntity<List<PollResponse>> getGroupPolls(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(pollService.getGroupPolls(groupId, userId));
    }

    /**
     * Get active group polls
     * GET /api/groups/{groupId}/polls/active
     */
    @GetMapping("/groups/{groupId}/polls/active")
    public ResponseEntity<List<PollResponse>> getActiveGroupPolls(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(pollService.getActiveGroupPolls(groupId, userId));
    }

    /**
     * Cast a vote
     * POST /api/polls/{id}/vote
     */
    @PostMapping("/polls/{id}/vote")
    public ResponseEntity<VoteResponse> castVote(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody CastVoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pollService.castVote(id, userId, request));
    }

    /**
     * Get my votes on a poll
     * GET /api/polls/{id}/my-votes
     */
    @GetMapping("/polls/{id}/my-votes")
    public ResponseEntity<List<VoteResponse>> getMyVotes(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(pollService.getMyVotes(id, userId));
    }
}