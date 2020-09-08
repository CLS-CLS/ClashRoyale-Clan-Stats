package org.lytsiware.clash.runners;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.war.domain.aggregation.PlayerAggregationWarStats;
import org.lytsiware.clash.war.domain.aggregation.PlayerAggregationWarStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import javax.transaction.Transactional;
import java.util.List;

@Slf4j
public class UpdateWarScoreRunner implements CommandLineRunner {

    @Autowired
    PlayerAggregationWarStatsRepository playerAggregationWarStatsRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        List<PlayerAggregationWarStats> aggretationWarStats = playerAggregationWarStatsRepository.findAll();
        for (PlayerAggregationWarStats warStat : aggretationWarStats) {
            warStat.setScore((int) ((0.50 + 0.50 * warStat.getAvgWins()) * warStat.getAvgCards()));
            warStat.setTotalGamesMissed(warStat.getGamesNotPlayed() + warStat.getCollectionGamesMissed());
            playerAggregationWarStatsRepository.save(warStat);
        }

    }
}
