package org.lytsiware.clash.donation.service.job;


import org.lytsiware.clash.Week;
import org.lytsiware.clash.core.domain.job.Job;
import org.lytsiware.clash.core.domain.job.JobRepository;
import org.lytsiware.clash.core.service.job.RunAtStartupJob;
import org.lytsiware.clash.core.service.job.scheduledname.ScheduledName;
import org.lytsiware.clash.donation.service.DonationAggregationService;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@Profile("statsRoyale")
@Transactional(Transactional.TxType.REQUIRED)
public class DonatationAggratationJobImpl implements RunAtStartupJob {

    public static final String CRON_MONDAY = "cron.monday";

    private final Logger logger = LoggerFactory.getLogger(DonatationAggratationJobImpl.class);

    private final JobRepository weeklyJobRepository;

    private final DonationAggregationService aggregationService;

    private final PropertyResolver propertyResolver;

    @Autowired
    public DonatationAggratationJobImpl(JobRepository weeklyJobRepository, DonationAggregationService aggregationService, PropertyResolver propertyResolver) {
        this.weeklyJobRepository = weeklyJobRepository;
        this.aggregationService = aggregationService;
        this.propertyResolver = propertyResolver;
    }


    @Override
    @Scheduled(cron = "${" + CRON_MONDAY + "}")
    @ScheduledName(name = "Mondays task")
    @Retryable(maxAttempts = 3, backoff = @Backoff(600000))
    public void run() {
        try {
            logger.info("Job Triggered at {}", LocalDateTime.now());
            Week week = Week.now().previous();
            aggregationService.calculateAndSaveAvgs(week);
            weeklyJobRepository.save(new Job(StatsRoyaleWeekendJobImpl.class.getSimpleName(), LocalDateTime.now()));
        } catch (Exception e) {
            logger.error("oops", e);
            throw e;
        }
    }

    @Override
    public boolean shouldRun() {
        logger.info("Checking if scheduler should run");
        boolean result = false;

        Job latestRun = weeklyJobRepository.findById(StatsRoyaleWeekendJobImpl.class.getSimpleName()).orElse(null);

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

        ZonedDateTime nextExecutionDate = Utils.getNextExecutionDate(cronExpression, latestRun.getLatestExecution().atZone(ZoneId.systemDefault()));
        logger.info("latest scheduler run was at {} ", latestRun.getLatestExecution());
        logger.info("next execution date is at {}", nextExecutionDate);
        if (nextExecutionDate.isAfter(ZonedDateTime.now(ZoneId.systemDefault()))) {
            logger.info("Scheduler has already run");
            return false;
        }

        logger.info("Scheduler was not fired");

        return true;
    }
}
