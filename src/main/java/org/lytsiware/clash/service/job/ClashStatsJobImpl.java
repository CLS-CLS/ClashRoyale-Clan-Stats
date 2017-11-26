package org.lytsiware.clash.service.job;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.ZoneIdConfiguration;
import org.lytsiware.clash.domain.job.WeekJobRepository;
import org.lytsiware.clash.domain.job.WeeklyJob;
import org.lytsiware.clash.service.ClanStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Profile("clashStats")
public class ClashStatsJobImpl implements Job, RunAtStartupJob {

	private Logger logger = LoggerFactory.getLogger(ClashStatsJobImpl.class);

	@Autowired
	ClanStatsService clanStatsService;

	@Autowired
	WeekJobRepository weeklyJobRepository;

	@Override
	@Scheduled(cron = "0 5 10 ? * MON ")
	@Retryable(maxAttempts = 3, backoff = @Backoff(600000))
	public void run() {
		try {
			logger.info("Job Triggered at {}", LocalDate.now());

			clanStatsService.updateDatabaseWithLatest();

			weeklyJobRepository.save(new WeeklyJob(new Week().minusWeeks(1).getWeek()));
		} catch (Exception e) {
			logger.error("oops", e);
			throw e;
		}
	}

	@Override
	public boolean shouldRun() {
		logger.info("Checking if scheduler should run");
		boolean result = false;

		Week previousWeek = new Week(LocalDate.now()).minusWeeks(1);

		WeeklyJob latestRun = weeklyJobRepository.loadLatest();

		if (latestRun.getLatestWeek() == previousWeek.getWeek()) {
			logger.info("scheduler has already been fired");
			return false;
		}

		// FIX the "dayTheSchedulerShouldHaveRun" should not be hardcoded but
		// calculated from the cron expression:

		// There is a gap between the day the timer runs and when the week
		// changes (Sunday). In that case we dont want
		// to update the db.
		// Monday 12:10 GMT+2 time 
		LocalDateTime dayTheSchedulerShouldHaveRun = previousWeek.getEndDate().atTime(LocalTime.of(9, 10)).plusDays(1); 

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
