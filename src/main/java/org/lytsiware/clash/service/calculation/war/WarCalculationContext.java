package org.lytsiware.clash.service.calculation.war;

import lombok.Getter;
import lombok.Setter;
import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contains the data needed for the stats calculations for a league
 */
@Getter
@Setter
public class WarCalculationContext {

    /**
     * the number of leagues to aggregate data
     */
    int leagueSpan;

    /**
     * The league' s (we are calculating its stats) start date
     */
    private LocalDate leagueStartDate;

    /**
     * The latest Nth warleagues (includind the league we are calculating its stats
     */
    private List<WarLeague> warLeagues;

    /**
     * The players that were in tha clan at that date
     */
    private Set<String> playersInClanAtDate;

    /**
     * The players were eligible to play in the war
     */
    private Set<String> eligiblePlayers;

    /**
     * The latest Nth (where n is the {@link WarCalculationContext#leagueSpan}) stats for the wars the player HAS participated.
     */
    private Map<String, List<PlayerWarStat>> latestNthParticipationStats;


}
