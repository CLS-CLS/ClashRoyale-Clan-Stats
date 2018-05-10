package org.lytsiware.clash.service.war;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStatsRepository;
import org.lytsiware.clash.domain.war.*;
import org.lytsiware.clash.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
@Slf4j
public class PlayerWarStatsServiceImpl implements PlayerWarStatsService {

    public static final int WAR_DURATION = 2;
    private PlayerWarStatsRepository playerWarStatsRepository;
    private WarLeagueRepository warLeagueRepository;
    private PlayerWeeklyStatsRepository playerWeeklyStatsRepository;
    private Clock clock;

    @Autowired
    public PlayerWarStatsServiceImpl(PlayerWarStatsRepository playerWarStatsRepository,
                                     WarLeagueRepository warLeagueRepository,
                                     PlayerWeeklyStatsRepository playerWeeklyStatsRepository,
                                     Clock clock) {

        this.playerWarStatsRepository = playerWarStatsRepository;
        this.warLeagueRepository = warLeagueRepository;
        this.playerWeeklyStatsRepository = playerWeeklyStatsRepository;
        this.clock = clock;
    }

    @Override
    public List<PlayerWarStat> persistPlayerWarStats(List<PlayerWarStat> playerWarStats, LocalDate leagueStartDate, String leagueName) {

        List<PlayerWarStat> persistedWarStats = new ArrayList<>();

        playerWarStatsRepository.save(playerWarStats).forEach(persisted -> persistedWarStats.add(persisted));

        return persistedWarStats;

    }

    @Override
    public Map<Player, List<PlayerWarStat>> findAllPlayerWarStats(int numberOfPastWars, LocalDate endDate) {

        List<WarLeague> leagues = warLeagueRepository.findByStartDateBetween(endDate.minusDays(WAR_DURATION * numberOfPastWars), endDate);
        List<PlayerWarStat> playerWarStats = playerWarStatsRepository.findByWarLeagueIn(leagues);

        HashMap<Player, List<PlayerWarStat>> statsPerPlayer = playerWarStats.stream()
                .collect(Utils.collectToMapOfLists(PlayerWarStat::getPlayer, Function.identity()));

        return statsPerPlayer;
    }

    @Override
    public String replaceNameWithTag(InputStream inputStream, String filename) {

        List<String> lines = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
        LocalDate leagueDate = LocalDate.parse(filename.split("\\.")[0], DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        Map<String, String> playersInClan = Stream.concat(playerWeeklyStatsRepository.findByWeek(Week.fromDate(leagueDate)).stream(),
                playerWeeklyStatsRepository.findByWeek(Week.fromDate(leagueDate).minusWeeks(1)).stream())
                .map(PlayerWeeklyStats::getPlayer).collect(Collectors.toMap(player -> player.getName().toLowerCase(), player -> player.getTag(), (l, r) -> l));

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(lines.get(0)).append("\r\n").append(lines.get(1)).append("\r\n");
        String[] previousStats = null;
        for (String line : lines.subList(2, lines.size())) {
            try {
                String[] stats = line.split(",");
                String name = stats[0].trim();
                String tag = playersInClan.computeIfAbsent(name.toLowerCase(), s -> "**** " + name);
                stringBuilder
                        .append(tag).append(",")
                        .append(stats[1]).append(",")
                        .append(stats[2]).append(",")
                        .append(stats[3]).append(",")
                        .append(stats[4]).append("\r\n");
                validate(stats, previousStats, line);
                previousStats = stats;
            } catch (Exception ex) {
                throw new RuntimeException("Error in line: " + line, ex);
            }
        }

        return stringBuilder.toString();

    }

    private void validate(String[] stats, String[] previousStats, String line) {
        if (previousStats == null) {
            return;
        }
        if (Integer.valueOf(previousStats[3].trim()) < Integer.valueOf(stats[3].trim())) {
            throw new IllegalArgumentException("Games won should not be more in the previous stat, line: " + line);
        }
        if (Integer.valueOf(previousStats[4].trim()) == Integer.valueOf(stats[4].trim()) &&
                Integer.valueOf(previousStats[2].trim()) == Integer.valueOf(stats[2].trim())
                && Integer.valueOf(previousStats[3].trim()) == Integer.valueOf(stats[3].trim())) {
            if (Integer.valueOf(previousStats[1].trim()) < Integer.valueOf(stats[1].trim())) {
                throw new IllegalArgumentException("Cards cannot be more that the previous stat, line: " + line);
            }
        }
    }


    @Override
    public void upload(InputStream inputStream, String fileName) {
        log.info("uploading file {}", fileName);
        List<String> lines = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
        String leagueName = lines.get(0).split(",")[0].trim();
        String rank = lines.get(0).split(",")[1].trim();
        String trophies = lines.get(0).split(",")[2].trim();

        lines = lines.subList(2, lines.size()); //remove two first lines (league stats, and header)

        List<PlayerWarStat> statsList = new ArrayList<>();

        LocalDate leagueDate = LocalDate.parse(fileName.split("\\.")[0], DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        WarLeague warLeague = new WarLeague(leagueDate);
        warLeague.setName(leagueName);
        warLeague.setRank(Integer.valueOf(rank));
        warLeague.setTrophies(Integer.valueOf(rank));

        for (String line : lines) {
            String[] stats = line.split(",");
            String tag = stats[0].trim();
            Integer cardsWon = Integer.valueOf(stats[1].trim());
            Integer gamesGranted = Integer.valueOf(stats[2].trim());
            Integer gamesWon = Utils.parseNullableInt(stats[3].trim());
            Integer gamesLost = Utils.parseNullableInt(stats[4].trim());

            if (gamesLost == null) {
                if (gamesGranted != gamesWon) {
                    throw new IllegalArgumentException("Games lost should have been provided");
                }
                gamesLost = 0;
            }

            if (gamesWon == null) {
                if (gamesGranted != gamesLost) {
                    throw new IllegalArgumentException("Games won should have been provided");
                }
                gamesWon = 0;
            }

            PlayerWarStat pws = PlayerWarStat.builder()
                    .player(new Player(tag, null, null, true))
                    .warLeague(warLeague)
                    .collectionPhaseStats(CollectionPhaseStats.builder()
                            .cardsWon(cardsWon)
                            .build())
                    .warPhaseStats(WarPhaseStats.builder()
                            .gamesWon(gamesWon)
                            .gamesLost(gamesLost)
                            .gamesGranted(gamesGranted)
                            .build())
                    .build();
            statsList.add(pws);
        }

        Set<String> playersInClan = playerWeeklyStatsRepository.findByWeek(Week.fromDate(leagueDate))
                .stream().map(PlayerWeeklyStats::getPlayer).map(Player::getTag).collect(Collectors.toSet());

        for (PlayerWarStat pws : statsList) {
            playersInClan.remove(pws.getPlayer().getTag());
        }
        for (String playerTag : playersInClan) {
            statsList.add(PlayerWarStat.builder()
                    .warLeague(warLeague)
                    .player(new Player(playerTag, null, null, true))
                    .warPhaseStats(WarPhaseStats.builder()
                            .gamesGranted(0)
                            .gamesLost(0)
                            .gamesWon(0)
                            .build())
                    .collectionPhaseStats(CollectionPhaseStats.builder()
                            .cardsWon(0)
                            .gamesPlayed(0)
                            .build())
                    .build());
        }


        this.persistPlayerWarStats(statsList, leagueDate, leagueName);

    }
}
