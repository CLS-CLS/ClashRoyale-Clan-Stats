package org.lytsiware.clash.domain.war.playerwarstat;


import org.lytsiware.clash.domain.war.league.WarLeague;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PlayerWarStatsRepository extends JpaRepository<PlayerWarStat, Long> {

    List<PlayerWarStat> findAllByWarLeague(WarLeague warLeague);

    //lol
    @EntityGraph(value = "playerWarStat.graph", type = EntityGraph.EntityGraphType.LOAD)
    List<PlayerWarStat> findFirst20ByPlayerTagAndWarLeagueStartDateLessThanEqualOrderByWarLeagueStartDateDesc(String tag, LocalDate startDate);

    @Query(nativeQuery = true,
            value = " select PWS.* from PLAYER_WAR_STAT AS PWS left join WAR_LEAGUE AS WL " +
                    " on PWS.WAR_LEAGUE_ID=WL.ID " +
                    " where WL.START_DATE <= :startDate " +
                    " and PWS.CARDS_WON > 0 " +
                    " and PWS.PLAYER_TAG = :tag" +
                    " order by WL.START_DATE DESC " +
                    " limit :n")
    List<PlayerWarStat> findFirstNthParticipatedWarStats(@Param("tag") String tag, @Param("startDate") LocalDate startDate, @Param("n") int n);


}
