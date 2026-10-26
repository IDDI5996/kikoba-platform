package com.kikoba.kikoba_backend.contribution.service;

import com.kikoba.kikoba_backend.contribution.dto.*;

import java.util.List;
import java.util.UUID;

public interface ContributionService {

    ContributionResponse makeContribution(UUID userId, UUID groupId, MakeContributionRequest request);

    ContributionResponse confirmContribution(UUID contributionId, UUID requesterId, ConfirmContributionRequest request);

    ContributionResponse cancelContribution(UUID contributionId, UUID requesterId);

    ContributionResponse getContributionById(UUID contributionId, UUID requesterId);

    List<ContributionResponse> getMyContributions(UUID userId);

    List<ContributionResponse> getMyContributionsInGroup(UUID userId, UUID groupId);

    List<ContributionResponse> getGroupContributions(UUID groupId, UUID requesterId);

    List<ContributionResponse> getPendingGroupContributions(UUID groupId, UUID requesterId);

    ContributionSummaryResponse getMySummary(UUID userId, UUID groupId);

    ContributionSummaryResponse getGroupSummary(UUID groupId, UUID requesterId);
}