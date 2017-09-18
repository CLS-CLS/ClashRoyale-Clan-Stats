package org.lytsiware.clash.domain.job;

public interface WeekJobRepository {
    
	public WeeklyJob loadLatest();

    public void save(WeeklyJob week);


}
