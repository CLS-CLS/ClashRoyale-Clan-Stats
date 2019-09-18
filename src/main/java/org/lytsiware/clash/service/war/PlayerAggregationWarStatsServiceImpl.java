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
import org.lytsiware.clash.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public void recalculateAndUpdateWarStatsForLeagues(List<WarLeague> warLeagues) {
        List<WarLeague> affectedLeagues = warLeagues.stream()
                .flatMap(warLeague -> warLeagueRepository.findFirstNthWarLeaguesAfterDate(warLeague.getStartDate(), WarConstants.leagueSpan).stream())
                .distinct().collect(Collectors.toList());

        for (WarLeague affectedLeague : affectedLeagues) {
            calculateAndUpdateStats(affectedLeague.getStartDate(), WarConstants.leagueSpan);
        }
    }


    @Override
    public void calculateStatsBetweenDates(LocalDate from, LocalDate to, int span) {
        List<WarLeague> warLeagues = warLeagueRepository.findByStartDateBetween(from, to);
        for (WarLeague warLeague : warLeagues) {
            calculateAndUpdateStats(warLeague.getStartDate(), span);
        }
    }


    @Override
    public List<PlayerAggregationWarStats> calculateStats(LocalDate latestLeagueStartDate, int leagueSpan) {

        List<WarLeague> warLeagues = warLeagueRepository.findFirstNthWarLeaguesBeforeDate(latestLeagueStartDate, leagueSpan);

        if (warLeagues.isEmpty() || !warLeagues.get(0).getStartDate().equals(latestLeagueStartDate)) {
            throw new IllegalArgumentException("Strict mode enabled but no league with the provided start date exists");
        }

        List<PlayerAggregationWarStats> partialCalculatedStats = calculateStats(warLeagues, latestLeagueStartDate.atTime(warLeagues.get(0).getTime()), leagueSpan);
        Set<Player> players = partialCalculatedStats.stream().map(PlayerAggregationWarStats::getPlayer).collect(Collectors.toSet());
        List<PlayerAggregationWarStats> avgCalculatedStats = calculateAvgsStatsAndScore(latestLeagueStartDate, leagueSpan, players, partialCalculatedStats);
        merge(avgCalculatedStats, partialCalculatedStats);
        return partialCalculatedStats;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<PlayerAggregationWarStats> calculateAndUpdateStats(LocalDate startDate, int leagueSpan) {
        List<PlayerAggregationWarStats> aggregationStatsToUpdate = calculateStats(startDate, leagueSpan);

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
    public ClansWarGlobalStatsDto findLatestWarAggregationStatsForWar(int deltaWar) {
        log.info("START findLatestWarAggregationStatsForWar for deltawar {}", deltaWar);
        LocalDate latestRecordedDate = warLeagueRepository.findLatestRecordedWarLeague().map(WarLeague::getStartDate).orElse(null);

        return warLeagueRepository.findNthWarLeague(deltaWar)
                .map(warLeague1 -> playerAggregationWarStatsRepository.findByDateAndLeagueSpan(warLeague1.getStartDate(), WarConstants.leagueSpan))
                .map(playerAggregationWarStats -> new ClansWarGlobalStatsDto(playerAggregationWarStats, latestRecordedDate)).orElse(null);

    }


    @Override
    public List<PlayerAggregationWarStats> findWarStatsForPlayer(String tag, int leagueSpan, LocalDate untilDate) {
        return playerAggregationWarStatsRepository.findFirst40ByPlayerTagAndLeagueSpanAndDateLessThanEqualOrderByDateDesc(tag, leagueSpan, untilDate);
    }


    private List<PlayerAggregationWarStats> calculateStats(List<WarLeague> warLeagues, LocalDateTime date, int leagueSpan) {

        // players that have left the clan will have aggregation stats if we don't filter them out
        Set<String> playersInClanAtDate = playerCheckInCheckOutRepository.findPlayersInClanAtDate(date).stream().map(PlayerInOut::getTag).collect(Collectors.toSet());

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

    private void merge(List<PlayerAggregationWarStats> info, List<PlayerAggregationWarStats> to) {
        for (PlayerAggregationWarStats playerAggregationWarStat : to) {
            int index = info.indexOf(playerAggregationWarStat);
            if (index == -1) {
                throw new IllegalArgumentException("Avg stats not found for player aggregation stats player = " + playerAggregationWarStat.getPlayer());
            }
            playerAggregationWarStat.setAvgCards(info.get(index).getAvgCards());
            playerAggregationWarStat.setAvgWins(info.get(index).getAvgWins());
            playerAggregationWarStat.setScore(info.get(index).getScore());
        }
    }

    private List<PlayerAggregationWarStats> calculateAvgsStatsAndScore(LocalDate latestLeagueStartDate, int leagueSpan, Collection<Player> players, List<PlayerAggregationWarStats> partialCalculatedStats) {

        List<WarLeague> warLeagues = warLeagueRepository.findFirstNthWarLeaguesBeforeDate(latestLeagueStartDate, 1);
        WarLeague warLeague = (warLeagues.size() == 1) ? warLeagues.get(0) : null;

        if (warLeague == null) {
            return null;
        }

        List<PlayerAggregationWarStats> result = new ArrayList<>();

        for (Player player : players) {
            List<PlayerWarStat> latestPlayersStats = playerWarStatsRepository.findFirstNthParticipatedWarStats(player.getTag(),
                    latestLeagueStartDate, leagueSpan);

            int averageCardsWon = (int) latestPlayersStats.stream().mapToInt(pws -> pws.getCollectionPhaseStats().getCardsWon())
                    .average().orElse(0);
            int gamesGranted = latestPlayersStats.stream().filter(pws -> pws.getWarPhaseStats() != null).mapToInt(pws -> pws.getWarPhaseStats().getGamesGranted()).sum();

            double winRatio = (gamesGranted > 0 ?
                    (double) latestPlayersStats.stream().filter(pws -> pws.getWarPhaseStats() != null).mapToInt(pws -> pws.getWarPhaseStats().getGamesWon()).sum() / (double) gamesGranted
                    : 0);

            PlayerAggregationWarStats partialPlayerStat = partialCalculatedStats.stream().filter(playerAggregationWarStat -> playerAggregationWarStat.getPlayer().equals(player)).findFirst().get();

            int score = (int) ((0.50 + 0.50 * winRatio) * averageCardsWon);

            result.add(PlayerAggregationWarStats.builder().player(player)
                    .date(warLeague.getStartDate())
                    .leagueSpan(leagueSpan)
                    .avgCards(averageCardsWon)
                    .avgWins(winRatio)
                    .score(score)
                    .build());
        }
        return result;
    }


}
