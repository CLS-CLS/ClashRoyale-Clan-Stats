package org.lytsiware.clash.runners;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.*;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStatsRepository;
import org.lytsiware.clash.service.ClanStatsService;
import org.lytsiware.clash.service.clan.PlayerInOutServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PlayerCheckInOutMigrationRunner implements CommandLineRunner {

    @Autowired
    ClanStatsService clanStatsService;

    @Autowired
    PlayerWeeklyStatsRepository playerWeeklyStatsRepository;

    @Autowired
    PlayerInOutServiceImpl playerInOutService;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    PlayerInOutRepository playerInOutRepository;

    @Autowired
    PlayerInOutHistoryRepository playerInOutHistoryRepository;


    @Override
    public void run(String... args) throws Exception {
        Map<String, Player> allPlayers = playerRepository.loadAll();

        if (Arrays.stream(args).peek(log::info).anyMatch(arg -> "migrate".equals(arg))) {
            for (int i = 0; i <= Week.now().getWeek(); i++) {
                migrate(i, allPlayers);
            }
            for (String tag : allPlayers.keySet()) {
                PlayerInOut playerInOut = playerInOutRepository.findByTag(tag).orElse(null);
                if (playerInOut == null) {
                    playerInOut = new PlayerInOut(null, tag, Week.ZERO_WEEK, Week.ZERO_WEEK);
                    playerInOutRepository.save(playerInOut);
                }
            }
        }
    }

    private void migrate(int weekNumber, Map<String, Player> allPlayers) {

        Set<String> currentWeekTags = playerWeeklyStatsRepository.findByWeek(Week.fromWeek(weekNumber)).stream()
                .map(pws -> pws.getPlayer().getTag()).collect(Collectors.toSet());
        for (String tag : allPlayers.keySet()) {
            PlayerInOut playerInOut = playerInOutRepository.findByTag(tag).orElse(null);
            if (currentWeekTags.contains(tag)) {
                if (playerInOut != null) {
                    if (playerInOut.getCheckOut() == null) {
                        log.info("Player {} already in clan", tag);
                        continue;
                    }

                    PlayerInOutHistory playerInOutHistory = PlayerInOutHistory.from(playerInOut);
                    playerInOutHistoryRepository.save(playerInOutHistory);

                    playerInOut.setCheckIn(LocalDate.from(Week.fromWeek(weekNumber).getStartDate()));
                    playerInOut.setCheckOut(null);
                } else {
                    log.info("Checking in new player {} at week {}", tag, weekNumber);
                    playerInOut = new PlayerInOut(tag, Week.fromWeek(weekNumber).getStartDate());
                }
                playerInOutRepository.save(playerInOut);
            } else if (playerInOut != null && playerInOut.getCheckOut() == null) {
                log.info("Checking OUT player {} at week {}", tag, weekNumber);
                playerInOut.setCheckOut(Week.fromWeek(weekNumber).getStartDate());
                playerInOutRepository.save(playerInOut);
            }
        }

    }
}
