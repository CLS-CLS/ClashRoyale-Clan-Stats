package org.lytsiware.clash.domain.war;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PlayerWarStatsRepository extends CrudRepository<PlayerWarStat, Long> {

    List<PlayerWarStat> findAllByWarLeague(WarLeague warLeague);

    @EntityGraph(value = "PlayerWarStat.detail", type = EntityGraph.EntityGraphType.LOAD)
    List<PlayerWarStat> findByWarLeagueIn(List<WarLeague> leagues);
}
