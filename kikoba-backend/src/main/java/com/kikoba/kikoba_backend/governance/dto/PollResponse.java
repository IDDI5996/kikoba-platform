package com.kikoba.kikoba_backend.governance.dto;

import com.kikoba.kikoba_backend.governance.enums.PollStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollResponse {
    private UUID id;
    private UUID groupId;
    private String groupName;
    private String title;
    private String description;
    private List<String> options;
    private PollStatus status;
    private Boolean isAnonymous;
    private Boolean allowMultipleChoices;
    private LocalDateTime closesAt;
    private UUID createdBy;
    private String createdByName;
    private LocalDateTime closedAt;
    private long totalVotes;
    private Map<String, Long> results;
    private Boolean hasVoted;
    private LocalDateTime createdAt;
}