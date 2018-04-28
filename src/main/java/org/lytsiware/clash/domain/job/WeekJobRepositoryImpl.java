package org.lytsiware.clash.domain.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

@Repository
public class WeekJobRepositoryImpl implements WeekJobRepository {

    Logger logger = LoggerFactory.getLogger(WeekJobRepositoryImpl.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional(value=TxType.REQUIRED)
    public void save(WeeklyJob week) {
    	logger.info("Save job run for week: {}," + week.getLatestExecution());
        em.merge(week);
    }

	@Override
	public WeeklyJob loadLatest(String jobId) {
		return em.find(WeeklyJob.class, jobId);
	}

}
