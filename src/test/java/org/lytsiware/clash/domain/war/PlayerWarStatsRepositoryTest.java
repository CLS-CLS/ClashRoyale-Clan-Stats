package org.lytsiware.clash.domain.war;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.playerwarstat.CollectionPhaseStats;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.domain.war.playerwarstat.WarPhaseStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
public class PlayerWarStatsRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Test
    public void findAllByLeague() throws Exception {
        Player player = new Player("123", "Chris", "Elder", true);
        entityManager.persist(player);

        WarLeague warLeague1 = new WarLeague(LocalDateTime.now());
        warLeague1.setName("War League 1");
        warLeague1.setRank(3);

        PlayerWarStat pws = PlayerWarStat.builder()
                .warEligible(true)
                .collectionPhaseStats(CollectionPhaseStats.builder()
                        .cardsWon(400)
                        .gamesLost(1)
                        .gamesWon(1)
                        .build())
                .warPhaseStats(WarPhaseStats.builder()
                        .gamesGranted(2)
                        .gamesLost(1)
                        .gamesWon(1)
                        .build())
                .player(player)
                .warLeague(warLeague1)
                .build();

        pws = entityManager.persistAndFlush(pws);
        entityManager.clear();
        Long pwsId = pws.getId();
        WarLeague warLeague = entityManager.find(WarLeague.class, LocalDate.now());
        assertEquals("War League 1", warLeague.getName());
        assertEquals(3, (int) warLeague.getRank());


        assertEquals(400, (int) new ArrayList<>(warLeague.getPlayerWarStats()).get(0).getCollectionPhaseStats().getCardsWon());
        assertEquals(2, (int) new ArrayList<>(warLeague.getPlayerWarStats()).get(0).getWarPhaseStats().getGamesGranted());
    }


}