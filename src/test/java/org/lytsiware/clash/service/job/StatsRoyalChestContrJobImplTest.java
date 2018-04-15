package org.lytsiware.clash.service.job;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lytsiware.clash.ZoneIdConfiguration;
import org.lytsiware.clash.domain.job.WeekJobRepository;
import org.lytsiware.clash.domain.job.WeekJobRepositoryImpl;
import org.lytsiware.clash.domain.job.WeeklyJob;
import org.lytsiware.clash.utils.TestableLocalDateTime;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.env.MockEnvironment;

import java.time.ZonedDateTime;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TestableLocalDateTime.class)
public class StatsRoyalChestContrJobImplTest {

    private MockEnvironment propertyResolver = new MockEnvironment();

    private WeekJobRepository weeklyJobRepository;

    private StatsRoyalChestContrJobImpl statsRoyalChestContrJob;


    @Before
    public void init() {
        weeklyJobRepository = Mockito.mock(WeekJobRepositoryImpl.class);
        propertyResolver.setProperty(StatsRoyalChestContrJobImpl.CRON_MONDAY, "0 0 8 ? * MON");
        PowerMockito.mockStatic(TestableLocalDateTime.class);
        statsRoyalChestContrJob = new StatsRoyalChestContrJobImpl(null, weeklyJobRepository, null, null, propertyResolver);
    }

    @Test
    public void shouldNotRun() throws Exception {
        Mockito.when(weeklyJobRepository.loadLatest(Mockito.anyString())).thenReturn(new WeeklyJob("job", ZonedDateTime.of(2018, 4, 9, 8, 0, 0, 0, ZoneIdConfiguration.zoneId())));
        Mockito.when(TestableLocalDateTime.getZonedDateTimeNow()).thenReturn(ZonedDateTime.of(2018, 4, 16, 7, 0, 0, 0, ZoneIdConfiguration.zoneId()));
        Assert.assertFalse(statsRoyalChestContrJob.shouldRun());
    }

    @Test
    public void shouldRun() throws Exception {
        Mockito.when(weeklyJobRepository.loadLatest(Mockito.anyString())).thenReturn(new WeeklyJob("job", ZonedDateTime.of(2018, 4, 9, 8, 0, 0, 0, ZoneIdConfiguration.zoneId())));
        Mockito.when(TestableLocalDateTime.getZonedDateTimeNow()).thenReturn(ZonedDateTime.of(2018, 4, 16, 9, 0, 0, 0, ZoneIdConfiguration.zoneId()));
        Assert.assertTrue(statsRoyalChestContrJob.shouldRun());
    }

}