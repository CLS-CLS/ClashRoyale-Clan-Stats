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
        ScheduledFuture<?> previousSelfScheduledFuture = scheduledFuture;
        executeAndReschedule();
        if (previousSelfScheduledFuture != null) {
            previousSelfScheduledFuture.cancel(false);
        }
    }

    /**
     * Implement this method with business logic that need te be run
     *
     * @return The date of the next execution
     */
    protected abstract Date run();


    private void executeAndReschedule() {
        Date date = run();
        synchronized (monitor) {
            log.info("Task will run again at : {} ", date);
            trigger.setNextExecutionTime(date);
            scheduledFuture = taskScheduler.schedule(() -> executeAndReschedule(), trigger);
        }
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
