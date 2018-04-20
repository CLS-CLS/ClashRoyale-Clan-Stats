package org.lytsiware.clash.service.job;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.ZoneIdConfiguration;
import org.lytsiware.clash.domain.job.WeekJobRepository;
import org.lytsiware.clash.domain.job.WeeklyJob;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.service.AggregationService;
import org.lytsiware.clash.service.clan.UpdateStatsServiceImpl;
import org.lytsiware.clash.service.integration.StatsRoyaleSiteServiceImpl;
import org.lytsiware.clash.service.job.scheduledname.ScheduledName;
import org.lytsiware.clash.utils.TestableLocalDateTime;
import org.lytsiware.clash.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.PropertyResolver;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@Profile("statsRoyale")
@Transactional(Transactional.TxType.REQUIRED)
public class StatsRoyalChestContrJobImpl implements RunAtStartupJob {

    public static final String CRON_MONDAY = "cron.monday";
    private static final String CRON_MONDAY_PROPERTY = "${" + CRON_MONDAY + "}";

    private Logger logger = LoggerFactory.getLogger(StatsRoyalChestContrJobImpl.class);

    private StatsRoyaleSiteServiceImpl siteIntegrationService;

    private WeekJobRepository weeklyJobRepository;

    private UpdateStatsServiceImpl updateStatsService;

    private AggregationService aggregationService;

    private PropertyResolver propertyResolver;

    @Autowired
    public StatsRoyalChestContrJobImpl(StatsRoyaleSiteServiceImpl siteIntegrationService, WeekJobRepository weeklyJobRepository,
                                       UpdateStatsServiceImpl updateStatsService, AggregationService aggregationService,
                                       PropertyResolver propertyResolver) {
        this.siteIntegrationService = siteIntegrationService;
        this.weeklyJobRepository = weeklyJobRepository;
        this.updateStatsService = updateStatsService;
        this.aggregationService = aggregationService;
        this.propertyResolver = propertyResolver;
    }


    @Override
    @Scheduled(cron = "${" + CRON_MONDAY + "}", zone = ZoneIdConfiguration.zoneId)
    @ScheduledName("Mondays task")
    @Retryable(maxAttempts = 3, backoff = @Backoff(600000))
    public void run() {
        try {
            logger.info("Job Triggered at {}", LocalDateTime.now());

            List<PlayerWeeklyStats> stats = siteIntegrationService.retrieveData(true);

            Week week = Week.now().previous();
            updateStatsService.updateChestContibutionAndRole(stats, week, true);
            updateStatsService.markPlayerIsInClan(stats);
            aggregationService.calculateAndSaveAvgs(week);
            aggregationService.calculateAndUpdateClanChestScore(week);
            weeklyJobRepository.save(new WeeklyJob(StatsRoyaleWeekendJobImpl.class.getSimpleName(), ZonedDateTime.now()));
        } catch (Exception e) {
            logger.error("oops", e);
            throw e;
        }
    }

    @Override
    public boolean shouldRun() {
        logger.info("Checking if scheduler should run");
        boolean result = false;

        WeeklyJob latestRun = weeklyJobRepository.loadLatest(StatsRoyaleWeekendJobImpl.class.getSimpleName());

        //first time the changes are applied, that is why there is no latestRun. Do not run the scheduler
        if (latestRun == null) {
            return false;
        }

        String cronExpression;

        try {
            cronExpression = propertyResolver.resolvePlaceholders(this.getClass().getMethod("run").getAnnotation(Scheduled.class).cron());
        } catch (NoSuchMethodException e) {
            logger.error("Error while checking if the scheduler has to be run", e);
            return false;
        }

        ZonedDateTime nextExecutionDate = Utils.getNextExecutionDate(cronExpression, latestRun.getLatestExecution());
        logger.info("latest scheduler run was at {} ", latestRun.getLatestExecution());
        logger.info("next execution date is at {}", nextExecutionDate);
        if (nextExecutionDate.isAfter(TestableLocalDateTime.getZonedDateTimeNow())) {
            logger.info("Scheduler has already run");
            return false;
        }

        logger.info("Scheduler was not fired");

        return true;
    }
}
