package org.lytsiware.clash.service.war;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.player.PlayerInOut;
import org.lytsiware.clash.domain.player.PlayerRepository;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStatsRepository;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStats;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStatsRepository;
import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.league.WarLeagueRepository;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStatsRepository;
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
    @Autowired
    private PlayerWarStatsRepository playerWarStatsRepository;

    @Autowired
    private WarLeagueService warLeagueService;

    @Autowired
    private WarLeagueRepository warLeagueRepository;

    @Autowired
    private PlayerWeeklyStatsRepository playerWeeklyStatsRepository;

    @Autowired
    private PlayerAggregationWarStatsService playerAggregationWarStatsService;

    @Autowired
    private PlayerAggregationWarStatsRepository playerAggregationWarStatsRepository;

    @Autowired
    private WarInputServiceImpl warInputService;

    @Autowired
    private StatsRoyaleDateParse statsRoyaleDateParse;

    @Autowired
    private PlayerCheckInService playerCheckInService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private Clock clock;


    @Override
    public PlaywerWarStatsWithAvgsDto getLatestPlayerWarStatsUntil(String tag, LocalDate untilDate) {

        Map<LocalDate, PlayerWarStat> playerWarStats = playerWarStatsRepository.findFirst40ByPlayerTagAndWarLeagueStartDateLessThanEqualOrderByWarLeagueStartDateDesc(tag, untilDate)
                .stream()
                .filter(playerWarStat -> playerWarStat.getWarPhaseStats() != null) //filter out incomplete data )
                .collect(Utils.collectToMap(pws -> pws.getWarLeague().getStartDate(), Function.identity()));

        Map<LocalDate, PlayerAggregationWarStats> playerAggrWarStats = playerAggregationWarStatsService.findWarStatsForPlayer(tag, WarConstants.leagueSpan, untilDate)
                .stream().collect(Utils.collectToMap(PlayerAggregationWarStats::getDate, Function.identity()));

        return new PlaywerWarStatsWithAvgsDto(playerWarStats, playerAggrWarStats);

    }


    @Override
    public void saveWarStatsAndUpdateStatistics(List<PlayerWarStat> statsList) throws EntityExistsException {
        WarLeague warLeague = statsList.get(0).getWarLeague();
        WarLeague warLeagueDb = warLeagueRepository.findByStartDate(warLeague.getStartDate()).orElse(null);
        if (warLeagueDb != null) {
            updateWarStatsAndUpdateStatistics(warLeagueDb, warLeague, statsList);
            warLeague = warLeagueDb;
        }
        //TODO check transient
        Map<String, Player> playersDb = playerRepository.loadAll();
        statsList.stream().map(PlayerWarStat::getPlayer).filter(player -> !playersDb.containsKey(player.getTag())).forEach(player -> log.info("TRANSIENT PLAYER {}", player));
        playerWarStatsRepository.saveAll(statsList);
        playerWarStatsRepository.flush();
        warLeagueService.calculateLeagueAvgsAndSave(warLeague);
        playerAggregationWarStatsService.recalculateAndUpdateWarStatsForLeagues(Collections.singletonList(warLeague));
    }

    private void updateWarStatsAndUpdateStatistics(WarLeague warLeagueDb, WarLeague warLeague, List<PlayerWarStat> statsList) {
        warLeagueDb.setRank(warLeague.getRank());
        warLeagueDb.setTrophies(warLeague.getTrophies());
        playerWarStatsRepository.deleteAll(warLeagueDb.getPlayerWarStats());
        playerAggregationWarStatsRepository.deleteInBatch(playerAggregationWarStatsRepository.findByDateAndLeagueSpan(warLeague.getStartDate(),
                WarConstants.leagueSpan));
        warLeagueDb.clearPlayerWarStats();
        statsList.forEach(playerWarStat -> playerWarStat.setWarLeague(warLeagueDb));
        playerWarStatsRepository.flush();
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
    public List<PlayerWarStat> findWarStatsForWar(Integer deltaWar) {
        log.info("START findWarStatsForWar for deltawar {}", deltaWar);
        List<WarLeague> warLeague = warLeagueRepository.findAll(PageRequest.of(deltaWar, 1, Sort.by(Sort.Direction.DESC, "startDate"))).getContent();
        if (warLeague.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        return playerWarStatsRepository.findAllByWarLeague(warLeague.get(0));
    }

}
