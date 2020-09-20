package org.lytsiware.clash.donation.service;


import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.core.domain.player.*;
import org.lytsiware.clash.war.domain.league.WarLeagueRepository;
import org.lytsiware.clash.war.domain.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.war.domain.playerwarstat.PlayerWarStatsRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lytsiware.clash.donation.service.PlayerCheckInService.CheckInCheckoutDataDto.CheckInCheckOutDto;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlayerCheckInService {

    private final PlayerCheckInCheckOutRepository playerCheckInCheckOutRepository;

    private final PlayerInOutHistoryRepository playerInOutHistoryRepository;

    private final PlayerRepository playerRepository;

    private final WarLeagueRepository warLeagueRepository;

    private final PlayerWarStatsRepository playerWarStatsRepository;


    /**
     * returns all the checkin checkout dates of the players that have been in clan with extra information about
     * the total weeks that were in clan and the total weeks between the latest checkin / checkout (or the current date if
     * he is still in clan).
     */
    public List<CheckInCheckoutDataDto> getCheckinCheckoutData() {
        Map<String, Player> players = playerRepository.loadAll();
        return this.findAllCheckInCheckOut().entrySet().stream().map(entry -> {

            List<CheckInCheckoutDataDto.CheckInCheckOutDto> cicos = entry.getValue().stream()
                    .map(cico -> CheckInCheckOutDto.builder()
                            .checkIn(cico.getCheckIn())
                            .checkOut(cico.getCheckOut())
                            .stayingHours((int) cico.getCheckIn().until(Optional.ofNullable(cico.getCheckOut()).orElse(LocalDateTime.now()), ChronoUnit.HOURS))
                            .abandonedWar(cico.hasAbandonedWar())
                            .build())
                    .collect(Collectors.toList());

            return CheckInCheckoutDataDto.builder()
                    .tag(entry.getKey())
                    .name(players.get(entry.getKey()).getName())
                    .firstJoined(cicos.get(cicos.size() - 1).getCheckIn())
                    .latestJoin(cicos.get(0).getCheckIn())
                    .totalStayingHours(cicos.stream().mapToLong(CheckInCheckOutDto::getStayingHours).sum())
                    .numberCheckouts(cicos.get(0).getCheckOut() == null ? cicos.size() - 1 : cicos.size())
                    .abandonedWar(cicos.stream().map(CheckInCheckOutDto::isAbandonedWar).reduce(Boolean::logicalOr).orElse(false))
                    .checkInCheckouts(cicos)
                    .inClan(cicos.get(0).getCheckOut() == null)
                    .build();
        }).collect(Collectors.toList());
    }


    private boolean hasAbandonedWar(String tag, LocalDateTime checkIn, LocalDateTime checkOut) {
        log.info("Checking if player  {} has abandoned war at checkout date {}", tag, checkOut);

        Optional<PlayerWarStat> latestWarStat = playerWarStatsRepository.findBetweenDatesForPlayer(tag, checkIn.toLocalDate(),
                checkOut.toLocalDate()).stream().findFirst();

        if (latestWarStat.isPresent()) {
            log.info("Latest recorder war stat is {}", latestWarStat.get().getId());
            latestWarStat = latestWarStat.filter(playerWarStat -> playerWarStat.getWarLeague().getEndDate().isAfter(checkOut));
            if (latestWarStat.isPresent()) {
                log.info("Player left before completion of war");
                if (latestWarStat.get().hasAbandonedWar()) {
                    log.info("and HAS ABANDONED WAR");
                    return true;
                }
            }
        }
        return false;

    }


    @Transactional(propagation = Propagation.REQUIRED)
    public void checkoutPlayer(String tag, @Nullable LocalDateTime checkoutOutDate) {
        log.info("Checking out player {} at datetime {}", tag, checkoutOutDate);
        PlayerInOut playerInOut = playerCheckInCheckOutRepository.findByTag(tag).orElse(null);
        doCheckoutPlayer(playerInOut, checkoutOutDate);
    }

    private void doCheckoutPlayer(PlayerInOut playerInOut, LocalDateTime checkoutDate) {
        //WAR IS OVER
//        checkoutDate = (checkoutDate == null ? LocalDateTime.now() : checkoutDate);
//        if (playerInOut != null) {
//            if (playerInOut.getCheckOut() == null) {
//                playerInOut.setCheckOut(checkoutDate);
//                playerInOut.setAbandonedWar(hasAbandonedWar(playerInOut.getTag(), playerInOut.getCheckIn(), playerInOut.getCheckOut()));
//                playerCheckInCheckOutRepository.save(playerInOut);
//            }
//        }
    }

    /**
     * Helper method to check in players.
     * Use this method to limit queries in the database instead of calling multiple times
     * the {@link PlayerCheckInService#checkinPlayer(Player, LocalDateTime)}.
     *
     * @return in all cases returns the attached entity players
     * @implNote The method loads all players (and their checkins) at once instead of loading player by tag
     */
    public List<Player> checkinPlayers(List<Player> players, @Nullable LocalDateTime checkInTime) {
        Map<String, Player> allPlayers = playerRepository.loadAll();
        Map<String, PlayerInOut> allCheckins = playerCheckInCheckOutRepository.findAll().stream()
                .collect(Collectors.toMap(PlayerInOut::getTag, Function.identity()));

        List<Player> playersDb = players.stream().map(p ->
                allPlayers.get(p) == null ? playerRepository.persist(p) : allPlayers.get(p))
                .collect(Collectors.toList());

        playersDb.stream().forEach(p -> doCheckinPlayer(allCheckins.get(p.getTag()), p.getTag(), checkInTime));
        return playersDb;

    }

    /**
     * Checks in the player if is needed to be checked in
     *
     * @return in all cases returns the attached entity player
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Player checkinPlayer(Player player, @Nullable LocalDateTime checkInTime) {
        Player playerDb = playerRepository.findByTag(player.getTag());
        if (playerDb == null) {
            playerDb = new Player(player.getTag(), player.getName(), "member");
            playerRepository.persist(playerDb);
        }
        PlayerInOut playerInOut = playerCheckInCheckOutRepository.findByTag(player.getTag()).orElse(null);
        doCheckinPlayer(playerInOut, player.getTag(), checkInTime);
        return playerDb;
    }

    private void doCheckinPlayer(PlayerInOut playerInOut, String tag, LocalDateTime checkInDate) {
        log.info("Check in player {}", tag);
        if (checkInDate == null) {
            checkInDate = LocalDateTime.now();
        }
        if (playerInOut != null) {
            if (playerInOut.getCheckOut() == null) {
                log.info(" -- already in clan");
                return;
            }
            PlayerInOutHistory playerInOutHistory = PlayerInOutHistory.from(playerInOut);
            playerInOutHistoryRepository.save(playerInOutHistory);

            playerInOut.setCheckIn(checkInDate);
            playerInOut.setCheckOut(null);
            playerInOut.setAbandonedWar(false);
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
            doCheckinPlayer(playersInOutByTag.get(playerTag), playerTag, LocalDateTime.now());
            player.setInClan(true);
            checkoutPlayers.remove(player.getTag());
        }

        for (String checkoutPlayerTag : checkoutPlayers.keySet()) {
            checkoutPlayers.get(checkoutPlayerTag).setInClan(false);
            doCheckoutPlayer(playersInOutByTag.get(checkoutPlayerTag), null);
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
                .stream().map(h -> new PlayerInOut(tag, h.getCheckIn(), h.getCheckOut(), h.hasAbandonedWar())).collect(Collectors.toList());
        playerCheckInCheckOutRepository.findByTag(tag).ifPresent(c -> historic.add(0, c));
        return historic;
    }

    public Map<String, List<PlayerInOut>> findAllCheckInCheckOut() {
        return Stream.concat
                (
                        playerCheckInCheckOutRepository.findAll().stream(),
                        playerInOutHistoryRepository.findAll().stream()
                                .map(h -> new PlayerInOut(h.getTag(), h.getCheckIn(), h.getCheckOut(), h.hasAbandonedWar()))
                )
                .sorted(Comparator.comparing(PlayerInOut::getCheckIn).reversed())
                .collect(Collectors.groupingBy(PlayerInOut::getTag));
    }


    @AllArgsConstructor
    @Setter(AccessLevel.PROTECTED)
    @Getter
    @Builder
    public static class CheckInCheckoutDataDto {
        private String tag;
        private String name;
        private LocalDateTime firstJoined;
        private LocalDateTime latestJoin;
        private long totalStayingHours;
        private List<CheckInCheckOutDto> checkInCheckouts;
        private int numberCheckouts;
        private boolean abandonedWar;
        private boolean inClan;


        @AllArgsConstructor
        @NoArgsConstructor
        @Builder
        @Getter
        @Setter(AccessLevel.PROTECTED)
        public static class CheckInCheckOutDto {
            private LocalDateTime checkIn;
            private LocalDateTime checkOut;
            private int stayingHours;
            private boolean abandonedWar;
        }
    }


}
