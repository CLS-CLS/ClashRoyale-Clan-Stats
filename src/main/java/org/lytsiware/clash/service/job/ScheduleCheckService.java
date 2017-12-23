package org.lytsiware.clash.service.job;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class ScheduleCheckService {
	
	private Logger logger = LoggerFactory.getLogger(ScheduleCheckService.class);
	
	@Value("${checkMissingScheduler}")
	boolean checkMissingScheduler;
	
	@Autowired
	List<Job> jobs;
	
	/**
     * 
     * @return false if the scheduler has already run
     */
    @PostConstruct
	@Transactional
	@Retryable(maxAttempts = 3, backoff = @Backoff(600000))
	public void runMissingScheduler() {
    	if (!checkMissingScheduler) {
    		return;
    	}
    	
    	logger.info("Check for missing schedulers");
    	for (Job job : jobs) {
    		if (job instanceof RunAtStartupJob) {
		    	if (((RunAtStartupJob)job).shouldRun()) {
		    		logger.info("Scheduler has not ran: Starting job now");
		    		job.run();
		    	}else {
		    		logger.info("Status OK");
		    	}
    		}
    	}
    	
    }

}
