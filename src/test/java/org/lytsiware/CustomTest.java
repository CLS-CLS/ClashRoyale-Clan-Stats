package org.lytsiware;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lytsiware.clash.Application;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.job.WeekJobRepository;
import org.lytsiware.clash.domain.job.WeeklyJob;
import org.lytsiware.clash.service.job.ClanStatsJob;
import org.lytsiware.clash.service.job.ScheduleCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
@TestPropertySource(locations="classpath:test.properties")
public class CustomTest {
    
    @Autowired
    private WeekJobRepository weeklyJobRepo;
    
    @Autowired
    private ClanStatsJob clanStatJob;
    
    @MockBean
    private ScheduleCheckService scheduleCheckService;
    
    @MockBean
    private org.springframework.scheduling.TaskScheduler scheduler;
    
    @Test
    public void testWeeklyJob(){
    	WeeklyJob wj0 = new WeeklyJob(0);
    	weeklyJobRepo.save(wj0);
    	WeeklyJob wj10 = new WeeklyJob(10);
    	weeklyJobRepo.save(wj10);
    	WeeklyJob wj11 = new WeeklyJob(11);
    	weeklyJobRepo.save(wj11);
    	
    	WeeklyJob latest = weeklyJobRepo.loadLatest();
    	Assert.assertEquals(11, latest.getLatestWeek());
    }
    
    @Test
    public void schedulePostConstruct() {
    	weeklyJobRepo.save(new WeeklyJob(new Week().minusWeeks(2).getWeek()));
    	boolean shouldRun  = clanStatJob.shouldRun();
    	Assert.assertTrue(shouldRun);
    	clanStatJob.run();
    	shouldRun  = clanStatJob.shouldRun();
    	Assert.assertFalse(shouldRun);
    	
    	
    	
    }


}
