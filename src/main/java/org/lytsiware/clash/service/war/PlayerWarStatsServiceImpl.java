package org.lytsiware.clash.service.war;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.player.PlayerInOut;
import org.lytsiware.clash.domain.player.PlayerRepository;
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
import org.lytsiware.clash.service.clan.PlayerCheckInService;
import org.lytsiware.clash.service.integration.statsroyale.StatsRoyaleDateParse;
import org.lytsiware.clash.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private WarInputServiceImpl warInputService;
    private StatsRoyaleDateParse statsRoyaleDateParse;
    private PlayerCheckInService playerCheckInService;
    private PlayerRepository playerRepository;
    private Clock clock;

    @Autowired
    public PlayerWarStatsServiceImpl(PlayerWarStatsRepository playerWarStatsRepository,
                                     WarLeagueService warLeagueService,
                                     WarLeagueRepository warLeagueRepository,
                                     PlayerWeeklyStatsRepository playerWeeklyStatsRepository,
                                     PlayerAggregationWarStatsService playerAggregationWarStatsService,
                                     PlayerCheckInService playerCheckInService,
                                     PlayerRepository playerRepository,
                                     Clock clock) {
        this.playerWarStatsRepository = playerWarStatsRepository;
        this.warLeagueService = warLeagueService;
        this.warLeagueRepository = warLeagueRepository;
        this.playerWeeklyStatsRepository = playerWeeklyStatsRepository;
        this.playerAggregationWarStatsService = playerAggregationWarStatsService;
        this.playerCheckInService = playerCheckInService;
        this.playerRepository = playerRepository;
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

        Map<LocalDate, PlayerWarStat> playerWarStats = playerWarStatsRepository.findFirst40ByPlayerTagAndWarLeagueStartDateLessThanEqualOrderByWarLeagueStartDateDesc(tag, untilDate)
                .stream().collect(Utils.collectToMap(pws -> pws.getWarLeague().getStartDate(), Function.identity()));

        Map<LocalDate, PlayerAggregationWarStats> playerAggrWarStats = playerAggregationWarStatsService.findWarStatsForPlayer(tag, WarConstants.leagueSpan, untilDate)
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
                .flatMap(warLeague -> warLeagueRepository.findFirstNthWarLeaguesAfterDate(warLeague.getStartDate(), WarConstants.leagueSpan).stream())
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
        updateWarStatsForAffectedLeagues(Collections.singletonList(warLeague));
    }


    @Override
    public Map<Player, PlayerInOut> findPlayersNotParticipatedInWar(WarStatsInputDto playersInWar, LocalDateTime date, Integer faultTolleranceInMinutes) {
        if (faultTolleranceInMinutes == null) {
            faultTolleranceInMinutes = 0;
        }
        Set<String> playersInWarSet = playersInWar.getPlayerWarStats().stream()
                .map(WarStatsInputDto.PlayerWarStatInputDto::getTag).collect(Collectors.toSet());


        Set<PlayerInOut> checkedInAtDate = new HashSet<>(playerCheckInService.findCheckedInPlayersAtDate(date));
        Set<PlayerInOut> checkedInWithFaultTollerance = new HashSet<>(playerCheckInService.findCheckedInPlayersAtDate(date.plusMinutes(faultTolleranceInMinutes)));
        checkedInAtDate.addAll(checkedInWithFaultTollerance);

        Map<String, PlayerInOut> allCheckedInPlayers = checkedInAtDate.stream().collect(Collectors.toMap(PlayerInOut::getTag, Function.identity()));

        List<String> checkedInPlayersNotParticipated = checkedInAtDate.stream()
                .map(PlayerInOut::getTag).filter(tag -> !playersInWarSet.contains(tag)).collect(Collectors.toList());

        //better load all and filter them out than hit n times the db for each one
        Map<String, Player> allPlayers = playerRepository.loadAll();

        return checkedInPlayersNotParticipated.stream().collect(Collectors.toMap(allPlayers::get, allCheckedInPlayers::get));
    }

    @Override
    public List<PlayerWarStat> findLatestWarStatsForWar(Integer deltaWar) {
        log.info("START findLatestWarStatsForWar for deltawar {}", deltaWar);
        List<WarLeague> warLeague = warLeagueRepository.findAll(PageRequest.of(deltaWar, 1, Sort.by(Sort.Direction.DESC, "startDate"))).getContent();
        if (warLeague.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        return playerWarStatsRepository.findAllByWarLeague(warLeague.get(0));
    }

}
