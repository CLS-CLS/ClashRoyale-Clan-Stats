package org.lytsiware.clash.core.domain.job;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "JOB_EXECUTION")
public class Job {

	@Id
    private String jobId;

    @Basic
    private LocalDateTime latestExecution;

    public Job(String jobId, LocalDateTime latestExecution) {
        this.jobId = jobId;
        this.latestExecution = latestExecution;
    }

    public Job() {

    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public LocalDateTime getLatestExecution() {
        return latestExecution;
    }

    public void setLatestExecution(LocalDateTime latestWeek) {
        this.latestExecution = latestWeek;
    }


}
