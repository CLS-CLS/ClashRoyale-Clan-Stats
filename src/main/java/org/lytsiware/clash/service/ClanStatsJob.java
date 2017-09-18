package org.lytsiware.clash.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ClanStatsJob {

    private Logger logger = LoggerFactory.getLogger(ClanStatsJob.class);
	


    @Autowired
    IClanStatsService clanStatsService;

    @Scheduled(cron ="0 23 11 ? * MON ")
    @Retryable(maxAttempts = 3, backoff = @Backoff(600000))
    public void run() {
        logger.info("Job Triggered at {}", LocalDate.now());
        clanStatsService.updateDatabaseWithLatest();
    }

}
