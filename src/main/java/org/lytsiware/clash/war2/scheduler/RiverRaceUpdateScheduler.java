package org.lytsiware.clash.war2.scheduler;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.core.domain.job.Job;
import org.lytsiware.clash.core.domain.job.JobRepository;
import org.lytsiware.clash.core.service.job.RunAtStartupJob;
import org.lytsiware.clash.core.service.job.scheduledname.ScheduledName;
import org.lytsiware.clash.war2.domain.RiverRace;
import org.lytsiware.clash.war2.repository.RiverRaceRepository;
import org.lytsiware.clash.war2.service.RiverRaceManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class RiverRaceUpdateScheduler implements RunAtStartupJob {

    private static final String JOB_ID = "ActualRiverRaceExecution";

    private final JobRepository jobRepository;
    private final RiverRaceManager riverRaceManager;
    private final RiverRaceRepository riverRaceRepository;

    /**
     * logs the appropriate message depenging on the boolean
     */
    @Builder
    @Slf4j
    private static class Description {
        final String trueMessage;
        final String falseMessage;

        public Description(String trueMessage, String falseMessage) {
            this.trueMessage = Optional.ofNullable(trueMessage).orElse("");
            this.falseMessage = Optional.ofNullable(falseMessage).orElse("");
        }

        public boolean of(boolean bool) {
            if (bool) {
                log.info(trueMessage);
            } else {
                log.info(falseMessage);
            }
            return bool;
        }

    }

    private static final Description description1 = new Description("River Race does not exist (should run)", "River race exists");
    private static final Description description2 = new Description("River race has not finished", "River race finished");
    private static final Description description3 = new Description("job has to run for at least 2 hours (should run)", "job has already run in less than 2 hours");
    private static final Description description4 = new Description("job runs for first time (should run)", "There is a previous job that had run");
    private static final Description description6 = new Description("Job has not run for at least 23 hours", "Previous job had run in less than 23 hours");


    @Override
    public boolean shouldRun() {
        boolean shouldRun = false;
        RiverRace riverRace = riverRaceRepository.activeRace().orElse(null);
        Job latestJob = jobRepository.findById(JOB_ID).orElse(null);

        if (description1.of(riverRace == null)) {
            shouldRun = true;
        } else if (description2.of(riverRace.getClan().getFinishTime() == null)
                && description3.of(latestJob.getLatestExecution().plusHours(2).isBefore(LocalDateTime.now()))) {
            shouldRun = true;
        } else {
            if (description4.of(latestJob == null)) {
                shouldRun = true;
            } else if (riverRace.getClan().getFinishTime() != null && description6.of(
                    latestJob.getLatestExecution().plusHours(23).isBefore(LocalDateTime.now()))) {
                log.info("Job has not run for at least 23 hours");
                shouldRun = true;
            }
        }
        log.info("Should run = {}", shouldRun);
        return shouldRun;
    }

    @ScheduledName(name = "riverrace")
    @Scheduled(cron = "${cron.riverrace}")
    public void run() {
        log.info("River race scheduler triggered .. check if should run");
        if (shouldRun()) {
            doRun();
        }
    }

    /**
     * Scheduler to force run even the shouldRun says no
     */
    @ScheduledName(name = "riverrace-force", missingScheduled = true)
    @Transactional
    public void doRun() {
        log.info("running job riverrace (conditions are met or manually triggered");
        riverRaceManager.updateRiverRace();
        jobRepository.saveAndFlush(new Job(JOB_ID, LocalDateTime.now()));

    }

    @ScheduledName(name = JOB_ID, missingScheduled = true)
    public void workaroud() {
        log.info("Run scheduler riverrace-force instead or riverrace");
        //this is just a workaround in order to see when the scheduler with name JOB_ID has run the last time
    }


}
