package org.lytsiware.clash.service.war;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStatsRepository;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStats;
import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.league.WarLeagueRepository;
import org.lytsiware.clash.domain.war.playerwarstat.CollectionPhaseStats;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStatsRepository;
import org.lytsiware.clash.domain.war.playerwarstat.WarPhaseStats;
import org.lytsiware.clash.dto.PlaywerWarStatsWithAvgsDto;
import org.lytsiware.clash.dto.war.input.WarStatsInputDto;
import org.lytsiware.clash.service.integration.statsroyale.StatsRoyaleDateParse;
import org.lytsiware.clash.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.lytsiware.clash.dto.war.input.WarStatsInputDto.PlayerWarStatInputDto;

@Service
@Transactional
@Slf4j
public class PlayerWarStatsServiceImpl implements PlayerWarStatsService {

    public static final int WAR_DURATION = 2;
    private PlayerWarStatsRepository playerWarStatsRepository;
    private WarLeagueService warLeagueService;
    private WarLeagueRepository warLeagueRepository;
    private PlayerWeeklyStatsRepository playerWeeklyStatsRepository;
    private PlayerAggregationWarStatsService playerAggregationWarStatsService;
    private WarInputService warInputService;
    private StatsRoyaleDateParse statsRoyaleDateParse;
    private Clock clock;

    @Autowired
    public PlayerWarStatsServiceImpl(PlayerWarStatsRepository playerWarStatsRepository,
                                     WarLeagueService warLeagueService,
                                     WarLeagueRepository warLeagueRepository,
                                     PlayerWeeklyStatsRepository playerWeeklyStatsRepository,
                                     PlayerAggregationWarStatsService playerAggregationWarStatsService,
                                     WarInputService warInputService,
                                     StatsRoyaleDateParse statsRoyaleDateParse,
                                     Clock clock) {

        this.playerWarStatsRepository = playerWarStatsRepository;
        this.warLeagueService = warLeagueService;
        this.warLeagueRepository = warLeagueRepository;
        this.playerWeeklyStatsRepository = playerWeeklyStatsRepository;
        this.playerAggregationWarStatsService = playerAggregationWarStatsService;
        this.warInputService = warInputService;
        this.statsRoyaleDateParse = statsRoyaleDateParse;
        this.clock = clock;
    }

    @Override
    public List<PlayerWarStat> persistPlayerWarStats(List<PlayerWarStat> playerWarStats) {
        List<PlayerWarStat> persisted = playerWarStatsRepository.saveAll(playerWarStats);
        playerWarStatsRepository.flush();
        return persisted;
    }


    @Override
    public PlaywerWarStatsWithAvgsDto getPlayerWarStatsForWeek(String tag, LocalDate untilDate) {

        Map<LocalDate, PlayerWarStat> playerWarStats = playerWarStatsRepository.findFirst20ByPlayerTagAndWarLeagueStartDateBeforeOrderByWarLeagueStartDateDesc(tag, untilDate)
                .stream().collect(Utils.collectToMap(pws -> pws.getWarLeague().getStartDate(), Function.identity()));

        Map<LocalDate, PlayerAggregationWarStats> playerAggrWarStats = playerAggregationWarStatsService.findFirst20ByPlayerTagAndLeagueSpanAndDateBeforeOrderByDateDesc(tag, WarConstants.leagueSpan, untilDate)
                .stream().collect(Utils.collectToMap(PlayerAggregationWarStats::getDate, Function.identity()));

        return new PlaywerWarStatsWithAvgsDto(playerWarStats, playerAggrWarStats);

    }


    @Override
    public WarLeague saveWarStatsWithMissingParticipants(List<PlayerWarStat> statsList) {

        WarLeague warLeague = statsList.get(0).getWarLeague();
        Set<String> playersInClan = playerWeeklyStatsRepository.findByWeek(Week.fromDate(warLeague.getStartDate()))
                .stream().map(PlayerWeeklyStats::getPlayer).map(Player::getTag).collect(Collectors.toSet());

        for (PlayerWarStat pws : statsList) {
            playersInClan.remove(pws.getPlayer().getTag());
        }


        for (String playerTag : playersInClan) {
            PlayerWarStat notParticipatingPWS = PlayerWarStat.builder()
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
                    .build();
            notParticipatingPWS.setWarLeague(warLeague);
            statsList.add(notParticipatingPWS);
        }


        persistPlayerWarStats(statsList);

        return warLeague;
    }

