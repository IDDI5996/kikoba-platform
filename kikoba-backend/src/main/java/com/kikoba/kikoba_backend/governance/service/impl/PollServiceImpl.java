package com.kikoba.kikoba_backend.governance.service.impl;

import com.kikoba.kikoba_backend.exception.BadRequestException;
import com.kikoba.kikoba_backend.exception.ResourceAlreadyExistsException;
import com.kikoba.kikoba_backend.exception.ResourceNotFoundException;
import com.kikoba.kikoba_backend.exception.UnauthorizedException;
import com.kikoba.kikoba_backend.governance.dto.CastVoteRequest;
import com.kikoba.kikoba_backend.governance.dto.CreatePollRequest;
import com.kikoba.kikoba_backend.governance.dto.PollResponse;
import com.kikoba.kikoba_backend.governance.dto.VoteResponse;
import com.kikoba.kikoba_backend.governance.entity.Poll;
import com.kikoba.kikoba_backend.governance.entity.Vote;
import com.kikoba.kikoba_backend.governance.enums.PollStatus;
import com.kikoba.kikoba_backend.governance.mapper.PollMapper;
import com.kikoba.kikoba_backend.governance.repository.PollRepository;
import com.kikoba.kikoba_backend.governance.repository.VoteRepository;
import com.kikoba.kikoba_backend.governance.service.PollService;
import com.kikoba.kikoba_backend.group.entity.Group;
import com.kikoba.kikoba_backend.group.entity.GroupMember;
import com.kikoba.kikoba_backend.group.enums.GroupRole;
import com.kikoba.kikoba_backend.group.repository.GroupMemberRepository;
import com.kikoba.kikoba_backend.notification.enums.NotificationPriority;
import com.kikoba.kikoba_backend.notification.enums.NotificationType;
import com.kikoba.kikoba_backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PollServiceImpl implements PollService {

    private final PollRepository pollRepository;
    private final VoteRepository voteRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final PollMapper pollMapper;
    private final NotificationService notificationService;

    // ============================================
    // CREATE POLL
    // ============================================
    @Override
    @Transactional
    public PollResponse createPoll(UUID userId, UUID groupId, CreatePollRequest request) {
        log.info("User {} creating poll in group {}", userId, groupId);

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.CHAIRPERSON && requester.getRole() != GroupRole.SECRETARY) {
            throw new UnauthorizedException("Only Chairperson or Secretary can create polls");
        }

        // Deduplicate options
        List<String> cleanedOptions = request.getOptions().stream()
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());

        if (cleanedOptions.size() < 2) {
            throw new BadRequestException("Poll must have at least 2 unique options");
        }

        // Serialize options to pipe-separated string
        String optionsSerialized = PollMapper.serializeOptions(cleanedOptions);

        Group group = requester.getGroup();

        Poll poll = Poll.builder()
                .group(group)
                .title(request.getTitle())
                .description(request.getDescription())
                .options(optionsSerialized)
                .status(PollStatus.ACTIVE)
                .isAnonymous(request.getIsAnonymous())
                .allowMultipleChoices(request.getAllowMultipleChoices())
                .closesAt(request.getClosesAt())
                .createdBy(requester.getUser())
                .build();

        Poll saved = pollRepository.save(poll);

        log.info("Poll {} created in group {}", saved.getId(), groupId);

        // Notify all group members about the new poll
        List<GroupMember> members = groupMemberRepository.findByGroupIdAndIsActiveTrue(groupId);
        for (GroupMember member : members) {
            notificationService.notifyUserAsync(
                    member.getUser().getId(),
                    NotificationType.POLL_CREATED,
                    "New Poll: " + saved.getTitle(),
                    "A new poll has been created. Cast your vote today.",
                    "/polls/" + saved.getId(),
                    "Poll",
                    saved.getId(),
                    NotificationPriority.NORMAL
            );
        }

        return pollMapper.toResponse(saved, 0, Collections.emptyMap(), false);
    }

    // ============================================
    // CLOSE POLL
    // ============================================
    @Override
    @Transactional
    public PollResponse closePoll(UUID pollId, UUID requesterId) {
        Poll poll = pollRepository.findByIdWithDetails(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found"));

        Group group = poll.getGroup();

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.CHAIRPERSON && requester.getRole() != GroupRole.SECRETARY) {
            throw new UnauthorizedException("Only Chairperson or Secretary can close polls");
        }

        if (poll.getStatus() != PollStatus.ACTIVE) {
            throw new BadRequestException("Only ACTIVE polls can be closed. Current: " + poll.getStatus());
        }

        poll.setStatus(PollStatus.CLOSED);
        poll.setClosedAt(LocalDateTime.now());

        Poll saved = pollRepository.save(poll);

        Map<String, Long> results = calculateResults(pollId);
        long totalVotes = voteRepository.countByPollId(pollId);

        List<GroupMember> members = groupMemberRepository.findByGroupIdAndIsActiveTrue(group.getId());
        for (GroupMember member : members) {
            notificationService.notifyUserAsync(
                    member.getUser().getId(),
                    NotificationType.POLL_CLOSED,
                    "Poll Closed: " + poll.getTitle(),
                    "The poll has closed. View the results.",
                    "/polls/" + poll.getId(),
                    "Poll",
                    poll.getId(),
                    NotificationPriority.NORMAL
            );
        }

        return pollMapper.toResponse(saved, totalVotes, results, false);
    }

    // ============================================
    // CANCEL POLL
    // ============================================
    @Override
    @Transactional
    public PollResponse cancelPoll(UUID pollId, UUID requesterId) {
        Poll poll = pollRepository.findByIdWithDetails(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found"));

        Group group = poll.getGroup();

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(group.getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (requester.getRole() != GroupRole.CHAIRPERSON) {
            throw new UnauthorizedException("Only Chairperson can cancel polls");
        }

        if (poll.getStatus() == PollStatus.CLOSED) {
            throw new BadRequestException("Cannot cancel a closed poll");
        }

        poll.setStatus(PollStatus.CANCELLED);
        poll.setClosedAt(LocalDateTime.now());

        Poll saved = pollRepository.save(poll);
        return pollMapper.toResponse(saved, 0, Collections.emptyMap(), false);
    }

    // ============================================
    // QUERY
    // ============================================
    @Override
    public PollResponse getPollById(UUID pollId, UUID requesterId) {
        Poll poll = pollRepository.findByIdWithDetails(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found"));

        GroupMember requester = groupMemberRepository
                .findByGroupIdAndUserId(poll.getGroup().getId(), requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        Map<String, Long> results = calculateResults(pollId);
        long totalVotes = voteRepository.countByPollId(pollId);
        boolean hasVoted = voteRepository.existsByPollIdAndGroupMemberId(pollId, requester.getId());

        boolean isManager = requester.getRole() != GroupRole.MEMBER;
        Map<String, Long> visibleResults = (isManager || poll.getStatus() == PollStatus.CLOSED)
                ? results
                : Collections.emptyMap();

        return pollMapper.toResponse(poll, totalVotes, visibleResults, hasVoted);
    }

    @Override
    public List<PollResponse> getGroupPolls(UUID groupId, UUID requesterId) {
        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(groupId, requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        boolean isManager = requester.getRole() != GroupRole.MEMBER;

        return pollRepository.findAllByGroupIdWithDetails(groupId).stream()
                .map(p -> {
                    Map<String, Long> results = calculateResults(p.getId());
                    long totalVotes = voteRepository.countByPollId(p.getId());
                    boolean hasVoted = voteRepository.existsByPollIdAndGroupMemberId(p.getId(), requester.getId());

                    Map<String, Long> visibleResults = (isManager || p.getStatus() == PollStatus.CLOSED)
                            ? results
                            : Collections.emptyMap();

                    return pollMapper.toResponse(p, totalVotes, visibleResults, hasVoted);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PollResponse> getActiveGroupPolls(UUID groupId, UUID requesterId) {
        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(groupId, requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        boolean isManager = requester.getRole() != GroupRole.MEMBER;

        return pollRepository.findByGroupIdAndStatusOrderByCreatedAtDesc(groupId, PollStatus.ACTIVE).stream()
                .map(p -> {
                    Map<String, Long> results = calculateResults(p.getId());
                    long totalVotes = voteRepository.countByPollId(p.getId());
                    boolean hasVoted = voteRepository.existsByPollIdAndGroupMemberId(p.getId(), requester.getId());

                    Map<String, Long> visibleResults = isManager ? results : Collections.emptyMap();

                    return pollMapper.toResponse(p, totalVotes, visibleResults, hasVoted);
                })
                .collect(Collectors.toList());
    }

    // ============================================
    // CAST VOTE
    // ============================================
    @Override
    @Transactional
    public VoteResponse castVote(UUID pollId, UUID userId, CastVoteRequest request) {
        log.info("User {} voting on poll {}", userId, pollId);

        Poll poll = pollRepository.findByIdWithDetails(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found"));

        Group group = poll.getGroup();

        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(group.getId(), userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (poll.getStatus() != PollStatus.ACTIVE) {
            throw new BadRequestException("Poll is not active. Current: " + poll.getStatus());
        }

        if (poll.getClosesAt() != null && LocalDateTime.now().isAfter(poll.getClosesAt())) {
            throw new BadRequestException("Poll has already closed");
        }

        // Validate choice is a valid option
        List<String> options = parseOptions(poll.getOptions());
        if (!options.contains(request.getChoice())) {
            throw new BadRequestException("Invalid choice. Valid options: " + options);
        }

        // For single-choice polls, prevent double voting
        if (!poll.getAllowMultipleChoices()) {
            boolean hasVoted = voteRepository.existsByPollIdAndGroupMemberId(pollId, member.getId());
            if (hasVoted) {
                throw new ResourceAlreadyExistsException("You have already voted on this poll");
            }
        } else {
            if (voteRepository.findByPollIdAndGroupMemberIdAndChoice(
                    pollId, member.getId(), request.getChoice()).isPresent()) {
                throw new ResourceAlreadyExistsException("You already voted for this option");
            }
        }

        Vote vote = Vote.builder()
                .poll(poll)
                .groupMember(member)
                .choice(request.getChoice())
                .build();

        Vote saved = voteRepository.save(vote);

        VoteResponse response = VoteResponse.builder()
                .id(saved.getId())
                .pollId(pollId)
                .memberId(member.getId())
                .memberName(member.getUser().getFirstName() + " " + member.getUser().getLastName())
                .choice(request.getChoice())
                .createdAt(saved.getCreatedAt())
                .build();

        if (Boolean.TRUE.equals(poll.getIsAnonymous())) {
            response.setMemberName("Anonymous");
        }

        log.info("Vote cast on poll {} by member {}", pollId, member.getId());

        return response;
    }

    @Override
    public List<VoteResponse> getMyVotes(UUID pollId, UUID userId) {
        Poll poll = pollRepository.findByIdWithDetails(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found"));

        GroupMember member = groupMemberRepository
                .findByGroupIdAndUserId(poll.getGroup().getId(), userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        return voteRepository.findByPollIdAndGroupMemberId(pollId, member.getId()).stream()
                .map(v -> VoteResponse.builder()
                        .id(v.getId())
                        .pollId(pollId)
                        .memberId(member.getId())
                        .memberName(member.getUser().getFirstName() + " " + member.getUser().getLastName())
                        .choice(v.getChoice())
                        .createdAt(v.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // ============================================
    // HELPERS
    // ============================================
    private Map<String, Long> calculateResults(UUID pollId) {
        List<Object[]> rows = voteRepository.countVotesByChoice(pollId);
        Map<String, Long> results = new LinkedHashMap<>();
        for (Object[] row : rows) {
            results.put((String) row[0], ((Number) row[1]).longValue());
        }
        return results;
    }

    /**
     * Parse options from a pipe-separated string.
     * Example: "Yes|No|Abstain" → ["Yes", "No", "Abstain"]
     */
    private List<String> parseOptions(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(raw.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}