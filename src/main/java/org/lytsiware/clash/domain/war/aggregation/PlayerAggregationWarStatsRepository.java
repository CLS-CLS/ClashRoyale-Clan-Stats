package org.lytsiware.clash.domain.war.aggregation;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PlayerAggregationWarStatsRepository extends JpaRepository<PlayerAggregationWarStats, Long> {

    @EntityGraph(value = "playerAggregationService.graph", type = EntityGraph.EntityGraphType.LOAD)
    List<PlayerAggregationWarStats> findByDateAndLeagueSpan(LocalDate startDate, int leagueSpan);

    @EntityGraph(value = "playerAggregationService.graph", type = EntityGraph.EntityGraphType.LOAD)
    List<PlayerAggregationWarStats> findFirst40ByPlayerTagAndLeagueSpanAndDateLessThanEqualOrderByDateDesc(String tag, int leagueSpan, LocalDate untilDate);
}
