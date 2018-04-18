package org.lytsiware.clash;

import org.springframework.context.annotation.Profile;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZonedDateTime;

/**
 * H2 database does not support {@link ZonedDateTime}, so we need to manually convert it to timestamp
 */
@Converter(autoApply = true)
@Profile("!postgres")
public class ZonedDateTimeTimestampConverter implements AttributeConverter<ZonedDateTime, Timestamp> {


    @Override
    public Timestamp convertToDatabaseColumn(ZonedDateTime attribute) {
        return new Timestamp(attribute.toEpochSecond());
    }

    @Override
    public ZonedDateTime convertToEntityAttribute(Timestamp dbData) {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(dbData.getTime()), ZoneIdConfiguration.zoneId());
    }
}
