package org.lytsiware.clash.war.domain.playerwarstat;


import org.lytsiware.clash.war.domain.league.WarLeague;
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
    List<PlayerWarStat> findFirst40ByPlayerTagAndWarLeagueStartDateLessThanEqualOrderByWarLeagueStartDateDesc(String tag, LocalDate startDate);

    @Query(nativeQuery = true,
            value = " select PWS.* from PLAYER_WAR_STAT AS PWS left join WAR_LEAGUE AS WL " +
                    " on PWS.WAR_LEAGUE_ID=WL.ID " +
                    " where WL.START_DATE <= :startDate " +
                    " and PWS.CARDS_WON > 0 " +
                    " and PWS.PLAYER_TAG = :tag" +
                    " order by WL.START_DATE DESC " +
                    " limit :n")
    List<PlayerWarStat> findFirstNthParticipatedWarStats(@Param("tag") String tag, @Param("startDate") LocalDate startDate, @Param("n") int n);

    @Query(nativeQuery = true,
            value = " select * from" +
                    "   (with filtered as (" +
                    "      select pws.*, l.START_DATE " +
                    "      from PLAYER_WAR_STAT pws " +
                    "      join WAR_LEAGUE as l on (pws.WAR_LEAGUE_ID = l.ID) " +
                    "      where l.START_DATE <= :startDate " +
                    "      and pws.CARDS_WON > 0 " +
                    "      and pws.PLAYER_TAG in (" +
                    "          select PLAYER_TAG from PLAYER_WAR_STAT " +
                    "          join WAR_LEAGUE as wl on (wl.ID = WAR_LEAGUE_ID) " +
                    "          where START_DATE = :startDate )" +
                    "      order by l.START_DATE)" +
                    "   select row_number() over (partition by PLAYER_TAG order by START_DATE desc) as row_num, * from filtered) " +
                    " as filtered_and_numbered" +
                    " where row_num <= :n")
    List<PlayerWarStat> findFirstNthParticipatedWarStats(@Param("startDate") LocalDate startDate, @Param("n") int n);

    @Query("select pws from PlayerWarStat pws JOIN FETCH pws.warLeague where pws.player.tag = :tag " +
            " and pws.warLeague.startDate between :fromDate and :toDate " +
            " order by pws.warLeague.startDate desc")
    List<PlayerWarStat> findBetweenDatesForPlayer(@Param("tag") String tag, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);


}
