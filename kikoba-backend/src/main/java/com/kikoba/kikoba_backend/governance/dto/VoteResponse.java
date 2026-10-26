package com.kikoba.kikoba_backend.governance.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteResponse {
    private UUID id;
    private UUID pollId;
    private UUID memberId;
    private String memberName;
    private String choice;
    private LocalDateTime createdAt;
}