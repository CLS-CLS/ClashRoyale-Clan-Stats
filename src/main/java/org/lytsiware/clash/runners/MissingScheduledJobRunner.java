package org.lytsiware.clash.runners;

import org.lytsiware.clash.core.service.job.RunAtStartupJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;

@Component
public class MissingScheduledJobRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MissingScheduledJobRunner.class);

    @Value("${checkMissingScheduler}")
    private boolean checkMissingScheduler;

    @Autowired(required = false)
    private List<RunAtStartupJob> jobs;

    @Override
    @Transactional
    @Retryable(maxAttempts = 3, backoff = @Backoff(600000))
    public void run(String... args) {
        if (!checkMissingScheduler) {
            logger.info("Checking for missing schedulers is disabled");
            return;
        }
        if (jobs == null || jobs.size() == 0) {
            logger.warn("No runAtStartUp schedulers was found");
            return;
        }
        logger.info("Checking for missing schedulers");
        for (RunAtStartupJob job : jobs) {
            logger.info("Checking status for scheduler {}", job.getClass().getName());
            if (job.shouldRun()) {
                logger.info("Scheduler {} has not ran: Starting job now", job.getClass().getSimpleName());
                job.run();
            } else {
                logger.info("Status OK");
            }
        }
    }
}
