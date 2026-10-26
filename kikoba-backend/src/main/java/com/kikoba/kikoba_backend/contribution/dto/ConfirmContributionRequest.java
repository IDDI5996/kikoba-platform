package com.kikoba.kikoba_backend.contribution.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmContributionRequest {

    @Size(max = 100, message = "Transaction reference cannot exceed 100 characters")
    private String transactionReference;

    @Size(max = 500)
    private String notes;
}