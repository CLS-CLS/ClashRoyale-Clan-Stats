package org.lytsiware.clash.service.job;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.job.WeekJobRepository;
import org.lytsiware.clash.domain.job.WeeklyJob;
import org.lytsiware.clash.service.IClanStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ClanStatsJobImpl implements ClanStatsJob {

    private Logger logger = LoggerFactory.getLogger(ClanStatsJobImpl.class);

    @Autowired
    IClanStatsService clanStatsService;
    
    @Autowired
    WeekJobRepository weeklyJobRepository;

    @Override
	@Scheduled(cron ="0 23 11 ? * MON ")
    @Retryable(maxAttempts = 3, backoff = @Backoff(600000))
    public void run() {
    	try {
	        logger.info("Job Triggered at {}", LocalDate.now());
	        
	        clanStatsService.updateDatabaseWithLatest();
	        
	        weeklyJobRepository.save(new WeeklyJob(new Week().minusWeeks(1).getWeek()));
    	}catch (Exception e) {
			logger.error("oops", e);
			throw e;
		}
    }
    
   

	@Override
	public boolean shouldRun() {
    	
    	boolean result = false;
    	
    	Week previousWeek = new Week(LocalDate.now()).minusWeeks(1);
    	
    	//FIX the "dayTheSchedulerShouldHaveRun" should not be hardcoded but calculated from the cron expression:
    	
    	//There is a gap  between the day the timer runs and when the week changes (Sunday). In that case we dont want 
    	//to update the db. 
    	LocalDateTime dayTheSchedulerShouldHaveRun = previousWeek.getEndDate().atTime(LocalTime.of(12, 0)).plusDays(1); //monday at 13:00 o clock server time
    	
    	if (LocalDateTime.now().isAfter(dayTheSchedulerShouldHaveRun)) {
    	
    		logger.info("The scheduler should have already run.  Checking status");
    		
    		WeeklyJob latestRun = weeklyJobRepository.loadLatest();
    		
    		if (latestRun.getLatestWeek() == previousWeek.getWeek()){
    			logger.info("Status OK");
    		}else {
    			logger.info("Scheduler has not ran: Starting job now");
    			result = true;
    		}
    	}
    	
    	return result;
	}
    

}
