package org.lytsiware.clash.controller;

import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.player.PlayerRepository;
import org.lytsiware.clash.domain.player.PlayerWeeklyStats;
import org.lytsiware.clash.domain.player.PlayerWeeklyStatsRepository;
import org.lytsiware.clash.dto.PlayerOverallStats;
import org.lytsiware.clash.service.ClanStatsService;
import org.lytsiware.clash.utils.DateWeekConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/clan")
public class ClanStatsController {

    Logger logger = LoggerFactory.getLogger(ClanStatsController.class);

    @Autowired
    private ClanStatsService clanStatsService;

    @Autowired
    private PlayerWeeklyStatsRepository playerWeeklyStatsRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @RequestMapping(value = "/{deltaWeek}", method = RequestMethod.GET)
    public List<PlayerOverallStats> retrieveClanStats(@PathVariable(required = false) Integer deltaWeek) {
        logger.info("START retrieveClanStats - week {}", deltaWeek);

        ArrayList<PlayerOverallStats> playerOverallStats = new ArrayList<>();

        if (deltaWeek < 1 || deltaWeek > 12) {
           deltaWeek = 1;
        }
        int week = DateWeekConverter.toWeek(LocalDate.now().minusWeeks(deltaWeek));
        return clanStatsService.retrieveClanStats(week);

    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public void test() {
        PlayerWeeklyStats playerWeeklyStats = new PlayerWeeklyStats();
        playerWeeklyStats.setCardDonation(40);
        playerWeeklyStats.setChestContribution(20);
        playerWeeklyStats.setWeek(DateWeekConverter.toWeek(LocalDate.now().minusWeeks(1)));
        Player player = new Player("Tag#1", "Vins", 50.4, 60.8);
        playerWeeklyStats.setPlayer(player);
        playerWeeklyStatsRepository.saveOrUpdate(playerWeeklyStats);

    }




    @RequestMapping(value = "/test2", method = RequestMethod.GET)
    public void test2(){
        Map<String, Player> results = playerRepository.loadAll();
        System.out.println(results);
    }

    @RequestMapping(value = "/test3", method = RequestMethod.GET)
    public void test3(){
        clanStatsService.updateDatabaseWithLatest();
    }



}
