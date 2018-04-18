package org.lytsiware.clash.utils;

import org.lytsiware.clash.ZoneIdConfiguration;

import java.time.ZonedDateTime;

public class TestableLocalDateTime {

    public static ZonedDateTime getZonedDateTimeNow() {
        return ZonedDateTime.now(ZoneIdConfiguration.zoneId());
    }

}
