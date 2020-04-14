package org.lytsiware.clash.runners;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.domain.player.PlayerCheckInCheckOutRepository;
import org.lytsiware.clash.domain.player.PlayerInOutHistoryRepository;
import org.lytsiware.clash.domain.war.league.WarLeagueRepository;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * calculates and sets the abandon war status on the checkinOut entity
 */
@Slf4j
public class PlayerCheckInOutAbandonRunner implements CommandLineRunner {

    @Autowired
    private WarLeagueRepository warLeagueRepository;

    @Autowired
    private PlayerCheckInCheckOutRepository playerCheckInCheckOutRepository;

    @Autowired
    private PlayerInOutHistoryRepository playerInOutHistoryRepository;

    @Autowired
    private PlayerWarStatsRepository playerWarStatsRepository;

    private boolean hasAbandonedWar(String tag, LocalDateTime checkIn, LocalDateTime checkOut) {
        return playerWarStatsRepository.findBetweenDatesForPlayer(tag, checkIn.toLocalDate(), checkOut.toLocalDate()).stream().findFirst()
                .filter(playerWarStat -> playerWarStat.getWarLeague().getEndDate().isAfter(checkOut))
                .map(playerWarStat -> playerWarStat.hasParticipated() && playerWarStat.getWarPhaseStats().getGamesNotPlayed() > 0)
                .orElse(false);
    }

    @Override
    public void run(String... args) {
        playerCheckInCheckOutRepository.findAll().stream()
                .filter(cico -> cico.getCheckOut() != null)
                .map(cico -> {
                    cico.setAbandonedWar(hasAbandonedWar(cico.getTag(), cico.getCheckIn(), cico.getCheckOut()));
                    return cico;
                }).collect(Collectors.toList()).forEach(playerCheckInCheckOutRepository::save);

        playerInOutHistoryRepository.findAll().stream()
                .filter(cico -> cico.getCheckOut() != null)
                .map(cico -> {
                    cico.setAbandonedWar(hasAbandonedWar(cico.getTag(), cico.getCheckIn(), cico.getCheckOut()));
                    return cico;
                }).collect(Collectors.toList()).forEach(playerInOutHistoryRepository::save);
    }
}
