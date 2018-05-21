package org.lytsiware.clash.service.war;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStatsRepository;
import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.playerwarstat.CollectionPhaseStats;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.domain.war.playerwarstat.WarPhaseStats;
import org.lytsiware.clash.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class WarUploadServiceImpl implements WarUploadService {

    private PlayerWeeklyStatsRepository playerWeeklyStatsRepository;
    private PlayerWarStatsService playerWarStatsService;
    private WarLeagueService warLeagueService;
    private PlayerAggregationWarStatsService playerAggregationWarStatsService;

    @Autowired
    public WarUploadServiceImpl(PlayerWeeklyStatsRepository playerWeeklyStatsRepository, PlayerWarStatsService playerWarStatsService,
                                PlayerAggregationWarStatsService playerAggregationWarStatsService, WarLeagueService warLeagueService) {
        this.playerWeeklyStatsRepository = playerWeeklyStatsRepository;
        this.playerWarStatsService = playerWarStatsService;
        this.playerAggregationWarStatsService = playerAggregationWarStatsService;
        this.warLeagueService = warLeagueService;
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
                Utils.validateWarStats(stats, previousStats, line);
                previousStats = stats;
            } catch (Exception ex) {
                throw new RuntimeException("Error in line: " + line, ex);
            }
        }

        return stringBuilder.toString();

    }




    @Override
    public void upload(InputStream inputStream, String fileName) {
        log.info("uploading file {}", fileName);

        List<PlayerWarStat> statsList = Utils.parseCsv(inputStream, fileName);

        if (statsList.isEmpty()) {
            return;
        }

        WarLeague warLeague = statsList.get(0).getWarLeague();
        Set<String> playersInClan = playerWeeklyStatsRepository.findByWeek(Week.fromDate(warLeague.getStartDate()))
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

        playerWarStatsService.persistPlayerWarStats(statsList);

        List<WarLeague> affectedLeagues = warLeagueService.getAffectedLeagues(warLeague.getStartDate(), WarConstants.leagueSpan);
        for (WarLeague warLeague1: affectedLeagues) {
            playerAggregationWarStatsService.calculateStats(warLeague1.getStartDate(), WarConstants.leagueSpan);
        }

    }

}