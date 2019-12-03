package org.lytsiware.clash.runners;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.*;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
//@Component
public class PlayerCheckInOutMigrationRunner implements CommandLineRunner {


    @Autowired
    PlayerWeeklyStatsRepository playerWeeklyStatsRepository;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    PlayerCheckInCheckOutRepository playerCheckInCheckOutRepository;

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
                PlayerInOut playerInOut = playerCheckInCheckOutRepository.findByTag(tag).orElse(null);
                if (playerInOut == null) {
                    playerInOut = new PlayerInOut(null, tag, Week.ZERO_WEEK.atStartOfDay(), Week.ZERO_WEEK.atStartOfDay(), false);
                    playerCheckInCheckOutRepository.save(playerInOut);
                }
            }
        }
    }

    private void migrate(int weekNumber, Map<String, Player> allPlayers) {

        Set<String> currentWeekTags = playerWeeklyStatsRepository.findByWeek(Week.fromWeek(weekNumber)).stream()
                .map(pws -> pws.getPlayer().getTag()).collect(Collectors.toSet());
        for (String tag : allPlayers.keySet()) {
            PlayerInOut playerInOut = playerCheckInCheckOutRepository.findByTag(tag).orElse(null);
            if (currentWeekTags.contains(tag)) {
                if (playerInOut != null) {
                    if (playerInOut.getCheckOut() == null) {
                        log.info("Player {} already in clan", tag);
                        continue;
                    }

                    PlayerInOutHistory playerInOutHistory = PlayerInOutHistory.from(playerInOut);
                    playerInOutHistoryRepository.save(playerInOutHistory);

                    playerInOut.setCheckIn(LocalDate.from(Week.fromWeek(weekNumber).getStartDate()).atStartOfDay());
                    playerInOut.setCheckOut(null);
                } else {
                    log.info("Checking in new player {} at week {}", tag, weekNumber);
                    playerInOut = new PlayerInOut(tag, Week.fromWeek(weekNumber).getStartDate().atStartOfDay());
                }
                playerCheckInCheckOutRepository.save(playerInOut);
            } else if (playerInOut != null && playerInOut.getCheckOut() == null) {
                log.info("Checking OUT player {} at week {}", tag, weekNumber);
                playerInOut.setCheckOut(Week.fromWeek(weekNumber).getStartDate().atStartOfDay());
                playerCheckInCheckOutRepository.save(playerInOut);
            }
        }

    }
}
