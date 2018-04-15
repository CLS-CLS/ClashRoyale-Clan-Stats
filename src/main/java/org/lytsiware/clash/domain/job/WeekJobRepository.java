package org.lytsiware.clash.domain.job;

public interface WeekJobRepository {

    void save(WeeklyJob week);

    WeeklyJob loadLatest(String jobId);
}
