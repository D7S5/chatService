package com.example.chatService.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Converter(autoApply = true)
public class OffsetDateTimeKstConverter implements AttributeConverter<OffsetDateTime, LocalDateTime> {

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");
    @Override
    public LocalDateTime convertToDatabaseColumn(OffsetDateTime attribute) {
        if (attribute == null) return null;

        // OffsetDateTime → KST LocalDateTime 변환 (DB 저장용)
        return attribute.atZoneSameInstant(KST_ZONE).toLocalDateTime();
    }

    @Override
    public OffsetDateTime convertToEntityAttribute(LocalDateTime dbData) {
        if (dbData == null) return null;

        // DB LocalDateTime(KST) → OffsetDateTime(+09:00) 변환 (Entity용)
        return dbData.atZone(KST_ZONE).toOffsetDateTime();
    }
}
