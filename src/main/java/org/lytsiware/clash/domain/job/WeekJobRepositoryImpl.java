package org.lytsiware.clash.domain.job;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository

public class WeekJobRepositoryImpl implements WeekJobRepository {

    Logger logger = LoggerFactory.getLogger(WeekJobRepositoryImpl.class);

    @PersistenceContext
    private EntityManager em;
    
    
    
    @Override
    @Transactional(value=TxType.REQUIRED)
    public void save(WeeklyJob week) {
    	logger.info("Save job run for week: {}," + week.getLatestWeek());
        em.persist(week);
    }

        

	@Override
	public WeeklyJob loadLatest() {
		WeeklyJob result = null;
		try {
			result = (WeeklyJob)em.createNamedQuery("weeklyjob.findLatest").setMaxResults(1).getSingleResult();
		}catch (NoResultException ex) {
			result  = new WeeklyJob(0);
		}
		return result;
		
	}

	

}
