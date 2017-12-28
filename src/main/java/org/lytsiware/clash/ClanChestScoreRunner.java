package org.lytsiware.clash;

import org.lytsiware.clash.service.ClanStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ClanChestScoreRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ClanChestScoreRunner.class);

    @Autowired
    private ClanStatsService clanStatsService;

    @Override
    public void run(String... args) throws Exception {

        for (int i = 1; i <= 12; i++) {
            Week week = Week.now().minusWeeks(i);
            clanStatsService.calculateAndUpdateClanChestScore(week);
        }

    }
}
