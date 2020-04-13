package org.lytsiware.clash.service.war;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.player.PlayerCheckInCheckOutRepository;
import org.lytsiware.clash.domain.player.PlayerInOut;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStats;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStatsRepository;
import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.league.WarLeagueRepository;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStatsRepository;
import org.lytsiware.clash.domain.war.playerwarstat.WarPhaseStats;
import org.lytsiware.clash.dto.ClansWarGlobalStatsDto;
import org.lytsiware.clash.service.calculation.war.WarCalculationContext;
import org.lytsiware.clash.service.calculation.war.WarStatsCalculationService;
import org.lytsiware.clash.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PlayerAggregationWarStatsServiceImpl implements PlayerAggregationWarStatsService {

    @Autowired
    PlayerAggregationWarStatsRepository playerAggregationWarStatsRepository;

    @Autowired
    WarLeagueRepository warLeagueRepository;

    @Autowired
    PlayerWarStatsRepository playerWarStatsRepository;

    @Autowired
    PlayerCheckInCheckOutRepository playerCheckInCheckOutRepository;

    @Autowired
    WarStatsCalculationService warStatsCalculationService;



    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void calculateAndUpdateStats(LocalDate startDate, int leagueSpan) {
        WarCalculationContext warCalculationContext = collectDataForStatsCalculation(startDate, leagueSpan);
        List<PlayerAggregationWarStats> aggregationStats = warStatsCalculationService.calculateStats(warCalculationContext);
        Iterable<PlayerAggregationWarStats> iterable = playerAggregationWarStatsRepository.saveAll(aggregationStats);
    }


    @Override
    public ClansWarGlobalStatsDto getGlobalWarStats(int deltaWar) {
        log.info("START getGlobalWarStats for deltawar {}", deltaWar);
        LocalDate latestRecordedDate = warLeagueRepository.findLatestRecordedWarLeague().map(WarLeague::getStartDate).orElse(null);

        return warLeagueRepository.findNthWarLeague(deltaWar)
                .map(warLeague1 -> playerAggregationWarStatsRepository.findByDateAndLeagueSpan(warLeague1.getStartDate(), WarConstants.leagueSpan))
                .map(playerAggregationWarStats -> new ClansWarGlobalStatsDto(playerAggregationWarStats, latestRecordedDate)).orElse(null);

    }


    @Override
    public List<PlayerAggregationWarStats> getWarStatsForPlayer(String tag, int leagueSpan, LocalDate untilDate) {
        return playerAggregationWarStatsRepository.findFirst40ByPlayerTagAndLeagueSpanAndDateLessThanEqualOrderByDateDesc(tag, leagueSpan, untilDate);
    }

    @Override
    public void deleteInBatch(LocalDate startDate, int leagueSpan) {
        List<PlayerAggregationWarStats> data = playerAggregationWarStatsRepository.findByDateAndLeagueSpan(startDate, leagueSpan);
        playerAggregationWarStatsRepository.deleteInBatch(data);
    }


    private List<PlayerAggregationWarStats> calculateStats(List<WarLeague> warLeagues, LocalDateTime date, int leagueSpan) {

        // players that have left the clan will have aggregation stats if we don't filter them out
        Set<String> playersInClanAtDate = playerCheckInCheckOutRepository.findPlayersInClanAtDate(date).stream().map(PlayerInOut::getTag).collect(Collectors.toSet());

        // still a player can join the clan after the war has begun and be able to join the war (though he will not appear in playerInClanAtDate). Also add those
        // who joined the war
        warLeagues.stream().findFirst().ifPresent(wl ->
                playersInClanAtDate.addAll(wl.getPlayerWarStats().stream()
                        .filter(ws -> ws.getCollectionPhaseStats().getGamesPlayed() > 0)
                        .map((ws -> ws.getPlayer().getTag()))
                        .collect(Collectors.toSet()))
        );

        Map<Player, List<PlayerWarStat>> warStatsPerPlayer = warLeagues.stream()
                .flatMap(warLeague -> warLeague.getPlayerWarStats().stream())
                .filter(playerWarStat -> playersInClanAtDate.contains(playerWarStat.getPlayer().getTag()))
                .collect(Utils.collectToMapOfLists(PlayerWarStat::getPlayer, Function.identity()));

        List<PlayerAggregationWarStats> playerAggregationWarStats = new ArrayList<>();

        for (List<PlayerWarStat> playerWarStats : warStatsPerPlayer.values()) {

            int numberOfWars = playerWarStats.stream().map(PlayerWarStat::getWarLeague).collect(Collectors.toSet()).size();
            List<PlayerWarStat> participatedWars = playerWarStats.stream()
                    .filter(pws -> pws.getCollectionPhaseStats().getCardsWon() != 0).collect(Collectors.toList());
            int numberOfWarsParticipated = participatedWars.size();
            int gamesGranted = participatedWars.stream().filter(pws -> pws.getWarPhaseStats() != null).mapToInt(pws -> pws.getWarPhaseStats().getGamesGranted()).sum();
            int totalCards = playerWarStats.stream().mapToInt(pws -> pws.getCollectionPhaseStats().getCardsWon())
                    .filter(i -> i != 0).sum();
            int wins = participatedWars.stream().filter(pws -> pws.getWarPhaseStats() != null).mapToInt(pws -> pws.getWarPhaseStats().getGamesWon()).sum();
            int gamesNotPlayed = participatedWars.stream().filter(pws -> Optional.ofNullable(pws.getWarPhaseStats()).map(WarPhaseStats::getGamesNotPlayed).isPresent())
                    .mapToInt(pws -> pws.getWarPhaseStats().getGamesNotPlayed()).sum();
            int crownsLost = gamesNotPlayed + participatedWars.stream().filter(pws -> pws.getWarPhaseStats() != null).mapToInt(pws -> pws.getWarPhaseStats().getGamesLost()).sum();
            int collectionGamesMissed = participatedWars.stream().mapToInt(pws -> pws.getCollectionPhaseStats().getGamesNotPlayed()).sum();

            PlayerAggregationWarStats playerAggregationWarStat = PlayerAggregationWarStats.builder()
                    .date(date.toLocalDate())
                    .gamesGranted(gamesGranted)
                    .gamesNotPlayed(gamesNotPlayed)
                    .gamesWon(wins)
                    .leagueSpan(leagueSpan)
                    .player(playerWarStats.get(0).getPlayer())
                    .totalCards(totalCards)
                    .warsEligibleForParticipation(numberOfWars)
                    .collectionGamesMissed(collectionGamesMissed)
                    .totalGamesMissed(collectionGamesMissed + gamesNotPlayed)
                    .warsParticipated(numberOfWarsParticipated).build();

            playerAggregationWarStats.add(playerAggregationWarStat);
        }

        return playerAggregationWarStats;
    }

    private WarCalculationContext collectDataForStatsCalculation(LocalDate leagueStartDate, int leagueSpan) {

        List<WarLeague> warLeagues = warLeagueRepository.findFirstNthWarLeaguesBeforeDateEager(leagueStartDate, leagueSpan);

        if (warLeagues.isEmpty() || !warLeagues.get(0).getStartDate().equals(leagueStartDate)) {
            throw new EntityNotFoundException("No league with the provided start date exists");
        }

        WarCalculationContext warCalculationContext = new WarCalculationContext();
        warCalculationContext.setWarLeagues(warLeagues);
        warCalculationContext.setLeagueStartDate(leagueStartDate);
        warCalculationContext.setLeagueSpan(leagueSpan);

        // this info is needed to filter our the players. Players that have left the clan will have aggregation stats
        // if we don't filter them out
        Set<String> playersInClanAtDate = playerCheckInCheckOutRepository.findPlayersInClanAtDate(leagueStartDate.atTime(warLeagues.get(0).getTime()))
                .stream()
                .map(PlayerInOut::getTag)
                .collect(Collectors.toSet());
        warCalculationContext.setPlayersInClanAtDate(playersInClanAtDate);

        Set<String> eligibleForWarPlayers = WarStatsCalculationService.findPlayersEligibleForWar(warCalculationContext);
        warCalculationContext.setEligiblePlayers(eligibleForWarPlayers);

        Map<String, List<PlayerWarStat>> latestNthParticipationStats =
                playerWarStatsRepository.findFirstNthParticipatedWarStats(leagueStartDate, leagueSpan).stream()
                        .collect(Collectors.groupingBy(pws -> pws.getPlayer().getTag()));

//                eligibleForWarPlayers.stream()
//                .map(tag -> playerWarStatsRepository.findFirstNthParticipatedWarStats(tag, leagueStartDate, leagueSpan))
//                .flatMap(List::stream)
//                .collect(Collectors.groupingBy(pws -> pws.getPlayer().getTag()));
        warCalculationContext.setLatestNthParticipationStats(latestNthParticipationStats);

        return warCalculationContext;
    }


}
