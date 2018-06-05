package org.lytsiware.clash.service.war;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStatsRepository;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStats;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStatsRepository;
import org.lytsiware.clash.domain.war.league.WarLeagueRepository;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStatsRepository;
import org.lytsiware.clash.dto.PlaywerWarStatsWithAvgsDto;
import org.lytsiware.clash.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@Transactional
@Slf4j
public class PlayerWarStatsServiceImpl implements PlayerWarStatsService {

    public static final int WAR_DURATION = 2;
    private PlayerWarStatsRepository playerWarStatsRepository;
    private WarLeagueRepository warLeagueRepository;
    private PlayerWeeklyStatsRepository playerWeeklyStatsRepository;
    private PlayerAggregationWarStatsRepository playerAggregationWarStatsRepository;
    private Clock clock;

    @Autowired
    public PlayerWarStatsServiceImpl(PlayerWarStatsRepository playerWarStatsRepository,
                                     WarLeagueRepository warLeagueRepository,
                                     PlayerWeeklyStatsRepository playerWeeklyStatsRepository,
                                     PlayerAggregationWarStatsRepository playerAggregationWarStatsRepository,
                                     Clock clock) {

        this.playerWarStatsRepository = playerWarStatsRepository;
        this.warLeagueRepository = warLeagueRepository;
        this.playerWeeklyStatsRepository = playerWeeklyStatsRepository;
        this.playerAggregationWarStatsRepository = playerAggregationWarStatsRepository;
        this.clock = clock;
    }

    @Override
    public List<PlayerWarStat> persistPlayerWarStats(List<PlayerWarStat> playerWarStats) {

        List<PlayerWarStat> persistedWarStats = new ArrayList<>();

        playerWarStatsRepository.saveAll(playerWarStats).forEach(persisted -> persistedWarStats.add(persisted));

        return persistedWarStats;

    }


    @Override
    public PlaywerWarStatsWithAvgsDto getPlayerWarStatsForWeek(String tag, LocalDate untilDate) {

        Map<LocalDate, PlayerWarStat> playerWarStats = playerWarStatsRepository.findFirst20ByPlayerTagAndWarLeagueStartDateBeforeOrderByWarLeagueStartDateDesc(tag, untilDate)
                .stream().collect(Utils.collectToMap(pws -> pws.getWarLeague().getStartDate(), Function.identity()));

        Map<LocalDate, PlayerAggregationWarStats> playerAggrWarStats = playerAggregationWarStatsRepository.findFirst20ByPlayerTagAndLeagueSpanAndDateBeforeOrderByDateDesc(tag, WarConstants.leagueSpan, untilDate)
                .stream().collect(Utils.collectToMap(PlayerAggregationWarStats::getDate, Function.identity()));

        return new PlaywerWarStatsWithAvgsDto(playerWarStats, playerAggrWarStats);

    }


}
