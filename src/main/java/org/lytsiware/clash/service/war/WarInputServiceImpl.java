package org.lytsiware.clash.service.war;


import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.player.PlayerInOut;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStatsRepository;
import org.lytsiware.clash.dto.war.input.WarStatsInputDto;
import org.lytsiware.clash.service.integration.SiteIntegrationService;
import org.lytsiware.clash.service.integration.SiteQualifier;
import org.lytsiware.clash.service.integration.statsroyale.StatsRoyaleDateParse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class WarInputServiceImpl implements WarInputService {

    private SiteIntegrationService<List<WarStatsInputDto>> siteIntegrationService;

    private Clock clock;

    private StatsRoyaleDateParse statsRoyaleDateParse;

    private PlayerWeeklyStatsRepository playerWeeklyStatsRepository;

    private PlayerWarStatsService playerWarStatsService;


    @Autowired
    public WarInputServiceImpl(@SiteQualifier(SiteQualifier.Name.STATS_ROYALE_WAR) SiteIntegrationService<List<WarStatsInputDto>> siteIntegrationService,
                               StatsRoyaleDateParse statsRoyaleDateParse,
                               PlayerWeeklyStatsRepository playerWeeklyStatsRepository,
                               PlayerWarStatsService playerWarStatsService,
                               Clock clock) {
        this.siteIntegrationService = siteIntegrationService;
        this.statsRoyaleDateParse = statsRoyaleDateParse;
        this.playerWeeklyStatsRepository = playerWeeklyStatsRepository;
        this.playerWarStatsService = playerWarStatsService;
        this.clock = clock;
    }

    @Override
    public List<WarStatsInputDto> getWarStatsFromSite() {
        return siteIntegrationService.retrieveData();
    }


    @Override
    public List<WarStatsInputDto> getPlayerWarStatsForInput(boolean includeNotParticipating) {
        List<WarStatsInputDto> siteAllWarLeagueStats = getWarStatsFromSite();

        for (WarStatsInputDto siteWarLeagueStat : siteAllWarLeagueStats) {

            LocalDateTime leagueStartDate = statsRoyaleDateParse.parseDescriptiveDate(siteWarLeagueStat.getLeagueName(), LocalDateTime.now(clock));

            Map<Player, PlayerInOut> playerNotParticipated = (includeNotParticipating ? playerWarStatsService.findPlayersNotParticipatedInWar(siteWarLeagueStat, leagueStartDate, 240) : new HashMap<>());

            List<WarStatsInputDto.PlayerWarStatInputDto> playerNorParticipatedWarStatsInputDto = updateDeleteStatusFonNotParticipated(playerNotParticipated, leagueStartDate, 240);

            siteWarLeagueStat.setPlayersNotParticipated(playerNorParticipatedWarStatsInputDto);

            siteWarLeagueStat.setStartDate(leagueStartDate);

        }

        normalizeWarInputData(siteAllWarLeagueStats);

        return siteAllWarLeagueStats;
    }

    private List<WarStatsInputDto.PlayerWarStatInputDto> updateDeleteStatusFonNotParticipated(Map<Player, PlayerInOut> playerNotParticipated, LocalDateTime startDate, int faultTolerance) {

        List<WarStatsInputDto.PlayerWarStatInputDto> notParticipatedInputDtoList = new ArrayList<>();

        for (Player player : playerNotParticipated.keySet()) {
            WarStatsInputDto.PlayerWarStatInputDto playerWarStatsInputDto = WarStatsInputDto.PlayerWarStatInputDto.zeroFieldPlayerWarStatInputDto(player.getTag(), player.getName());
            PlayerInOut pIO = playerNotParticipated.get(player);
            if (pIO.getCheckIn().isAfter(startDate)) {
                playerWarStatsInputDto.setDelete(true);
            }
            notParticipatedInputDtoList.add(playerWarStatsInputDto);
        }

        return notParticipatedInputDtoList;
    }


    private void normalizeWarInputData(List<WarStatsInputDto> siteAllWarLeagueStats) {

        for (WarStatsInputDto siteWarLeagueStat : siteAllWarLeagueStats) {
            siteWarLeagueStat.getPlayerWarStats().stream().filter(player -> player.getGamesGranted() == 0).forEach(
                    player -> {
                        player.setGamesGranted(1);
                        player.setGamesNotPlayed(1);
                    }
            );
            siteWarLeagueStat.getPlayerWarStats().sort(Comparator.comparing(WarStatsInputDto.PlayerWarStatInputDto::getGamesWon)
                    .thenComparing(WarStatsInputDto.PlayerWarStatInputDto::getGamesLost)
                    .thenComparing(WarStatsInputDto.PlayerWarStatInputDto::getGamesNotPlayed)
                    .thenComparing(WarStatsInputDto.PlayerWarStatInputDto::getCards).reversed());
        }
    }
}
