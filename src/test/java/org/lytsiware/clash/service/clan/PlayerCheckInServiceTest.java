package org.lytsiware.clash.service.clan;


import org.junit.Assert;
import org.junit.Test;
import org.lytsiware.clash.AbstractSpringBootTest;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.player.PlayerCheckInCheckOutRepository;
import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.league.WarLeagueRepository;
import org.lytsiware.clash.domain.war.playerwarstat.CollectionPhaseStats;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStatsRepository;
import org.lytsiware.clash.domain.war.playerwarstat.WarPhaseStats;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public class PlayerCheckInServiceTest extends AbstractSpringBootTest {

    @Autowired
    PlayerCheckInService playerCheckInService;

    @Autowired
    PlayerCheckInCheckOutRepository playerCheckInCheckOutRepository;

    @Autowired
    PlayerWarStatsRepository playerWarStatsRepository;

    @Autowired
    WarLeagueRepository warLeagueRepository;

    @Test
    public void testSomething() {
        Player chris = new Player("123", "Chris", "");
        playerCheckInService.checkinPlayer(chris, LocalDateTime.of(2000, 11, 10, 11, 0));
        playerCheckInService.checkoutPlayer("123", LocalDateTime.of(2000, 11, 10, 23, 30));

        playerCheckInService.checkinPlayer(chris, LocalDateTime.of(2000, 12, 1, 11, 00));
        playerCheckInService.checkoutPlayer("123", LocalDateTime.of(2000, 12, 11, 23, 00));

        playerCheckInService.checkinPlayer(chris, LocalDateTime.now().minusHours(10));

        WarLeague warLeague = new WarLeague(LocalDateTime.of(2000, 12, 10, 21, 00));

        PlayerWarStat playerWarStat = PlayerWarStat.builder()
                .player(chris)
                .collectionPhaseStats(CollectionPhaseStats.builder()
                        .gamesPlayed(1)
                        .cardsWon(400)
                        .build())
                .warPhaseStats(WarPhaseStats.builder()
                        .gamesGranted(1)
                        .build())
                .build();
        playerWarStat.setWarLeague(warLeague);
        warLeagueRepository.save(warLeague);
        playerWarStatsRepository.save(playerWarStat);

        playerWarStatsRepository.flush();

        PlayerCheckInService.CheckInCheckoutDataDto checkInData = playerCheckInService.getCheckinCheckoutData().get(0);
        Assert.assertEquals(11, checkInData.getTotalStayingHours() /24);
        Assert.assertEquals(3, checkInData.getCheckInCheckouts().size());
        Assert.assertEquals(10, checkInData.getCheckInCheckouts().get(0).getStayingHours() / 24);
        Assert.assertEquals(0, checkInData.getCheckInCheckouts().get(1).getStayingHours() / 24);
        Assert.assertFalse(checkInData.getCheckInCheckouts().get(1).isAbandonedWar());
        Assert.assertTrue(checkInData.getCheckInCheckouts().get(0).isAbandonedWar());
        Assert.assertEquals(2, checkInData.getNumberCheckouts());

    }

}