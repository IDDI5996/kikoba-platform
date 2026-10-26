package com.kikoba.kikoba_backend.loan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRejectionRequest {

    @NotBlank(message = "Rejection reason is required")
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;
}