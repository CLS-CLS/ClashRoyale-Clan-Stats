package org.lytsiware.clash.service.job;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.ZoneIdConfiguration;
import org.lytsiware.clash.domain.job.WeekJobRepository;
import org.lytsiware.clash.domain.job.WeeklyJob;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.service.ClanStatsService;
import org.lytsiware.clash.service.integration.StatsRoyaleSiteServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Profile("statsRoyale")
public class StatsRoyalChestContrJobImpl implements Job, RunAtStartupJob {

	private Logger logger = LoggerFactory.getLogger(StatsRoyalChestContrJobImpl.class);

	@Autowired
	ClanStatsService clanStatsService;
	
	@Autowired
	StatsRoyaleSiteServiceImpl siteIntegrationService; ;
	
	@Autowired
	WeekJobRepository weeklyJobRepository;

	

	@Override
	@Scheduled(cron = "0 0 8 ? * MON")
	@Retryable(maxAttempts = 3, backoff = @Backoff(600000))
	public void run() {
		try {
			logger.info("Job Triggered at {}", LocalDateTime.now());
			
			List<PlayerWeeklyStats> stats = siteIntegrationService.retrieveData(true);
			
			Week week = new Week().minusWeeks(1);
			clanStatsService.updateChestContributions(stats, week);
			clanStatsService.recalculateAndSaveAvgs(week);
			weeklyJobRepository.save(new WeeklyJob(new Week().getWeek()));
		} catch (Exception e) {
			logger.error("oops", e);
			throw e;
		}
	}



	@Override
	public boolean shouldRun() {
		logger.info("Checking if scheduler should run");
		boolean result = false;

		WeeklyJob latestRun = weeklyJobRepository.loadLatest();

		if (latestRun.getLatestWeek() == new Week().getWeek()) {
			logger.info("scheduler has already been fired");
			return false;
		}
		
		LocalDateTime dayTheSchedulerShouldHaveRun = new Week().getStartDate().atTime(LocalTime.of(8, 0)); 

		if (LocalDateTime.now(ZoneIdConfiguration.zoneId()).isAfter(dayTheSchedulerShouldHaveRun)) {
			logger.info("Scheduler was not fired");
			result = true;
		} else {
			logger.info("date is in between the gap");
		}
		
		logger.info("Scheduler should run now: {}", result);
		return result;
	}
}
