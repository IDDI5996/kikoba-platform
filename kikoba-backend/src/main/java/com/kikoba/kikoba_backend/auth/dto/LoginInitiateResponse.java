package com.kikoba.kikoba_backend.auth.dto;

import com.kikoba.kikoba_backend.user.dto.UserResponse;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginInitiateResponse {

    /** True if the user has MFA enabled and must complete a second step. */
    private boolean mfaRequired;

    // ---- Populated when mfaRequired = FALSE (login is complete) ----
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserResponse user;

    // ---- Populated when mfaRequired = TRUE ----
    private String sessionToken;   // short-lived token for the MFA step
    private String maskedPhone;    // e.g. "+25***78" so the user knows where to expect the code
    private String mfaMethod;      // "SMS" | "TOTP"
}