package com.kikoba.kikoba_backend.governance.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.UUID;

@Component
public class JitsiRoomGenerator {

    private static final String JITSI_BASE_URL = "https://meet.jit.si/";
    private static final String ROOM_PREFIX = "KikobaConnect-";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a unique Jitsi room ID.
     * Format: KikobaConnect-{groupShortId}-{randomSuffix}
     * Example: KikobaConnect-a1b2c3d4-x9y7
     */
    public String generateRoomId(UUID groupId) {
        String groupShort = groupId.toString().substring(0, 8);
        String random = Integer.toHexString(RANDOM.nextInt(0xFFFF));
        return ROOM_PREFIX + groupShort + "-" + random;
    }

    public String generateRoomUrl(String roomId) {
        return JITSI_BASE_URL + roomId;
    }
}