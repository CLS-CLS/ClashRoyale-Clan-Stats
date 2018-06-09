package org.lytsiware.clash.domain.war.playerwarstat;


import org.lytsiware.clash.domain.war.league.WarLeague;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PlayerWarStatsRepository extends JpaRepository<PlayerWarStat, Long> {

    List<PlayerWarStat> findAllByWarLeague(WarLeague warLeague);

    @EntityGraph(value = "playerWarStat.graph", type = EntityGraph.EntityGraphType.LOAD)
    List<PlayerWarStat> findFirst20ByPlayerTagAndWarLeagueStartDateBeforeOrderByWarLeagueStartDateDesc(String tag, LocalDate startDate);


}
