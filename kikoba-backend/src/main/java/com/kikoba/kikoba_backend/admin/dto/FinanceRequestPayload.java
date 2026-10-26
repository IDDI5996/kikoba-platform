package com.kikoba.kikoba_backend.admin.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FinanceRequestPayload {
    private String targetUser; // Phone number
    private BigDecimal amount;
    private String reason;
}