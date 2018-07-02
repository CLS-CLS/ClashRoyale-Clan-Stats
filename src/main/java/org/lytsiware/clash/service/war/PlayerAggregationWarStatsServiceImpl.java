package org.lytsiware.clash.service.war;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStats;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStatsRepository;
import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.league.WarLeagueRepository;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PlayerAggregationWarStatsServiceImpl implements PlayerAggregationWarStatsService {

    @Autowired
    PlayerAggregationWarStatsRepository playerAggregationWarStatsRepository;

    @Autowired
    WarLeagueRepository warLeagueRepository;

    @Override
    public List<PlayerAggregationWarStats> calculateStats(LocalDate latestLeagueStartDate, int leagueSpan, boolean strict) {

        List<WarLeague> warLeagues = warLeagueRepository.findFirstNthWarLeaguesBeforeDate(latestLeagueStartDate, leagueSpan);

        Map<Player, List<PlayerWarStat>> warStatsPerPlayer = warLeagues.stream()
                .flatMap(warLeague -> warLeague.getPlayerWarStats().stream())
                .collect(Utils.collectToMapOfLists(PlayerWarStat::getPlayer, Function.identity()));

        if (strict) {
            if (warLeagues.isEmpty() || !warLeagues.get(0).getStartDate().equals(latestLeagueStartDate)) {
                throw new IllegalArgumentException("Strict mode enabled but no league with the provided start date exists");
            }
        } else {
            if (!warLeagues.isEmpty() && !latestLeagueStartDate.equals(warLeagues.get(0).getStartDate())) {
                log.warn("The provided date {} does not correspond to the closest league date {}", latestLeagueStartDate, warLeagues.get(0).getStartDate());
                latestLeagueStartDate = warLeagues.get(0).getStartDate();
            }
        }

        List<PlayerAggregationWarStats> playerAggregationWarStats = new ArrayList<>();

        for (List<PlayerWarStat> playerWarStats : warStatsPerPlayer.values()) {

            int numberOfWars = playerWarStats.stream().map(PlayerWarStat::getWarLeague).collect(Collectors.toSet()).size();
            List<PlayerWarStat> participatedWars = playerWarStats.stream()
                    .filter(pws -> pws.getCollectionPhaseStats().getCardsWon() != 0).collect(Collectors.toList());
            int numberOfWarsParticipated = participatedWars.size();
            int averageCardsWon = (int) playerWarStats.stream().mapToInt(pws -> pws.getCollectionPhaseStats().getCardsWon())
                    .filter(i -> i != 0).average().orElse(0);
            int totalCards = playerWarStats.stream().mapToInt(pws -> pws.getCollectionPhaseStats().getCardsWon())
                    .filter(i -> i != 0).sum();
            int wins = participatedWars.stream().mapToInt(pws -> pws.getWarPhaseStats().getGamesWon()).sum();
            int gamesNotPlayed = participatedWars.stream().mapToInt(pws -> pws.getWarPhaseStats().getGamesNotPlayed()).sum();
            int crownsLost = gamesNotPlayed + participatedWars.stream().mapToInt(pws -> pws.getWarPhaseStats().getGamesLost()).sum();
            double winRatio = 0;
            int score = 0;
            if (numberOfWarsParticipated > 0) {
                winRatio = wins / (double) (crownsLost + wins);
                score = (int) ((0.50 + 0.50 * winRatio) * averageCardsWon);
            }
            PlayerAggregationWarStats playerAggregationWarStat = PlayerAggregationWarStats.builder()
                    .avgCards(averageCardsWon)
                    .avgWins(winRatio)
                    .date(latestLeagueStartDate)
                    .gamesGranted(numberOfWarsParticipated)
                    .gamesNotPlayed(gamesNotPlayed)
                    .gamesWon(wins)
                    .leagueSpan(leagueSpan)
                    .player(playerWarStats.get(0).getPlayer())
                    .score(score)
                    .totalCards(totalCards)
                    .warsEligibleForParticipation(numberOfWars)
                    .warsParticipated(numberOfWarsParticipated).build();

            playerAggregationWarStats.add(playerAggregationWarStat);

        }

        return playerAggregationWarStats;
    }


    @Override
//    @Async
    @Transactional(propagation = Propagation.REQUIRED)
    public List<PlayerAggregationWarStats> calculateAndUpdateStats(LocalDate startDate, int leagueSpan, boolean strict) {
        List<PlayerAggregationWarStats> aggregationStatsToUpdate = calculateStats(startDate, leagueSpan, strict);

        Map<PlayerAggregationWarStats, Long> aggregationWarStatsInDb = playerAggregationWarStatsRepository.findByDateAndLeagueSpan(startDate, leagueSpan)
                .stream().collect(Collectors.toMap(Function.identity(), PlayerAggregationWarStats::getId));

        for (PlayerAggregationWarStats aggregationWarStatToUpdate : aggregationStatsToUpdate) {
            aggregationWarStatToUpdate.setId(aggregationWarStatsInDb.get(aggregationWarStatToUpdate));
        }

        Iterable<PlayerAggregationWarStats> iterable = playerAggregationWarStatsRepository.saveAll(aggregationStatsToUpdate);
        List<PlayerAggregationWarStats> saved = new ArrayList<>();
        iterable.forEach(saved::add);
        return saved;
    }

    @Override
    public List<PlayerAggregationWarStats> findLatestWarAggregationStatsForWeek(Week week) {
        List<WarLeague> warLeague = warLeagueRepository.findFirstNthWarLeaguesBeforeDate(week.getEndDate(), 1);
        if (warLeague.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        return playerAggregationWarStatsRepository.findByDateAndLeagueSpan(warLeague.get(0).getStartDate(), WarConstants.leagueSpan);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void calculateMissingStats(LocalDate from, LocalDate to) {
        List<LocalDate> leagueStartDates = warLeagueRepository.findAll().stream().map(WarLeague::getStartDate).collect(Collectors.toList());
        if (from != null) {
            leagueStartDates = leagueStartDates.stream().filter(date -> date.isAfter(from)).collect(Collectors.toList());
        }

        if (to != null) {
            leagueStartDates = leagueStartDates.stream().filter(date -> date.isBefore(to)).collect(Collectors.toList());
        }

        for (LocalDate startDate : leagueStartDates) {
            if (playerAggregationWarStatsRepository.findByDateAndLeagueSpan(startDate, WarConstants.leagueSpan).isEmpty()) {
                log.info("Calculating missing war stats fro date {}", startDate);
                calculateAndUpdateStats(startDate, WarConstants.leagueSpan, true);
            }
        }
    }

    @Override
    public List<PlayerAggregationWarStats> findFirst20ByPlayerTagAndLeagueSpanAndDateBeforeOrderByDateDesc(String tag, int leagueSpan, LocalDate untilDate) {
        return playerAggregationWarStatsRepository.findFirst20ByPlayerTagAndLeagueSpanAndDateBeforeOrderByDateDesc(tag, leagueSpan, untilDate);
    }
}
