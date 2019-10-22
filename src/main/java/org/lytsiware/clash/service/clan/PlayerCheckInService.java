package org.lytsiware.clash.service.clan;


import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.domain.player.*;
import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.league.WarLeagueRepository;
import org.lytsiware.clash.service.war.PlayerWarStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lytsiware.clash.service.clan.PlayerCheckInService.CheckInCheckoutDataDto.CheckInCheckOutDto;

@Service
@Slf4j
public class PlayerCheckInService {

    @Autowired
    private PlayerCheckInCheckOutRepository playerCheckInCheckOutRepository;

    @Autowired
    private PlayerInOutHistoryRepository playerInOutHistoryRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerWarStatsService warStatsService;

    @Autowired
    private WarLeagueRepository warLeagueRepository;

    /**
     * returns all the checkin checkout dates of the players that have been in clan with extra information about
     * the total weeks that were in clan and the total weeks between the latest checkin / checkout (or the current date if
     * he is still in clan).
     *
     * @return
     */
    public List<CheckInCheckoutDataDto> getCheckinCheckoutData() {
        return playerRepository.loadAll().values().stream()
                .map(player -> {
                    List<PlayerInOut> checkInCheckouts = this.findAllCheckInCheckOut(player.getTag());

                    List<CheckInCheckoutDataDto.CheckInCheckOutDto> cicos = checkInCheckouts.stream()
                            .map(cico -> CheckInCheckOutDto.builder()
                                    .checkIn(cico.getCheckIn())
                                    .checkOut(cico.getCheckOut())
                                    .stayingHours((int) cico.getCheckIn().until(Optional.ofNullable(cico.getCheckOut()).orElse(LocalDateTime.now()), ChronoUnit.HOURS))
                                    .abandonedWar(cico.getCheckOut() != null ? hasAbandonedWar(cico.getTag(), cico.getCheckIn(), cico.getCheckOut()) : false)
                                    .build())
                            .collect(Collectors.toList());

                    return CheckInCheckoutDataDto.builder()
                            .tag(player.getTag())
                            .name(player.getName())
                            .latestPeriodStayingHours(cicos.get(0).getStayingHours())
                            .latestCheckin(cicos.get(0).getCheckIn())
                            .latestCheckout(cicos.get(0).getCheckOut())
                            .maxPeriodStayingHours(cicos.stream().map(CheckInCheckOutDto::getStayingHours).reduce(0, Integer::max))
                            .totalStayingHours(cicos.stream().mapToLong(CheckInCheckOutDto::getStayingHours).sum())
                            .numberCheckouts(cicos.get(0).getCheckOut() == null ? cicos.size() - 1 : cicos.size())
                            .checkInCheckouts(cicos)
                            .build();
                }).collect(Collectors.toList());
    }

    private boolean hasAbandonedWar(String tag, LocalDateTime checkIn, LocalDateTime checkOut) {
        return warLeagueRepository.findAllBetweenStartDateEagerFetchPlayerStats(checkOut.minusDays(2).toLocalDate(), checkOut.toLocalDate())
                .stream()
                .reduce((w1, w2) -> w1.getStartDate().isAfter(w2.getStartDate()) ? w1 : w2)
                .filter(warLeague -> warLeague.getEndDate().isAfter(checkOut))
                .map(WarLeague::getPlayerWarStats)
                .orElse(new HashSet<>())
                .stream()
                .filter(playerWarStat -> playerWarStat.getPlayer().getTag().equals(tag))
                .findAny()
                .map(playerWarStat -> playerWarStat.getWarPhaseStats().hasParticipated() && playerWarStat.getWarPhaseStats().getGamesNotPlayed() > 0)
                .orElse(false);
    }

    @AllArgsConstructor
    @Setter(AccessLevel.PROTECTED)
    @Getter
    @Builder
    public static class CheckInCheckoutDataDto {
        private String tag;
        private String name;
        private LocalDateTime latestCheckin;
        private LocalDateTime latestCheckout;
        private int latestPeriodStayingHours;
        private int maxPeriodStayingHours;
        private long totalStayingHours;
        private List<CheckInCheckOutDto> checkInCheckouts;
        private int numberCheckouts;

        private boolean hasAbandonedWar() {
            return checkInCheckouts.stream().map(CheckInCheckOutDto::isAbandonedWar).reduce(Boolean::logicalOr).orElse(false);
        }

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


    @Transactional(propagation = Propagation.REQUIRED)
    public void checkoutPlayer(String tag, @Nullable LocalDateTime checkoutOutDate) {
        log.info("Checking out player {}", tag);
        PlayerInOut playerInOut = playerCheckInCheckOutRepository.findByTag(tag).orElse(null);
        doCheckoutPlayer(playerInOut, checkoutOutDate);
    }

    private void doCheckoutPlayer(PlayerInOut playerInOut, LocalDateTime checkoutDate) {
        checkoutDate = (checkoutDate == null ? LocalDateTime.now() : checkoutDate);
        if (playerInOut != null) {
            if (playerInOut.getCheckOut() == null) {
                playerInOut.setCheckOut(checkoutDate);
                playerCheckInCheckOutRepository.save(playerInOut);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void checkinPlayer(Player player, @Nullable LocalDateTime checkInTime) {
        Player playerDb = playerRepository.findByTag(player.getTag());
        if (playerDb == null) {
            playerRepository.saveOrUpdate(new Player(player.getTag(), player.getName(), "member"));
        }
        PlayerInOut playerInOut = playerCheckInCheckOutRepository.findByTag(player.getTag()).orElse(null);
        doCheckinPlayer(playerInOut, player.getTag(), checkInTime);
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
                .stream().map(h -> new PlayerInOut(tag, h.getCheckIn(), h.getCheckOut())).collect(Collectors.toList());
        playerCheckInCheckOutRepository.findByTag(tag).ifPresent(c -> historic.add(0, c));
        return historic;

    }
}
