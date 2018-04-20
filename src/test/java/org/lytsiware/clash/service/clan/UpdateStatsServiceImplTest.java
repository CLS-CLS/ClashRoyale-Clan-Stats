package org.lytsiware.clash.service.clan;

import org.junit.Test;
import org.lytsiware.clash.AbstractSpringBootTest;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.player.PlayerRepository;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.service.UpdateStatService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UpdateStatsServiceImplTest extends AbstractSpringBootTest {

    @Autowired
    private UpdateStatService updateStatsService;

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    @Transactional
    public void markPlayerIsInClan() throws Exception {
        Player player1 = new Player("1", "123", "123");
        Player player2 = new Player("2", "123", "123");
        Player player3 = new Player("3", "123", "123");
        PlayerWeeklyStats pws1 = PlayerWeeklyStats.builder().withWeek(Week.now().getWeek()).withPlayer(player1).build();
        PlayerWeeklyStats pws2 = PlayerWeeklyStats.builder().withWeek(Week.now().getWeek()).withPlayer(player2).build();
        em.persist(player1);
        em.persist(player2);
        em.persist(player3);

        List<PlayerWeeklyStats> playerInClan = Arrays.asList(pws1, pws2);

        updateStatsService.markPlayerIsInClan(playerInClan);

        Map<String, Player> players = playerRepository.loadAll();
        assertTrue(players.get("1").getInClan());
        assertTrue(players.get("2").getInClan());
        assertFalse(players.get("3").getInClan());
    }

}