package org.lytsiware.clash.service.clan;


import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.domain.player.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
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
    private PlayerCheckInCheckOutRepository playerCheckInCheckOutRepository;

    @Autowired
    private PlayerInOutHistoryRepository playerInOutHistoryRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public void checkoutPlayer(String tag, @Nullable LocalDateTime checkoutOutDate) {
        log.info("Checking out player {}", tag);
        PlayerInOut playerInOut = playerCheckInCheckOutRepository.findByTag(tag).orElse(null);
        checkoutPlayer(playerInOut, checkoutOutDate);
    }

    private void checkoutPlayer(PlayerInOut playerInOut, LocalDateTime checkoutDate) {
        checkoutDate = (checkoutDate == null ? LocalDateTime.now() : checkoutDate);
        if (playerInOut != null) {
            if (playerInOut.getCheckOut() == null) {
                playerInOut.setCheckOut(checkoutDate);
                playerCheckInCheckOutRepository.save(playerInOut);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void checkinPlayer(String tag, @Nullable LocalDateTime checkInTime) {
        checkInTime = (checkInTime == null ? LocalDateTime.now() : checkInTime);
        PlayerInOut playerInOut = playerCheckInCheckOutRepository.findByTag(tag).orElse(null);
        checkinPlayer(playerInOut, tag, checkInTime);
    }

    private void checkinPlayer(PlayerInOut playerInOut, String tag, LocalDateTime checkInDate) {
        if (playerInOut != null) {
            if (playerInOut.getCheckOut() == null) {
                log.info("Player {} already in clan", tag);
                return;
            }

            PlayerInOutHistory playerInOutHistory = PlayerInOutHistory.from(playerInOut);
            playerInOutHistoryRepository.save(playerInOutHistory);

            playerInOut.setCheckIn(checkInDate);
            playerInOut.setCheckOut(null);
        } else {
            playerInOut = new PlayerInOut(tag, checkInDate);
        }
        playerCheckInCheckOutRepository.save(playerInOut);
    }

    @Transactional
    public void markPlayersInClan(List<Player> currentPlayers) {
        Map<String, PlayerInOut> playersInOutByTag = playerCheckInCheckOutRepository.findAll().stream().collect(Collectors.toMap(PlayerInOut::getTag, Function.identity()));

        Map<String, Player> checkoutPlayers = playerRepository.loadAll();

        for (Player player : currentPlayers) {
            String playerTag = player.getTag();
            checkinPlayer(playersInOutByTag.get(playerTag), playerTag, null);
            player.setInClan(true);
            checkoutPlayers.remove(player.getTag());
        }

        for (String checkoutPlayerTag : checkoutPlayers.keySet()) {
            checkoutPlayers.get(checkoutPlayerTag).setInClan(false);
            checkoutPlayer(playersInOutByTag.get(checkoutPlayerTag), null);
        }

        playerRepository.saveOrUpdate(Stream.concat(currentPlayers.stream(),
                checkoutPlayers.values().stream()).collect(Collectors.toList()));
    }

    public LocalDateTime getFirstCheckInForPlayer(String tag) {
        List<PlayerInOutHistory> historyResult = playerInOutHistoryRepository.findByTagOrderByCheckInDesc(tag);
        if (historyResult.isEmpty()) {
            Optional<PlayerInOut> currentResult = playerCheckInCheckOutRepository.findByTag(tag);
            return currentResult.get().getCheckIn();
        } else {
            return historyResult.get(0).getCheckIn();
        }
    }

    public List<PlayerInOut> findCheckedInPlayersAtDate(LocalDateTime date) {
        return playerCheckInCheckOutRepository.findPlayersInClanAtDate(date);
    }

    public List<PlayerInOut> findAllCheckInCheckOut(String tag) {
        List<PlayerInOut> historic = playerInOutHistoryRepository.findByTagOrderByCheckInDesc(tag)
                .stream().map(h -> new PlayerInOut(tag, h.getCheckIn(), h.getCheckOut())).collect(Collectors.toList());
        playerCheckInCheckOutRepository.findByTag(tag).ifPresent(c -> historic.add(0, c));
        return historic;

    }
}
