package org.lytsiware.clash.service.job;

import java.time.LocalDateTime;
import java.util.List;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.job.WeekJobRepository;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.service.ClanStatsService;
import org.lytsiware.clash.service.integration.SiteIntegrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Profile("statsRoyale")
public class StatsRoyaleMondayFinalCallJobImpl implements Job {

	private Logger logger = LoggerFactory.getLogger(StatsRoyaleMondayFinalCallJobImpl.class);

	@Autowired
	ClanStatsService clanStatsService;
	
	@Autowired
	SiteIntegrationService siteIntegrationService;

	@Autowired
	WeekJobRepository weeklyJobRepository;

	@Override
	@Scheduled(cron = "0 0 3-7/2 ? * MON")
	public void run() {
		try {
			logger.info("Job Triggered at {}", LocalDateTime.now());
			List<PlayerWeeklyStats> stats = siteIntegrationService.retrieveData();
			clanStatsService.updateOrInsertNewDonations(stats, new Week().minusWeeks(1), true);
			//TODO weeklyJobRepository 
		} catch (Exception e) {
			logger.error("oops", e);
			throw e;
		}
	}
	
	
}
