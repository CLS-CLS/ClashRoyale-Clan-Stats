package org.lytsiware.clash;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lytsiware.clash.domain.job.Job;
import org.lytsiware.clash.domain.job.JobRepository;
import org.lytsiware.clash.service.job.StatsRoyalChestContrJobImpl;
import org.lytsiware.clash.utils.TestableLocalDateTime;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.env.MockEnvironment;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TestableLocalDateTime.class)
public class StatsRoyalChestContrJobImplTest {

    private MockEnvironment propertyResolver = new MockEnvironment();

    private JobRepository weeklyJobRepository;

    private StatsRoyalChestContrJobImpl statsRoyalChestContrJob;


    @Before
    public void init() {
        weeklyJobRepository = Mockito.mock(JobRepository.class);
        propertyResolver.setProperty(StatsRoyalChestContrJobImpl.CRON_MONDAY, "0 0 8 ? * MON");
        PowerMockito.mockStatic(TestableLocalDateTime.class);
        statsRoyalChestContrJob = new StatsRoyalChestContrJobImpl(weeklyJobRepository, null, propertyResolver);
    }

    @Test
    public void shouldNotRun() throws Exception {
        Mockito.when(weeklyJobRepository.findById(Mockito.anyString())).thenReturn(Optional.of(new Job("job", LocalDateTime.of(2018, 4, 9, 8, 0, 0, 0))));
        Mockito.when(TestableLocalDateTime.getZonedDateTimeNow()).thenReturn(ZonedDateTime.of(2018, 4, 16, 7, 0, 0, 0, ZoneIdConfiguration.zoneId()));
        Assert.assertFalse(statsRoyalChestContrJob.shouldRun());
    }

    @Test
    public void shouldRun() throws Exception {
        Mockito.when(weeklyJobRepository.findById(Mockito.anyString())).thenReturn(Optional.of(new Job("job", LocalDateTime.of(2018, 4, 9, 8, 0, 0, 0))));
        Mockito.when(TestableLocalDateTime.getZonedDateTimeNow()).thenReturn(ZonedDateTime.of(2018, 4, 16, 9, 0, 0, 0, ZoneIdConfiguration.zoneId()));
        Assert.assertTrue(statsRoyalChestContrJob.shouldRun());
    }

}