    @Override
    public void updateWarStatsForAffectedLeagues(List<WarLeague> warLeagues) {
        List<WarLeague> affectedLeagues = warLeagues.stream()
                .flatMap(warLeague -> warLeagueService.findFirstNthWarLeaguesAfterDate(warLeague.getStartDate(), WarConstants.leagueSpan).stream())
                .distinct().collect(Collectors.toList());

        for (WarLeague affectedLeague : affectedLeagues) {
            playerAggregationWarStatsService.calculateAndUpdateStats(affectedLeague.getStartDate(), WarConstants.leagueSpan, true);
        }

    }

    @Override
    public void savePlayerWarStats(List<PlayerWarStat> statsList) throws EntityExistsException {
        WarLeague warLeague = statsList.get(0).getWarLeague();

        if (warLeagueRepository.findByStartDate(warLeague.getStartDate()).isPresent()) {
            throw new EntityExistsException("League with start date " + warLeague.getStartDate() + " already exists ");
        }

        warLeagueService.calculateLeagueAvgsAndSave(warLeague);
        persistPlayerWarStats(statsList);
        updateWarStatsForAffectedLeagues(Arrays.asList(warLeague));
    }

    @Override
    public List<WarStatsInputDto> getPlayerWarStatsForInput(boolean includeNotParticipating) {
        List<WarStatsInputDto> siteAllWarLeagueStats = warInputService.getWarStatsFromSite();

        for (WarStatsInputDto siteWarLeagueStat : siteAllWarLeagueStats) {

            LocalDate leagueStartDate = LocalDate.now();

            try {
                leagueStartDate = statsRoyaleDateParse.parseDescriptiveDate(siteWarLeagueStat.getLeagueName(), LocalDateTime.now()).toLocalDate();
            } catch (IllegalArgumentException ex) {
                log.warn("Error while trying to parse date from leagues description -  falling back to current date. ");
                log.warn("With exception : {}", ex);
            }

            Map<String, Player> playerNotParticipated = includeNotParticipating ? findPlayersNotParticipatedInWar(siteWarLeagueStat, leagueStartDate) : new HashMap<>();

            siteWarLeagueStat.setPlayersNotParticipated(
                    playerNotParticipated.entrySet().stream()
                            .map(entry -> WarStatsInputDto.PlayerWarStatInputDto.zeroFieldPlayerWarStatInputDto(entry.getKey(), entry.getValue().getName()))
                            .collect(Collectors.toList()));

            siteWarLeagueStat.setStartDate(leagueStartDate);

        }

        normilizaWarInputData(siteAllWarLeagueStats);

        return siteAllWarLeagueStats;

    }

    private void normilizaWarInputData(List<WarStatsInputDto> siteAllWarLeagueStats) {

        for (WarStatsInputDto siteWarLeagueStat : siteAllWarLeagueStats) {
            siteWarLeagueStat.getPlayerWarStats().stream().filter(player -> player.getGamesGranted() == 0).forEach(
                    player -> {
                        player.setGamesGranted(1);
                        player.setGamesNotPlayed(1);
                    }
            );
            Collections.sort(siteWarLeagueStat.getPlayerWarStats(), Comparator.comparing(PlayerWarStatInputDto::getGamesWon)
                    .thenComparing(PlayerWarStatInputDto::getGamesLost)
                    .thenComparing(PlayerWarStatInputDto::getGamesNotPlayed)
                    .thenComparing(PlayerWarStatInputDto::getCards).reversed());
        }
    }

    @Override
    public List<WarStatsInputDto.PlayerWarStatInputDto> getPlayersNotParticipated(LocalDate date,
                                                                                  List<WarStatsInputDto.PlayerWarStatInputDto> participants) {

        return findPlayersNotParticipatedInWar(WarStatsInputDto.builder().playerWarStats(participants).build(), date).entrySet().stream()
                .map(entry -> WarStatsInputDto.PlayerWarStatInputDto.zeroFieldPlayerWarStatInputDto(entry.getKey(), entry.getValue().getName()))
                .collect(Collectors.toList());
    }


    public Map<String, Player> findPlayersNotParticipatedInWar(WarStatsInputDto playersInWar, LocalDate date) {
        Map<String, Player> playersNotParticipated = playerWeeklyStatsRepository.findByWeek(Week.fromDate(date)).stream()
                .collect(Collectors.toMap(pws -> pws.getPlayer().getTag(), pws -> pws.getPlayer()));

        Set<String> playersInWarSet = playersInWar.getPlayerWarStats().stream()
                .map(WarStatsInputDto.PlayerWarStatInputDto::getTag).collect(Collectors.toSet());

        playersInWarSet.forEach(playersNotParticipated::remove);

        return playersNotParticipated;

    }


}
