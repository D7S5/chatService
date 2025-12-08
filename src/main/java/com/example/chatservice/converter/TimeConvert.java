package com.example.chatservice.converter;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class TimeConvert {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public static OffsetDateTime fromEpochMilliToKst(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis)
                .atZone(KST)
                .toOffsetDateTime();
    }
}
