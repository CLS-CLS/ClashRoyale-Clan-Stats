package org.lytsiware.clash.war2.scheduler;

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


@Service
@RequiredArgsConstructor
@Slf4j
public class RiverRaceUpdateScheduler implements RunAtStartupJob {

    private static final String JOB_ID = "ActualRiverRaceExecution";

    private final JobRepository jobRepository;
    private final RiverRaceManager riverRaceManager;
    private final RiverRaceRepository riverRaceRepository;


    @Override
    public boolean shouldRun() {
        boolean shouldRun = false;
        RiverRace riverRace = riverRaceRepository.activeRace().orElse(null);
        Job latestJob = jobRepository.findById(JOB_ID).orElse(null);
        if (riverRace == null) {
            log.info("River race does not exist - should run");
            shouldRun = true;
        } else if (riverRace.getClan().getFinishTime() == null
                && latestJob.getLatestExecution().plusHours(2).isBefore(LocalDateTime.now())) {
            log.info("River race does finished and job has to run for at least 2 hours - should run");
            shouldRun = true;
        } else {

            if (latestJob == null) {
                log.info("job runs for first time - should run");
                shouldRun = true;
            } else if (riverRace.getClan().getFinishTime() != null &&
                    latestJob.getLatestExecution().plusHours(23).isBefore(LocalDateTime.now())) {
                log.info("Job has not run for at least 23 hours - should run");
                shouldRun = true;
            }
        }
        return shouldRun;
    }

    @ScheduledName(name = "riverrace")
    @Scheduled(cron = "${cron.riverrace}")
    public void run() {
        log.info("River race scheduler triggered .. check if should run");
        if (shouldRun()) {
            doRun();
        } else {
            log.info("Scheduler has run recently and currentRace is finished - not need to run");
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
