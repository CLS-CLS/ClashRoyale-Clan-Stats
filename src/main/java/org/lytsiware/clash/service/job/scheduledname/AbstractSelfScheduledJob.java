package org.lytsiware.clash.service.job.scheduledname;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

/**
 * Runs a repeatable job which can alter the date of the next execution depending on the result of the current execution
 * The job to run should be implemented in the {@link #run()} method. The method should return the Date of the next execution.
 * The job should be manually started the first time. <p>If there is a scheduler that needs to also be triggered periodically, create
 * a method, annotate it with @Scheduled, and just call the {@link #fixedScheduler()}
 */
@Slf4j
public abstract class AbstractSelfScheduledJob {

    @Autowired
    protected TaskScheduler taskScheduler;

    private CustomTrigger trigger = new CustomTrigger();

    private Object monitor = new Object();

    private ScheduledFuture<?> scheduledFuture;

    /**
     * runs the business logic and reschedules the job according to the result of the the business logic execution.
     * Upon succesfull completion any previously self scheduled job is cancelled
     */
    public void fixedScheduler() {
        log.info("Fixed Scheduler");
        ScheduledFuture<?> previousSelfScheduledFuture = scheduledFuture;
        boolean success = executeAndReschedule();
        if (success && previousSelfScheduledFuture != null) {
            previousSelfScheduledFuture.cancel(false);
        } else {
            throw new RuntimeException("Fixed Scheduler failed");
        }
    }

    /**
     * Implement this method with business logic that need te be run
     *
     * @return The date of the next execution
     */
    protected abstract Date run();


    private boolean executeAndReschedule() {
        try {
            Date date = run();
            log.info("Task will run again at : {} ", date);
            if (date == null) {
                return true;
            }
            synchronized (monitor) {
                trigger.setNextExecutionTime(date);
                scheduledFuture = taskScheduler.schedule(() -> executeAndReschedule(), trigger);
            }
            return true;
        } catch (Exception ex) {
            log.warn("Error while rescheduling ", ex);
        }
        return false;
    }


    public static class CustomTrigger implements Trigger {

        private Date nextExecutionTime = new Date();

        public void setNextExecutionTime(Date nextExecutionTime) {
            this.nextExecutionTime = nextExecutionTime;
        }

        @Override
        public Date nextExecutionTime(TriggerContext triggerContext) {
            return nextExecutionTime;
        }
    }

}
