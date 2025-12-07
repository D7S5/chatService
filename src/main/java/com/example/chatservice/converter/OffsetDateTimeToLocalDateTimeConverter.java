package com.example.chatservice.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Converter(autoApply = true)
public class OffsetDateTimeToLocalDateTimeConverter implements AttributeConverter<OffsetDateTime, LocalDateTime> {
    @Override
    public LocalDateTime convertToDatabaseColumn(OffsetDateTime attribute) {
        if (attribute == null) return null;
        return LocalDateTime.ofInstant(attribute.toInstant(), ZoneOffset.UTC);
    }

    @Override
    public OffsetDateTime convertToEntityAttribute(LocalDateTime dbData) {
        if (dbData == null) return null;
        return OffsetDateTime.of(dbData, ZoneOffset.UTC);
    }
}
