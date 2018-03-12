package org.lytsiware;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.controller.ClanStatsRestController;
import org.lytsiware.clash.dto.PlayerOverallStats;
import org.lytsiware.clash.service.integration.SiteConfigurationService;
import org.lytsiware.clash.service.job.StatsRoyalChestContrJobImpl;
import org.lytsiware.clash.service.job.StatsRoyaleWeekendJobImpl;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.mockito.BDDMockito.given;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest(Week.class)
@PowerMockIgnore("javax.management.*")
public class FlowTest extends AbstractSpringBootTest {
//    @BeforeClass
//    public static void setErrorLogging() {
//        LoggingSystem.get(AbstractSpringBootTest.class.getClassLoader()).setLogLevel(LoggingSystem.ROOT_LOGGER_NAME, LogLevel.INFO);
//    }

    private static final Week WEEK_10 = Week.fromWeek(10);
    private static final Week WEEK_11 = Week.fromWeek(11);

    @MockBean
    private SiteConfigurationService siteConfigurationService;

    @Autowired
    private StatsRoyalChestContrJobImpl statsRoyalChestContrJob;

    @Autowired
    private StatsRoyaleWeekendJobImpl statsRoyaleWeekendJob;

    @Autowired
    private ClanStatsRestController restController;

    @Before
    public void initMocks(){
        Mockito.when(siteConfigurationService.getRefreshUrl()).thenReturn("");
    }


    @Test
    public void runJobAndShowClanStats() throws MalformedURLException {

        //Week 10
        PowerMockito.spy(Week.class);
        Mockito.when(Week.now()).thenReturn(WEEK_10);

        //run scheduler for first time
        given(siteConfigurationService.getDataResource()).willReturn(new ClassPathResource("statsroyale_response_1.html"));
        statsRoyaleWeekendJob.run();

        //run sheduler for 2nd time
        given(siteConfigurationService.getDataResource()).willReturn(new ClassPathResource("statsroyale_response_2.html"));
        statsRoyaleWeekendJob.run();

        //Week 11
        Mockito.when(Week.now()).thenReturn(WEEK_11);

        //run chest scheduler
        given(siteConfigurationService.getDataResource()).willReturn(new ClassPathResource("statsroyale_response_3.html"));
        statsRoyalChestContrJob.run();

        Map<String, PlayerOverallStats> result = restController.retrieveClanStats(1).stream().collect(Collectors.toMap(PlayerOverallStats::getName, Function.identity()));
        //happy path
        assertDonationAndConrubution(result.get("yatagarasu"), 20, 3);
        //kots13 has left during the cc and came back
        assertDonationAndConrubution(result.get("kots13"), 20, 2);
        //thanosGR something went wrong in 2nd call of the weekend job and had less crowns , donations unexpectedly
        assertDonationAndConrubution(result.get("thanosGr"), 50,  40);
        //kirakos vins joined after the cc started - and mondays contr is bigger but only saterdays should be takien into account
        assertDonationAndConrubution(result.get("Kiriakos Vins"), 70, 0);
        //paris z left during cc and never joined back
        assertDonationAndConrubution(result.get("paris z"), 5, 26);
        //chrisXD was promoted during the cc
        Assert.assertEquals("the role should have been updated", "new_role", result.get("ChrisXD").getRole());

    }

    private void assertDonationAndConrubution(PlayerOverallStats playerOverallStats, int expectedDonation, int expectedContribution ){
        Assert.assertEquals(expectedDonation, (int)playerOverallStats.getCardDonation());
        Assert.assertEquals(expectedContribution, (int)playerOverallStats.getChestContribution());
    }


}
