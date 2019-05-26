package org.lytsiware.clash.service.war;


import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.converter.InputDtoPlayerWarStatConverter;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.player.PlayerInOut;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStatsRepository;
import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.league.WarLeagueRepository;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.dto.war.input.WarStatsInputDto;
import org.lytsiware.clash.service.integration.SiteIntegrationService;
import org.lytsiware.clash.service.integration.statsroyale.StatsRoyaleDateParse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class WarInputServiceImpl implements WarInputService {
    @Autowired
    private SiteIntegrationService<List<WarStatsInputDto>> siteIntegrationService;
    @Autowired
    private Clock clock;
    @Autowired
    private StatsRoyaleDateParse statsRoyaleDateParse;
    @Autowired
    private PlayerWeeklyStatsRepository playerWeeklyStatsRepository;
    @Autowired
    private PlayerWarStatsService playerWarStatsService;

    @Autowired
    private WarLeagueRepository warLeagueRepository;


    @Override
    public List<WarStatsInputDto> getWarStatsFromSite() {
        return siteIntegrationService.retrieveData();
    }


    @Override
    public void updateOrSaveWarStats() {
        getPlayerWarStatsForInput(true).stream().findFirst().map(InputDtoPlayerWarStatConverter::toPlayerWarStat)
                .ifPresent(playerWarStatsService::saveWarStatsAndUpdateStatistics);
    }

    @Override
    public List<WarStatsInputDto> getPlayerWarStatsForInput(boolean includeNotParticipating) {
        List<WarStatsInputDto> siteAllWarLeagueStats = getWarStatsFromSite();
        for (WarStatsInputDto siteWarLeagueStat : siteAllWarLeagueStats) {

            LocalDateTime leagueStartDate = statsRoyaleDateParse.parseDescriptiveDate(siteWarLeagueStat.getLeagueName(), LocalDateTime.now(clock)).minusDays(WarConstants.WAR_DURATION);

            Map<Player, PlayerInOut> playerNotParticipated = (includeNotParticipating ? playerWarStatsService.findPlayersNotParticipatedInWar(siteWarLeagueStat, leagueStartDate, 240) : new HashMap<>());

            List<WarStatsInputDto.PlayerWarStatInputDto> playerNotParticipatedWarStatsInputDto = updateDeleteStatusFonNotParticipated(playerNotParticipated, leagueStartDate, 60 * 36);

            siteWarLeagueStat.setPlayersNotParticipated(playerNotParticipatedWarStatsInputDto);

            siteWarLeagueStat.setStartDate(leagueStartDate);

        }

        normalizeWarInputData(siteAllWarLeagueStats);

        return siteAllWarLeagueStats;
    }

    private List<WarStatsInputDto.PlayerWarStatInputDto> updateDeleteStatusFonNotParticipated(Map<Player, PlayerInOut> playerNotParticipated, LocalDateTime startDate, int faultTolerance) {

        List<WarStatsInputDto.PlayerWarStatInputDto> notParticipatedInputDtoList = new ArrayList<>();

        for (Player player : playerNotParticipated.keySet()) {
            WarStatsInputDto.PlayerWarStatInputDto playerWarStatsInputDto = WarStatsInputDto.PlayerWarStatInputDto
                    .zeroFieldPlayerWarStatInputDto(player.getTag(), player.getName());
            PlayerInOut pIO = playerNotParticipated.get(player);
            if (pIO.getCheckIn().isAfter(startDate.minusMinutes(faultTolerance))) {
                playerWarStatsInputDto.setDelete(true);
            }
            notParticipatedInputDtoList.add(playerWarStatsInputDto);
        }

        return notParticipatedInputDtoList;
    }

    /**
     * Normalizes the data as following
     * <ul><li>Sorts the data per games won</li>
     * <li> updates the collection games played from the db stats</li>
     * <li> if a player has earned collection day cards but has no wins or looses add +1 to games not played</li>
     * <li> if the stats from the site shows that the player has more collection cards won than the ones stored in the db (if any) add +1 to collection battles played</li>
     * </ul>
     *
     * @param siteAllWarLeagueStats
     */
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

            Set<PlayerWarStat> playerWarStats = warLeagueRepository.findByStartDate(siteWarLeagueStat.getStartDate().toLocalDate())
                    .map(WarLeague::getPlayerWarStats).orElse(new HashSet<>());
            for (PlayerWarStat pwsDb : playerWarStats) {
                siteWarLeagueStat.getPlayerWarStats().stream()
                        // find player
                        .filter(playerWarStatInputDto -> playerWarStatInputDto.getTag().equals(pwsDb.getPlayer().getTag()))
                        .forEach(playerWarStatInputDto -> {
                            playerWarStatInputDto.setCollectionBattlesPlayed(pwsDb.getCollectionPhaseStats().getGamesPlayed());
                            // check the ones that the input shows that they have won more cards that was stored in the db
                            // add one more collection battle (there is no way they could have more cards if they did not player another battle)
                            if (playerWarStatInputDto.getCards() > pwsDb.getCollectionPhaseStats().getCardsWon()) {
                                playerWarStatInputDto.setCollectionBattlesPlayed(pwsDb.getCollectionPhaseStats().getGamesPlayed() + 1);
                            } else {
                                playerWarStatInputDto.setCollectionBattlesPlayed(pwsDb.getCollectionPhaseStats().getGamesPlayed());
                            }
                        });

            }
        }
    }
}
