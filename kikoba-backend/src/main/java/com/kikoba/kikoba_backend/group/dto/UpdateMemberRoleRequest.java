package com.kikoba.kikoba_backend.group.dto;

import com.kikoba.kikoba_backend.group.enums.GroupRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMemberRoleRequest {

    @NotNull(message = "Role is required")
    private GroupRole role;

    @NotBlank(message = "Reason is required for role changes")
    @Size(min = 10, max = 500, message = "Reason must be 10-500 characters")
    private String reason;
}