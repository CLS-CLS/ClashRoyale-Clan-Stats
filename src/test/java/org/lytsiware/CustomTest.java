package org.lytsiware;


import org.lytsiware.clash.Application;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.player.PlayerRepository;
import org.lytsiware.clash.domain.player.PlayerWeeklyStats;
import org.lytsiware.clash.domain.player.PlayerWeeklyStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.util.Map;

//@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
public class CustomTest {

    @Autowired
    private PlayerWeeklyStatsRepository playerWeeklyStatsRepository;

    @Autowired
    private PlayerRepository playerRepository;

    //@Test
    public void sampleTestCase(){
        PlayerWeeklyStats playerWeeklyStats = new PlayerWeeklyStats();
        playerWeeklyStats.setCardDonation(40);
        playerWeeklyStats.setChestContribution(20);
        playerWeeklyStats.setWeek(new Week().previousWeek(1).getWeek());
        Player player = new Player("Tag#1", "Vins", 50.4, 60.8);
        playerWeeklyStats.setPlayer(player);
        playerWeeklyStatsRepository.saveOrUpdate(playerWeeklyStats);
    }


    //@Test
    public void test2(){
        Map<String, Player> results = playerRepository.loadAll();
        System.out.println(results);
    }


}
