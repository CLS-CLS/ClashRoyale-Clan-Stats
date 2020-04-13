package org.lytsiware.clash.service.calculation.war;

import lombok.Getter;
import lombok.Setter;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;

import java.util.Set;

@Getter
@Setter
public class WarLeagueAvgCalculationContext {

    /**
     * the war results of each player
     */
    Set<PlayerWarStat> statsList;

    /**
     * trophies won or lost at the end of the war
     */
    Integer trophiesDelta;

    /**
     * The current trophies of the clan
     */
    Integer currentTrophies;


}
