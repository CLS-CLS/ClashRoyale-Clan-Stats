package org.lytsiware.clash.domain;

import org.lytsiware.clash.ZoneIdConfiguration;

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
        return new Timestamp(attribute.toEpochSecond(ZoneOffset.UTC));
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Timestamp dbData) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(dbData.getTime()), ZoneIdConfiguration.zoneId());
    }
}
