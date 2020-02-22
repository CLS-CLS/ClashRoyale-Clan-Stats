package org.lytsiware.clash.service.war;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.Application;
import org.lytsiware.clash.domain.LockService;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.player.PlayerInOut;
import org.lytsiware.clash.domain.player.PlayerRepository;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStats;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStatsRepository;
import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.league.WarLeagueRepository;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStatsRepository;
import org.lytsiware.clash.dto.PlaywerWarStatsWithAvgsDto;
import org.lytsiware.clash.dto.war.input.WarStatsInputDto;
import org.lytsiware.clash.service.clan.PlayerCheckInService;
import org.lytsiware.clash.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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
    private PlayerAggregationWarStatsService playerAggregationWarStatsService;

    @Autowired
    private PlayerAggregationWarStatsRepository playerAggregationWarStatsRepository;

    @Autowired
    private PlayerCheckInService playerCheckInService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private LockService lockService;


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
    @Async(Application.Config.WAR_INPUT_EXECUTOR)
    public CompletableFuture<String> saveWarStatsAndUpdateStatistics(List<PlayerWarStat> statsList) throws EntityExistsException {
        try {
            lock();
            WarLeague warLeague = statsList.get(0).getWarLeague();
            WarLeague warLeagueDb = warLeagueRepository.findByStartDate(warLeague.getStartDate()).orElse(null);
            saveTransientPlayers(statsList, warLeague);
            if (warLeagueDb != null) {
                updateWarStatsAndUpdateStatistics(warLeagueDb, warLeague, statsList);
                warLeague = warLeagueDb;
            }


            warLeagueService.calculateLeagueAvgsAndSave(warLeague);
            playerWarStatsRepository.saveAll(statsList);
            playerWarStatsRepository.flush();
            playerAggregationWarStatsService.recalculateAndUpdateStats(warLeague);
            return CompletableFuture.completedFuture("");
        } catch (Exception ex) {
            return CompletableFuture.completedFuture(ex.getMessage());
        } finally {
            unlock();
        }
    }

    private void unlock() {
        lockService.unlock();
    }

    private void lock() {
        if (!lockService.lock()) {
            throw new RuntimeException("Could not lock");
        }
    }


    /**
     * A player will not be recorded when transient when he has joined the clan before the war starts but has left the
     * clan before the scheduler run. Hence we checkin the player just before the start date of the warLeague and
     * checkout the player 12 hours later.
     */
    private void saveTransientPlayers(List<PlayerWarStat> statsList, WarLeague warLeague) {
        Map<String, Player> playersDb = playerRepository.loadAll();
        List<Player> transientPlayers = statsList.stream().map(PlayerWarStat::getPlayer).filter(player -> !playersDb.containsKey(player.getTag())).
                peek(player -> log.info("TRANSIENT PLAYER {}", player)).collect(Collectors.toList());

        for (Player transientPlayer : transientPlayers) {
            playerCheckInService.checkinPlayer(transientPlayer, warLeague.getStartDate().atTime(warLeague.getTime().minusHours(1)));
            playerCheckInService.checkoutPlayer(transientPlayer.getTag(), warLeague.getStartDate().atTime(warLeague.getTime().plusHours(12)));
        }
    }

    private void updateWarStatsAndUpdateStatistics(WarLeague warLeagueDb, WarLeague warLeague, List<PlayerWarStat> statsList) {
        warLeagueDb.setRank(warLeague.getRank());
        warLeagueDb.setTrophies(warLeague.getTrophies());
        playerWarStatsRepository.deleteInBatch(warLeagueDb.getPlayerWarStats());
        playerAggregationWarStatsRepository.deleteInBatch(playerAggregationWarStatsRepository.findByDateAndLeagueSpan(warLeague.getStartDate(),
                WarConstants.leagueSpan));
        warLeagueDb.getPlayerWarStats().clear();
        playerWarStatsRepository.flush();
        statsList.forEach(playerWarStat -> playerWarStat.setWarLeague(warLeagueDb));
        playerWarStatsRepository.saveAll(statsList);
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
