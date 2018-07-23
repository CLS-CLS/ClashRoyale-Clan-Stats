package org.lytsiware.clash.service.clan;


import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.domain.player.*;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class PlayerInOutServiceImpl {

    @Autowired
    PlayerInOutRepository playerInOutRepository;

    @Autowired
    PlayerInOutHistoryRepository playerInOutHistoryRepository;

    @Autowired
    PlayerRepository playerRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public void checkoutPlayer(String tag) {
        log.info("Checking out player {}", tag);
        PlayerInOut playerInOut = playerInOutRepository.findByTag(tag).get();
        if (playerInOut.getCheckOut() == null) {
            playerInOut.setCheckOut(LocalDate.now());
            playerInOutRepository.save(playerInOut);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void checkinPlayer(String tag) {
        PlayerInOut playerInOut = playerInOutRepository.findByTag(tag).orElse(null);
        if (playerInOut != null) {
            if (playerInOut.getCheckOut() == null) {
                log.info("Player {} already in clan", tag);
                return;
            }

            PlayerInOutHistory playerInOutHistory = PlayerInOutHistory.from(playerInOut);
            playerInOutHistoryRepository.save(playerInOutHistory);

            playerInOut.setCheckIn(LocalDate.now());
            playerInOut.setCheckOut(null);
        } else {
            playerInOut = new PlayerInOut(tag, LocalDate.now());
        }
        playerInOutRepository.save(playerInOut);

    }

    @Transactional
    public void markPlayersInClan(List<PlayerWeeklyStats> currentPlayersStats) {
        List<Player> currentPlayers = currentPlayersStats.stream().map(PlayerWeeklyStats::getPlayer).collect(Collectors.toList());
        currentPlayers.stream().map(Player::getTag).forEach(this::checkinPlayer);
        currentPlayers.forEach(player -> player.setInClan(true));

        Map<String, Player> checkoutPlayers = playerRepository.loadAll();
        currentPlayers.stream().map(Player::getTag).forEach(checkoutPlayers::remove);
        checkoutPlayers.values().forEach(player -> player.setInClan(false));
        checkoutPlayers.keySet().forEach(this::checkoutPlayer);

        playerRepository.saveOrUpdate(Stream.concat(currentPlayers.stream(),
                checkoutPlayers.values().stream()).collect(Collectors.toList()));
    }


}
