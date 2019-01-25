package org.lytsiware.clash.service.clan;


import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.domain.player.*;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class PlayerCheckInService {

    @Autowired
    PlayerCheckInCheckOutRepository playerCheckInCheckOutRepository;

    @Autowired
    PlayerInOutHistoryRepository playerInOutHistoryRepository;

    @Autowired
    PlayerRepository playerRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public void checkoutPlayer(String tag) {
        log.info("Checking out player {}", tag);
        PlayerInOut playerInOut = playerCheckInCheckOutRepository.findByTag(tag).orElse(null);
        checkoutPlayer(playerInOut);
    }

    private void checkoutPlayer(PlayerInOut playerInOut) {
        if (playerInOut != null) {
            if (playerInOut.getCheckOut() == null) {
                playerInOut.setCheckOut(LocalDateTime.now());
                playerCheckInCheckOutRepository.save(playerInOut);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void checkinPlayer(String tag) {
        PlayerInOut playerInOut = playerCheckInCheckOutRepository.findByTag(tag).orElse(null);
        checkinPlayer(playerInOut, tag);
    }

    private void checkinPlayer(PlayerInOut playerInOut, String tag) {
        if (playerInOut != null) {
            if (playerInOut.getCheckOut() == null) {
                log.info("Player {} already in clan", tag);
                return;
            }

            PlayerInOutHistory playerInOutHistory = PlayerInOutHistory.from(playerInOut);
            playerInOutHistoryRepository.save(playerInOutHistory);

            playerInOut.setCheckIn(LocalDateTime.now());
            playerInOut.setCheckOut(null);
        } else {
            playerInOut = new PlayerInOut(tag, LocalDateTime.now());
        }
        playerCheckInCheckOutRepository.save(playerInOut);
    }

    @Transactional
    public void markPlayersInClan(List<PlayerWeeklyStats> currentPlayersStats) {
        List<Player> currentPlayers = currentPlayersStats.stream().map(PlayerWeeklyStats::getPlayer).collect(Collectors.toList());
        Map<String, PlayerInOut> playersInOutByTag = playerCheckInCheckOutRepository.findAll().stream().collect(Collectors.toMap(PlayerInOut::getTag, Function.identity()));

        Map<String, Player> checkoutPlayers = playerRepository.loadAll();

        for (Player player : currentPlayers) {
            String playerTag = player.getTag();
            checkinPlayer(playersInOutByTag.get(playerTag), playerTag);
            player.setInClan(true);
            checkoutPlayers.remove(player.getTag());
        }

        for (String checkoutPlayerTag : checkoutPlayers.keySet()) {
            checkoutPlayers.get(checkoutPlayerTag).setInClan(false);
            checkoutPlayer(playersInOutByTag.get(checkoutPlayerTag));
        }

        playerRepository.saveOrUpdate(Stream.concat(currentPlayers.stream(),
                checkoutPlayers.values().stream()).collect(Collectors.toList()));
    }

    public LocalDateTime getFirstCheckInForPlayer(String tag){
        List<PlayerInOutHistory> historyResult = playerInOutHistoryRepository.findByTagOrderByCheckInDesc(tag);
        if (historyResult.isEmpty()) {
            Optional<PlayerInOut> currentResult = playerCheckInCheckOutRepository.findByTag(tag);
            return currentResult.get().getCheckIn();
        }else {
            return historyResult.get(0).getCheckIn();
        }
    }

    public List<PlayerInOut> findCheckedInPlayersAtDate(LocalDateTime date) {
        return playerCheckInCheckOutRepository.findCheckedInAtDate(date);
    }
}
