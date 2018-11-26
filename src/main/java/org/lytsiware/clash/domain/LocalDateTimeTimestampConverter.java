package org.lytsiware.clash.domain;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Converter(autoApply = true)
public class LocalDateTimeTimestampConverter implements AttributeConverter<LocalDateTime, Timestamp> {


    @Override
    public Timestamp convertToDatabaseColumn(LocalDateTime attribute) {
        if (attribute == null) {
            return null;
        }
        return new Timestamp(attribute.toEpochSecond(ZoneOffset.UTC));
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Timestamp dbData) {
        if (dbData == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(dbData.getTime()), ZoneOffset.UTC);
    }
}
