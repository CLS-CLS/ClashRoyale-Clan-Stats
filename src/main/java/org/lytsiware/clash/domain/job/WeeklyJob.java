package org.lytsiware.clash.domain.job;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.ZonedDateTime;

@Entity
@Table(name = "JOB_EXECUTION")
public class WeeklyJob {

	@Id
    private String jobId;

    private ZonedDateTime latestExecution;

    public WeeklyJob(String jobId, ZonedDateTime latestExecution) {
        this.jobId = jobId;
        this.latestExecution = latestExecution;
    }

    public WeeklyJob() {

    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public ZonedDateTime getLatestExecution() {
        return latestExecution;
    }

    public void setLatestExecution(ZonedDateTime latestWeek) {
        this.latestExecution = latestWeek;
    }


}
