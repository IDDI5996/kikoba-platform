package com.kikoba.kikoba_backend.governance.mapper;

import com.kikoba.kikoba_backend.governance.dto.PollResponse;
import com.kikoba.kikoba_backend.governance.entity.Poll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PollMapper {

    public PollResponse toResponse(Poll poll, long totalVotes,
                                   Map<String, Long> results,
                                   boolean hasVoted) {
        if (poll == null) return null;

        String createdByName = null;
        if (poll.getCreatedBy() != null) {
            createdByName = poll.getCreatedBy().getFirstName() + " "
                    + poll.getCreatedBy().getLastName();
        }

        List<String> options = parseOptions(poll.getOptions());

        return PollResponse.builder()
                .id(poll.getId())
                .groupId(poll.getGroup().getId())
                .groupName(poll.getGroup().getName())
                .title(poll.getTitle())
                .description(poll.getDescription())
                .options(options)
                .status(poll.getStatus())
                .isAnonymous(poll.getIsAnonymous())
                .allowMultipleChoices(poll.getAllowMultipleChoices())
                .closesAt(poll.getClosesAt())
                .createdBy(poll.getCreatedBy() != null ? poll.getCreatedBy().getId() : null)
                .createdByName(createdByName)
                .closedAt(poll.getClosedAt())
                .totalVotes(totalVotes)
                .results(results)
                .hasVoted(hasVoted)
                .createdAt(poll.getCreatedAt())
                .build();
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

    /**
     * Serialize options to a pipe-separated string.
     * Example: ["Yes", "No", "Abstain"] → "Yes|No|Abstain"
     *
     * Public static so it can be reused in the service layer.
     */
    public static String serializeOptions(List<String> options) {
        if (options == null || options.isEmpty()) {
            return "";
        }
        return String.join("|", options);
    }
}