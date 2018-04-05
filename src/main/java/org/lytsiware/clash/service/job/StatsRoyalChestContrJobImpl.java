package org.lytsiware.clash.service.job;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.ZoneIdConfiguration;
import org.lytsiware.clash.domain.job.WeekJobRepository;
import org.lytsiware.clash.domain.job.WeeklyJob;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.service.AggregationService;
import org.lytsiware.clash.service.ClanChestScoreService;
import org.lytsiware.clash.service.clan.UpdateStatsServiceImpl;
import org.lytsiware.clash.service.integration.StatsRoyaleSiteServiceImpl;
import org.lytsiware.clash.service.job.scheduledname.ScheduledName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Profile("statsRoyale")
@Transactional(Transactional.TxType.REQUIRED)
public class StatsRoyalChestContrJobImpl implements RunAtStartupJob {

	private Logger logger = LoggerFactory.getLogger(StatsRoyalChestContrJobImpl.class);

	@Autowired
    private StatsRoyaleSiteServiceImpl siteIntegrationService;

	@Autowired
    private WeekJobRepository weeklyJobRepository;

	@Autowired
    private UpdateStatsServiceImpl updateStatsService;

    @Autowired
    private  AggregationService aggregationService;

    @Autowired
    private ClanChestScoreService clanChestScoreService;


	@Override
	@Scheduled(cron = "${cron.monday}")
	@ScheduledName("Mondays task")
	@Retryable(maxAttempts = 3, backoff = @Backoff(600000))
	public void run() {
		try {
			logger.info("Job Triggered at {}", LocalDateTime.now());
			
			List<PlayerWeeklyStats> stats = siteIntegrationService.retrieveData(true);
			
			Week week = Week.now().previous();
            updateStatsService.updateChestContibutionAndRole(stats, week, true);
            aggregationService.calculateAndSaveAvgs(week);
            clanChestScoreService.calculateAndUpdateClanChestScore(week);
			weeklyJobRepository.save(new WeeklyJob(Week.now().getWeek()));
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

		if (latestRun.getLatestWeek() == Week.now().getWeek()) {
			logger.info("scheduler has already been fired");
			return false;
		}
		
		LocalDateTime dayTheSchedulerShouldHaveRun = Week.now().getStartDate().atTime(LocalTime.of(8, 0)); 

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
