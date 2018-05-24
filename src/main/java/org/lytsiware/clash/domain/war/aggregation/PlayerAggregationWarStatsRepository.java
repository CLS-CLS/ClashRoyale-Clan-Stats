package org.lytsiware.clash.domain.war.aggregation;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface PlayerAggregationWarStatsRepository extends CrudRepository<PlayerAggregationWarStats, PlayerAggregationWarStatsPK> {


    List<PlayerAggregationWarStats> findByDateAndLeagueSpan(LocalDate startDate, int leagueSpan);

}
