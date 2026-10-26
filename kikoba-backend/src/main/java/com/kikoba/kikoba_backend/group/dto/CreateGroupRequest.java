package com.kikoba.kikoba_backend.group.dto;

import com.kikoba.kikoba_backend.group.enums.GroupType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateGroupRequest {

    @NotBlank(message = "Group name is required")
    @Size(min = 3, max = 255, message = "Group name must be 3-255 characters")
    private String name;

    @NotNull(message = "Group type is required")
    private GroupType type;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Pattern(regexp = "^(WEEKLY|MONTHLY)$", message = "Meeting frequency must be WEEKLY or MONTHLY")
    private String meetingFrequency;

    private String meetingDay;

    private LocalTime meetingTime;

    @PositiveOrZero(message = "Minimum contribution must be positive or zero")
    private BigDecimal minContribution;

    @PositiveOrZero(message = "Maximum contribution must be positive or zero")
    private BigDecimal maxContribution;

    @PositiveOrZero(message = "Interest rate must be positive or zero")
    @DecimalMax(value = "100.0", message = "Interest rate cannot exceed 100%")
    private BigDecimal interestRate;

    @Positive(message = "Cycle duration must be positive")
    private Integer cycleDurationMonths;

    @Size(max = 3)
    private String currency;
}