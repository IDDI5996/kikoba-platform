package com.kikoba.kikoba_backend.share.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseSharesRequest {

    @NotNull(message = "Number of shares is required")
    @Min(value = 1, message = "Must purchase at least 1 share")
    @Max(value = 10000, message = "Cannot purchase more than 10,000 shares at once")
    private Integer numberOfShares;

    @Size(max = 100, message = "Reference cannot exceed 100 characters")
    private String transactionReference;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}