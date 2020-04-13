package org.lytsiware.clash.service.calculation.war;

import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStats;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.domain.war.playerwarstat.WarPhaseStats;
import org.lytsiware.clash.utils.Utils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class WarStatsCalculationServiceImpl implements WarStatsCalculationService {


    @Override
    public List<PlayerAggregationWarStats> calculateStats(WarCalculationContext warCalculationContext) {

        Map<Player, List<PlayerWarStat>> warStatsPerPlayer = warCalculationContext.getWarLeagues().stream()
                .flatMap(warLeague -> warLeague.getPlayerWarStats().stream())
                .filter(playerWarStat -> warCalculationContext.getEligiblePlayers().contains(playerWarStat.getPlayer().getTag()))
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
                    .date(warCalculationContext.getLeagueStartDate())
                    .gamesGranted(gamesGranted)
                    .gamesNotPlayed(gamesNotPlayed)
                    .gamesWon(wins)
                    .leagueSpan(warCalculationContext.getLeagueSpan())
                    .player(playerWarStats.get(0).getPlayer())
                    .totalCards(totalCards)
                    .warsEligibleForParticipation(numberOfWars)
                    .collectionGamesMissed(collectionGamesMissed)
                    .totalGamesMissed(collectionGamesMissed + gamesNotPlayed)
                    .warsParticipated(numberOfWarsParticipated).build();

            //calculate participation statistics ( = statistics based on the last Nth games that the player has participated)
            //a player may have not have participation stats if he has not participated in any war so long
            List<PlayerWarStat> participationStats = Optional.ofNullable(
                    warCalculationContext.getLatestNthParticipationStats().get(playerAggregationWarStat.getPlayer().getTag())
            ).orElse(new ArrayList<>());

            int averageCardsWon = (int) participationStats.stream().mapToInt(pws -> pws.getCollectionPhaseStats().getCardsWon())
                    .average().orElse(0);

            int participationGamesGranted = participationStats.stream().filter(pws -> pws.getWarPhaseStats() != null)
                    .mapToInt(pws -> pws.getWarPhaseStats().getGamesGranted()).sum();

            double participationGamesWon = participationStats.stream().filter(pws -> pws.getWarPhaseStats() != null)
                    .mapToInt(pws -> pws.getWarPhaseStats().getGamesWon()).sum();
            double winRatio = (participationGamesGranted > 0 ? participationGamesWon / (double) participationGamesGranted : 0);

            int score = (int) ((0.50 + 0.50 * winRatio) * averageCardsWon);

            playerAggregationWarStat.setAvgCards(averageCardsWon);
            playerAggregationWarStat.setAvgWins(winRatio);
            playerAggregationWarStat.setScore(score);

            playerAggregationWarStats.add(playerAggregationWarStat);
        }

        return playerAggregationWarStats;
    }


}
