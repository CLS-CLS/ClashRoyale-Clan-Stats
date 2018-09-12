package org.lytsiware.clash.domain.war.aggregation;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface PlayerAggregationWarStatsRepository extends CrudRepository<PlayerAggregationWarStats, Long> {

    @EntityGraph(value = "playerAggregationService.graph", type = EntityGraph.EntityGraphType.LOAD)
    List<PlayerAggregationWarStats> findByDateAndLeagueSpan(LocalDate startDate, int leagueSpan);

    @EntityGraph(value = "playerAggregationService.graph", type = EntityGraph.EntityGraphType.LOAD)
    List<PlayerAggregationWarStats> findFirst40ByPlayerTagAndLeagueSpanAndDateBeforeOrderByDateDesc(String tag, int leagueSpan, LocalDate untilDate);
}
