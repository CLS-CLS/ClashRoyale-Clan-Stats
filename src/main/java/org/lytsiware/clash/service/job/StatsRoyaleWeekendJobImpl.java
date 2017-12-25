package org.lytsiware.clash.service.job;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.ZoneIdConfiguration;
import org.lytsiware.clash.domain.job.WeekJobRepository;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.service.ClanStatsService;
import org.lytsiware.clash.service.integration.StatsRoyaleSiteServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Profile("statsRoyale")
public class StatsRoyaleWeekendJobImpl implements Job {

	private Logger logger = LoggerFactory.getLogger(StatsRoyaleWeekendJobImpl.class);

	@Autowired
	ClanStatsService clanStatsService;
	
	@Autowired
	StatsRoyaleSiteServiceImpl siteIntegrationService; ;

	@Autowired
	WeekJobRepository weeklyJobRepository;

	@Override
	@Scheduled(cron = "0 59 7/4 ? * SUN,FRI,SAT", zone = ZoneIdConfiguration.zoneId)
	public void run() {
		try {
			logger.info("Job Triggered at {}", LocalDateTime.now());
			List<PlayerWeeklyStats> stats = siteIntegrationService.retrieveData(true);
			clanStatsService.updateOrInsertNewDonations(stats, Week.now(), true);
			clanStatsService.updateChestContributions(stats, Week.now(), true);
		} catch (Exception e) {
			logger.error("oops", e);
			throw e;
		}
	}
	
	@Scheduled(cron = "0 59 21 ? * SUN", zone = ZoneIdConfiguration.zoneId)
	public void extraCheck(){
		run();
	}
	
}
