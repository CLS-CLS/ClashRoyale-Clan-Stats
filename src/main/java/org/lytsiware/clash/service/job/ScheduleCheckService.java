package org.lytsiware.clash.service.job;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class ScheduleCheckService {
	
	private Logger logger = LoggerFactory.getLogger(ScheduleCheckService.class);
	
	@Autowired
	ClanStatsJob job;
	
	/**
     * 
     * @return false if the scheduler has already run
     */
    @PostConstruct
	@Transactional
	@Retryable(maxAttempts = 3, backoff = @Backoff(600000))
	public void runMissingScheduler() {
    	logger.info("Check for missing schedulers");
    	
    	if (job.shouldRun()) {
    		logger.info("Scheduler has not ran: Starting job now");
    		job.run();
    	}else {
    		logger.info("Status OK");
    	}
    	
    }

}
