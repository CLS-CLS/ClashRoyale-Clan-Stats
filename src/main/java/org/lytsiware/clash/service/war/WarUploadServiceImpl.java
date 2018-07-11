package org.lytsiware.clash.service.war;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.domain.player.PlayerRepository;
import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WarUploadServiceImpl implements WarUploadService {

    private PlayerWarStatsService playerWarStatsService;
    private WarLeagueService warLeagueService;
    private PlayerRepository playerRepository;

    @Autowired
    public WarUploadServiceImpl(PlayerWarStatsService playerWarStatsService, WarLeagueService warLeagueService, PlayerRepository playerRepository) {
        this.playerWarStatsService = playerWarStatsService;
        this.warLeagueService = warLeagueService;
        this.playerRepository = playerRepository;
    }


    @Override
    public String replaceNameWithTag(InputStream inputStream, String filename) {

        List<String> lines = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
        LocalDate leagueDate = LocalDate.parse(filename.split("\\.")[0], DateTimeFormatter.ofPattern("dd-MM-yyyy"));

//        Map<String, String> playersInClan = Stream.concat(playerWeeklyStatsRepository.findByWeek(Week.fromDate(leagueDate)).stream(),
//                playerWeeklyStatsRepository.findByWeek(Week.fromDate(leagueDate).minusWeeks(1)).stream())
//                .map(PlayerWeeklyStats::getPlayer).collect(Collectors.toMap(player -> player.getName().toLowerCase(), player -> player.getTag(), (l, r) -> l));

        Map<String, String> playersInClan = playerRepository.loadAll().entrySet().stream().collect(Collectors.toMap(entry -> entry.getValue().getName().toLowerCase(),
                Map.Entry::getKey));

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
    public void upload(MultipartFile[] files) throws IOException {
        List<WarLeague> warLeagues = new ArrayList<>();

        for (MultipartFile file : files) {
            log.info("uploading file {}", file.getOriginalFilename());
            List<PlayerWarStat> statsList = Utils.parseCsv(file.getInputStream(), file.getOriginalFilename());

            if (statsList.isEmpty()) {
                log.warn("File {} was empty", file.getOriginalFilename());
                continue;
            }

            warLeagueService.calculateLeagueAvgsAndSave(statsList.get(0).getWarLeague());
            warLeagues.add(playerWarStatsService.saveWarStatsWithMissingParticipants(statsList));
        }

        playerWarStatsService.updateWarStatsForAffectedLeagues(warLeagues);

    }


}
