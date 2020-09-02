package org.lytsiware.clash.service.integration.statsroyale;

import org.junit.Test;
import org.lytsiware.clash.service.war.integration.statsroyale.StatsRoyaleDateParse;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class StatsRoyaleDateParseTest {


    private final StatsRoyaleDateParse statsRoyaleDateParse = new StatsRoyaleDateParse();

    @Test
    public void testDateParse() {
        LocalDateTime result = statsRoyaleDateParse.parseDescriptiveDate("2 days 23 hours ago", LocalDateTime.of(2018, 7, 3, 23, 59));
        assertEquals(LocalDateTime.of(2018, 7, 1, 0, 59), result);

    }

}