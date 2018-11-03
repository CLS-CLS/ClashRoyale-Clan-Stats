package org.lytsiware.clash.service.war;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStats;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStatsRepository;
import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.league.WarLeagueRepository;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStatsRepository;
import org.lytsiware.clash.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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


    @Override
    public void calculateStatsBetweenDates(LocalDate from, LocalDate to, int span) {
        List<WarLeague> warLeagues = warLeagueRepository.findByStartDateBetween(from, to);
        for (WarLeague warLeague : warLeagues) {
            calculateAndUpdateStats(warLeague.getStartDate(), span, true);
        }
    }


    @Override
    public List<PlayerAggregationWarStats> calculateStats(LocalDate latestLeagueStartDate, int leagueSpan, boolean strict) {

        List<WarLeague> warLeagues = warLeagueRepository.findFirstNthWarLeaguesBeforeDate(latestLeagueStartDate, leagueSpan);

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

        List<PlayerAggregationWarStats> partialCalculatedStats = calculateStats(warLeagues, latestLeagueStartDate, leagueSpan);
        Set<Player> players = partialCalculatedStats.stream().map(PlayerAggregationWarStats::getPlayer).collect(Collectors.toSet());
        List<PlayerAggregationWarStats> avgCalculatedStats = calculateAvgsStatsAndScore(latestLeagueStartDate, leagueSpan, players);
        merge(avgCalculatedStats, partialCalculatedStats);
        return partialCalculatedStats;
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

    private List<PlayerAggregationWarStats> calculateAvgsStatsAndScore(LocalDate latestLeagueStartDate, int leagueSpan, Collection<Player> players) {

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
            double winRatio = (double) latestPlayersStats.stream().mapToInt(pws -> pws.getWarPhaseStats().getGamesWon()).sum() /
                    (double) latestPlayersStats.stream().mapToInt(pws -> pws.getWarPhaseStats().getGamesGranted()).sum();
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


    @Override
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
        log.info("START findLatestWarAggregationStatsForWeek for week {}", week);
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
                log.info("Calculating missing war stats for date {}", startDate);
                calculateAndUpdateStats(startDate, WarConstants.leagueSpan, true);
            }
        }
    }

    @Override
    public List<PlayerAggregationWarStats> findWarStatsForPlayer(String tag, int leagueSpan, LocalDate untilDate) {
        return playerAggregationWarStatsRepository.findFirst40ByPlayerTagAndLeagueSpanAndDateBeforeOrderByDateDesc(tag, leagueSpan, untilDate);
    }


    private List<PlayerAggregationWarStats> calculateStats(List<WarLeague> warLeagues, LocalDate date, int leagueSpan) {

        // bug : calculate stats only for the players that have warstats in the latest league , otherwise players that have left the clan
        // will have aggregation stats. So find we find the current players and we filter out the rest (that the appear because of previous leagues)
        Set<Player> currentPlayers = warLeagues.stream().findFirst().get().getPlayerWarStats().stream().map(PlayerWarStat::getPlayer).collect(Collectors.toSet());

        Map<Player, List<PlayerWarStat>> warStatsPerPlayer = warLeagues.stream()
                .flatMap(warLeague -> warLeague.getPlayerWarStats().stream())
                .filter(playerWarStat -> currentPlayers.contains(playerWarStat.getPlayer()))
                .collect(Utils.collectToMapOfLists(PlayerWarStat::getPlayer, Function.identity()));

        List<PlayerAggregationWarStats> playerAggregationWarStats = new ArrayList<>();

        for (List<PlayerWarStat> playerWarStats : warStatsPerPlayer.values()) {

            int numberOfWars = playerWarStats.stream().map(PlayerWarStat::getWarLeague).collect(Collectors.toSet()).size();
            List<PlayerWarStat> participatedWars = playerWarStats.stream()
                    .filter(pws -> pws.getCollectionPhaseStats().getCardsWon() != 0).collect(Collectors.toList());
            int numberOfWarsParticipated = participatedWars.size();
            int gamesGranted = participatedWars.stream().mapToInt(pws -> pws.getWarPhaseStats().getGamesGranted()).sum();
            int totalCards = playerWarStats.stream().mapToInt(pws -> pws.getCollectionPhaseStats().getCardsWon())
                    .filter(i -> i != 0).sum();
            int wins = participatedWars.stream().mapToInt(pws -> pws.getWarPhaseStats().getGamesWon()).sum();
            int gamesNotPlayed = participatedWars.stream().mapToInt(pws -> pws.getWarPhaseStats().getGamesNotPlayed()).sum();
            int crownsLost = gamesNotPlayed + participatedWars.stream().mapToInt(pws -> pws.getWarPhaseStats().getGamesLost()).sum();

            PlayerAggregationWarStats playerAggregationWarStat = PlayerAggregationWarStats.builder()
                    .date(date)
                    .gamesGranted(gamesGranted)
                    .gamesNotPlayed(gamesNotPlayed)
                    .gamesWon(wins)
                    .leagueSpan(leagueSpan)
                    .player(playerWarStats.get(0).getPlayer())
                    .totalCards(totalCards)
                    .warsEligibleForParticipation(numberOfWars)
                    .warsParticipated(numberOfWarsParticipated).build();

            playerAggregationWarStats.add(playerAggregationWarStat);
        }

        return playerAggregationWarStats;
    }

}
