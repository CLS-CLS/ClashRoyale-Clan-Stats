package org.lytsiware.clash.service.war;

import org.junit.Assert;
import org.junit.Test;
import org.lytsiware.clash.AbstractSpringBootTest;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStats;
import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.playerwarstat.CollectionPhaseStats;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStatsRepository;
import org.lytsiware.clash.domain.war.playerwarstat.WarPhaseStats;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Transactional
public class PlayerAggregationWarStatsServiceTest extends AbstractSpringBootTest {

    @Autowired
    PlayerWarStatsRepository playerWarStatsRepository;

    @Autowired
    PlayerAggregationWarStatsService playerAggregationWarStatsService;

    @Test
    public void calculateStats() throws Exception {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            String name = "player" + i;
            Player player = new Player(name, name, name);
            em.persist(player);
            players.add(player);
        }

        for (int i = 0; i < 12; i++) {
            WarLeague warLeague = new WarLeague(LocalDate.now().minusDays((11 - i) * 2).atStartOfDay());
            em.persist(warLeague);
            for (int j = 0; j < players.size(); j++) {

                int cardsWon = 10 * (i + 1) * (j + 1) * ((i + j) % 3);
                int gamesGranted = ((i + j) % 3 == 0 ? 0 : 1);
                int gamesWon = ((i + j) % 2) * (cardsWon == 0 ? 0 : 1);
                int gamesLost = gamesGranted - gamesWon;
                PlayerWarStat pws = PlayerWarStat.builder().warEligible(true)
                        .player(players.get(j))
                        .collectionPhaseStats(CollectionPhaseStats.builder()
                                .cardsWon(cardsWon)
                                .build())
                        .warPhaseStats(WarPhaseStats.builder()
                                .gamesWon(gamesWon)
                                .gamesLost(gamesLost)
                                .gamesGranted(gamesGranted)
                                .build())
                        .build();
                pws.setWarLeague(warLeague);
                em.persist(pws);
            }
        }

        List<PlayerWarStat> pwsList = playerWarStatsRepository.findAll();
        Comparator<PlayerWarStat> comparing = Comparator.comparing((PlayerWarStat pws) -> pws.getPlayer().getName()).thenComparing(pws -> pws.getWarLeague().getId());
        pwsList.sort(comparing);
        pwsList.forEach(pws -> System.out.println(pws.getPlayer().getTag() + ", "
                + pws.getWarLeague().getId() + ", "
                + pws.getCollectionPhaseStats().getCardsWon() + ", "
                + pws.getWarPhaseStats().getGamesWon()));


        List<PlayerAggregationWarStats> result = playerAggregationWarStatsService.calculateStats(LocalDate.now(), 5, true);
        result.forEach(aggr -> System.out.println(aggr.getPlayer().getName() + ", " + aggr.getDate().format(DateTimeFormatter.ofPattern("dd-MM")) + aggr.getAvgCards()));

        PlayerAggregationWarStats player0AggrStat = findByPlayer(result, "player0");
        Assert.assertEquals(146, (int) player0AggrStat.getAvgCards());
        Assert.assertEquals(0.6D, player0AggrStat.getAvgWins(), 0);
        Assert.assertEquals(2, (int) player0AggrStat.getGamesWon());
        Assert.assertEquals(4, (int) player0AggrStat.getGamesGranted());

        Assert.assertEquals(260, (int) findByPlayer(result, "player1").getAvgCards());
//        Assert.assertEquals();


    }

    @Test
    public void calculateAndSaveStats() throws Exception {
    }

    @Test
    public void findLatestWarAggregationStatsForWeek() throws Exception {
    }

    private PlayerAggregationWarStats findByPlayer(List<PlayerAggregationWarStats> playerWarStats, String name) {
        return playerWarStats.stream().filter(pws -> pws.getPlayer().getTag().equals(name) && pws.getDate().isEqual(LocalDate.now())).findFirst().get();
    }

}