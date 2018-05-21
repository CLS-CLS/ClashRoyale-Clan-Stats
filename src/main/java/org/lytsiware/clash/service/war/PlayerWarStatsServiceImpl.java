package org.lytsiware.clash.service.war;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStatsRepository;
import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.league.WarLeagueRepository;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStatsRepository;
import org.lytsiware.clash.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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
    public List<PlayerWarStat> persistPlayerWarStats(List<PlayerWarStat> playerWarStats) {

        List<PlayerWarStat> persistedWarStats = new ArrayList<>();

        playerWarStatsRepository.saveAll(playerWarStats).forEach(persisted -> persistedWarStats.add(persisted));

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


}
