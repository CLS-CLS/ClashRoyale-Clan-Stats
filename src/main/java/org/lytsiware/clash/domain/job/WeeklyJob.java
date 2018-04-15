package org.lytsiware.clash.domain.job;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.ZonedDateTime;

@Entity
public class WeeklyJob {

	@Id
    String jobId;

    ZonedDateTime latestWeek;

    public WeeklyJob(String jobId, ZonedDateTime latestWeek) {
        this.jobId = jobId;
        this.latestWeek = latestWeek;
    }

    public WeeklyJob() {

    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public ZonedDateTime getLatestWeek() {
        return latestWeek;
    }

    public void setLatestWeek(ZonedDateTime latestWeek) {
        this.latestWeek = latestWeek;
    }


}
