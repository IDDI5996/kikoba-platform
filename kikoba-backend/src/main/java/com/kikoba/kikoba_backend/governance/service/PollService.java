package com.kikoba.kikoba_backend.governance.service;

import com.kikoba.kikoba_backend.governance.dto.CastVoteRequest;
import com.kikoba.kikoba_backend.governance.dto.CreatePollRequest;
import com.kikoba.kikoba_backend.governance.dto.PollResponse;
import com.kikoba.kikoba_backend.governance.dto.VoteResponse;

import java.util.List;
import java.util.UUID;

public interface PollService {

    PollResponse createPoll(UUID userId, UUID groupId, CreatePollRequest request);
    PollResponse closePoll(UUID pollId, UUID requesterId);
    PollResponse cancelPoll(UUID pollId, UUID requesterId);

    PollResponse getPollById(UUID pollId, UUID requesterId);
    List<PollResponse> getGroupPolls(UUID groupId, UUID requesterId);
    List<PollResponse> getActiveGroupPolls(UUID groupId, UUID requesterId);

    VoteResponse castVote(UUID pollId, UUID userId, CastVoteRequest request);
    List<VoteResponse> getMyVotes(UUID pollId, UUID userId);
}