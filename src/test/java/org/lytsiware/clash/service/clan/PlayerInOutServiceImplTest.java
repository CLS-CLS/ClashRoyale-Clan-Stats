package org.lytsiware.clash.service.clan;

import org.junit.Test;
import org.lytsiware.clash.AbstractSpringBootTest;
import org.lytsiware.clash.domain.player.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@Transactional
public class PlayerInOutServiceImplTest extends AbstractSpringBootTest {

    @Autowired
    PlayerCheckInService playerInOutService;

    @Autowired
    PlayerCheckInCheckOutRepository playerCheckInCheckOutRepository;

    @Autowired
    PlayerInOutHistoryRepository playerInOutHistoryRepository;

    @Autowired
    PlayerRepository playerRepository;


    @Test
    public void checkoutPlayer() throws Exception {
        Player p1 = new Player("tag1", "Chris", "Member");
        em.persist(p1);
        PlayerInOut playerInOut = new PlayerInOut("tag1", LocalDate.now().minusDays(2).atStartOfDay());
        em.persist(playerInOut);
        em.flush();
        em.clear();

        playerInOutService.checkoutPlayer("tag1", null);

        playerInOut = playerCheckInCheckOutRepository.findByTag("tag1").get();
        assertEquals(LocalDate.now().minusDays(2).atStartOfDay(), playerInOut.getCheckIn());
        assertEquals(LocalDate.now(), playerInOut.getCheckOut().toLocalDate());
    }

    @Test
    public void checkinPlayerWithCheckout() {
        LocalDate before4days = LocalDate.now().minusDays(4);
        LocalDate before2days = LocalDate.now().minusDays(2);
        Player p1 = new Player("tag1", "Chris", "Member");
        em.persist(p1);
        PlayerInOut playerInOut = new PlayerInOut(null, "tag1", before4days.atStartOfDay(), before2days.atStartOfDay());
        em.persist(playerInOut);
        em.flush();
        em.clear();

        playerInOutService.checkinPlayer("tag1", null);

        playerInOut = playerCheckInCheckOutRepository.findByTag("tag1").get();
        assertEquals(LocalDate.now(), playerInOut.getCheckIn().toLocalDate());
        assertNull(playerInOut.getCheckOut());

        PlayerInOutHistory playerInOutHistory = playerInOutHistoryRepository.findByTagOrderByCheckInDesc("tag1").get(0);
        assertEquals(before4days.atStartOfDay(), playerInOutHistory.getCheckIn());
        assertEquals(before2days, playerInOutHistory.getCheckOut().toLocalDate());
    }

    @Test
    public void checkinNewPlayer() {
        Player p1 = new Player("tag1", "Chris", "Member");
        em.persist(p1);
        em.flush();
        em.clear();

        playerInOutService.checkinPlayer("tag1", null);

        PlayerInOut playerInOut = playerCheckInCheckOutRepository.findByTag("tag1").get();
        assertEquals(LocalDate.now(), playerInOut.getCheckIn().toLocalDate());
        assertNull(playerInOut.getCheckOut());

        List<PlayerInOutHistory> playerInOutHistory = playerInOutHistoryRepository.findByTagOrderByCheckInDesc("tag1");
        assertEquals(0, playerInOutHistory.size());
    }

    @Test
    public void reCheckinExistingPlayer() {
        LocalDate before4days = LocalDate.now().minusDays(4);
        Player p1 = new Player("tag1", "Chris", "Member");
        em.persist(p1);
        PlayerInOut playerInOut = new PlayerInOut(null, "tag1", before4days.atStartOfDay(), null);
        em.persist(playerInOut);
        em.flush();
        em.clear();

        playerInOut = playerCheckInCheckOutRepository.findByTag("tag1").get();
        assertEquals(before4days.atStartOfDay(), playerInOut.getCheckIn());
        assertNull(playerInOut.getCheckOut());
        List<PlayerInOutHistory> playerInOutHistory = playerInOutHistoryRepository.findByTagOrderByCheckInDesc("tag1");
        assertEquals(0, playerInOutHistory.size());
    }


    @Test
    public void markPlayersInClan() {
        List<Player> currentPlayers = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            Player p = new Player("tag" + i, "", "");
            em.persist(p);
            currentPlayers.add(p);
        }

        LocalDate twoDaysAgo = LocalDate.now().minusDays(2);
        LocalDate oneDayAgo = LocalDate.now().minusDays(1);
        Player p3 = new Player("tag3", "", "");
        em.persist(p3);
        playerCheckInCheckOutRepository.save(new PlayerInOut(null, "tag3", twoDaysAgo.atStartOfDay(), null));

        Player p4 = new Player("tag4", "", "");
        em.persist(p4);
        playerCheckInCheckOutRepository.save(new PlayerInOut(null, "tag4", twoDaysAgo.atStartOfDay(), oneDayAgo.atStartOfDay()));

        playerInOutService.markPlayersInClan(currentPlayers);
        em.flush();
        em.clear();

        Map<String, Player> players = playerRepository.loadAll();
        assertTrue(players.get("tag0").getInClan());
        assertFalse(players.get("tag4").getInClan());

        assertEquals(LocalDate.now(), playerCheckInCheckOutRepository.findByTag("tag2").get().getCheckIn().toLocalDate());
        assertEquals(LocalDate.now(), playerCheckInCheckOutRepository.findByTag("tag3").get().getCheckOut().toLocalDate());
        assertEquals(oneDayAgo.atStartOfDay(), playerCheckInCheckOutRepository.findByTag("tag4").get().getCheckOut());

    }

}