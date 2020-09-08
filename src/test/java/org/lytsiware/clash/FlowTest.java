package org.lytsiware.clash;

import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.lytsiware.clash.core.service.integration.SiteConfigurationService;
import org.lytsiware.clash.donation.controller.ClanStatsRestController;
import org.lytsiware.clash.donation.dto.PlayerOverallStats;
import org.lytsiware.clash.donation.service.job.StatsRoyaleWeekendJobImpl;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest(Week.class)
@PowerMockIgnore("javax.management.*")
public class FlowTest extends AbstractSpringBootTest {

    private static final Week WEEK_10 = Week.fromWeek(10);
    private static final Week WEEK_11 = Week.fromWeek(11);

    @MockBean
    private SiteConfigurationService siteConfigurationService;


    @Autowired
    private StatsRoyaleWeekendJobImpl statsRoyaleWeekendJob;

    @Autowired
    private ClanStatsRestController restController;

    @Before
    public void initMocks(){
        Mockito.when(siteConfigurationService.getRefreshUrl()).thenReturn("");
    }

    /**
    @Test
    public void runJobAndShowClanStats() throws MalformedURLException {

        //Week 10
        PowerMockito.spy(Week.class);
        Mockito.when(Week.now()).thenReturn(WEEK_10);

        given(siteConfigurationService.getDeckshopClanUrlResource()).willReturn(new ClassPathResource("deckshop_response_1.html"));
        //run scheduler for first time
        given(siteConfigurationService.getDataResource()).willReturn(new ClassPathResource("statsroyale_response_1.html"));
        statsRoyaleWeekendJob.run();

        //run scheduler for 2nd time
        given(siteConfigurationService.getDeckshopClanUrlResource()).willReturn(new ClassPathResource("deckshop_response_1.html"));
        given(siteConfigurationService.getDataResource()).willReturn(new ClassPathResource("statsroyale_response_2.html"));
        statsRoyaleWeekendJob.run();

        //run scheduler for 3rd time
        given(siteConfigurationService.getDeckshopClanUrlResource()).willThrow(new ParseException("exception in deckshop parsing"));
        given(siteConfigurationService.getDataResource()).willReturn(new ClassPathResource("statsroyale_response_2_2.html"));
        statsRoyaleWeekendJob.run();

        //Week 11
        Mockito.when(Week.now()).thenReturn(WEEK_11);

        //run chest scheduler
        given(siteConfigurationService.getDataResource()).willReturn(new ClassPathResource("statsroyale_response_3.html"));
        statsRoyalChestContrJob.run();

        Map<String, PlayerOverallStats> result = restController.retrieveClanStats(1).stream().collect(Collectors.toMap(PlayerOverallStats::getName, Function.identity()));
        //happy path
        assertDonation(result.get("yatagarasu"), 30, null);
        //kots13 has left during the cc and came back
        assertDonation(result.get("kots13"), 20, 160);
        //thanosGR something went wrong in 2nd call of the weekend job and had less crowns , donations unexpectedly
        assertDonation(result.get("thanosGr"), 50, 80);
        //kirakos vins joined after the cc started - and mondays contr is bigger but only saturdays should be taken into account
        assertDonation(result.get("Kiriakos Vins"), 70, 80);
        //paris z left during cc and never joined back
        assertDonation(result.get("paris z"), 5, 200);
        //chrisXD was promoted during the cc
        Assert.assertEquals("the role should have been updated", "new_role", result.get("ChrisXD").getRole());

    }*/

    private void assertDonation(PlayerOverallStats playerOverallStats, int expectedDonation, Integer expectedReceived) {
        Assert.assertEquals(expectedDonation, (int)playerOverallStats.getCardDonation());
        Assert.assertEquals(expectedReceived, playerOverallStats.getCardsReceived());
    }


}
