package org.lytsiware.clash.domain.job;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries({ @NamedQuery(name = "weeklyjob.findLatest", query = "select w from WeeklyJob w order by w.latestWeek desc")})
public class WeeklyJob {

	@Id
	int latestWeek;

	public int getLatestWeek() {
		return latestWeek;
	}

	public void setLatestWeek(int latestWeek) {
		this.latestWeek = latestWeek;
	}

	public WeeklyJob(int latestWeek) {
		super();
		this.latestWeek = latestWeek;
	}

	public WeeklyJob() {
		super();
	}

}
