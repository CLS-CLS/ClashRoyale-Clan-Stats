package org.lytsiware.clash.service.war;

import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStats;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStatsRepository;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class PlayerAggregationWarStatsServiceImpl implements org.lytsiware.clash.service.war.PlayerAggregationWarStatsService {

    @Autowired
    PlayerAggregationWarStatsRepository playerAggregationWarStatsRepository;

    @Autowired
    PlayerWarStatsService playerWarStatsService;

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Future calculateStats(LocalDate startDate, int leagueSpan){

        Map<Player, List<PlayerWarStat>> warStatsPerPlayer = playerWarStatsService.findAllPlayerWarStats(leagueSpan, startDate.plusDays(WarConstants.leagueDays * leagueSpan));

        List<PlayerAggregationWarStats> playerAggregationWarStatServices = new ArrayList<>();

        for (List<PlayerWarStat> playerWarStats: warStatsPerPlayer.values()){

            int numberOfWars = playerWarStats.stream().map(PlayerWarStat::getWarLeague).collect(Collectors.toSet()).size();
            List<PlayerWarStat> participatedWars = playerWarStats.stream()
                    .filter(pws -> pws.getCollectionPhaseStats().getCardsWon() != 0).collect(Collectors.toList());
            int numberOfWarsParticipated = participatedWars.size();
            double averageCardsWon = playerWarStats.stream().mapToInt(pws -> pws.getCollectionPhaseStats().getCardsWon())
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
                 score = (int) ((0.75 + 0.25 * winRatio) * averageCardsWon);
            }
            PlayerAggregationWarStats playerAggregationWarStat = PlayerAggregationWarStats.builder()
                    .avgCards(averageCardsWon)
                    .avgWins(winRatio)
                    .dateFrom(startDate)
                    .gamesGranted(numberOfWarsParticipated)
                    .gamesNotPlayed(gamesNotPlayed)
                    .gamesWon(wins)
                    .leagueSpan(leagueSpan)
                    .player(playerWarStats.get(0).getPlayer())
                    .score(score)
                    .totalCards(totalCards)
                    .warsEligibleForParticipation(numberOfWars)
                    .warsParticipated(numberOfWarsParticipated).build();

            playerAggregationWarStatServices.add(playerAggregationWarStat);

        }

        Iterable<PlayerAggregationWarStats> saved = playerAggregationWarStatsRepository.saveAll(playerAggregationWarStatServices);

        return CompletableFuture.completedFuture(saved);
    }



}
