package org.lytsiware.clash.service.calculation.war;


import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStats;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface WarStatsCalculationService {

    /**
     * finds the players that were eligible for the war. The eligible players are the ones that
     * 1. were in clan when the war started
     * 2. joined the clan after the war started and participated in the war
     * <p>**limitations**
     * <p>Players that joined the clan after the war has started but did not participated in the war
     * are not considered as eligible because they may not be allowed to (i.e the have active war in previous clan)
     * Also players that have joined the clan at soon enough before the war started may also not allowed to participate,
     * but chances are they can (worst case is 50% not to be able to take place) so we count them as eligible.
     *
     * @param warCalculationContext
     * @return a set of eligible tag representing the players that are eligible for war
     */
    static Set<String> findPlayersEligibleForWar(WarCalculationContext warCalculationContext) {
        assert warCalculationContext.getPlayersInClanAtDate() != null;
        assert warCalculationContext.getWarLeagues() != null;
        assert warCalculationContext.getWarLeagues().size() > 0;


        // a player can join the clan after the war has begun and be able to join the war (though he will not appear in playerInClanAtDate).
        // Also add those who joined the war
        Set<String> eligiblePlayers = warCalculationContext.getWarLeagues().get(0).getPlayerWarStats().stream()
                .map(PlayerWarStat::getPlayer).map(Player::getTag)
                .collect(Collectors.toSet());
        eligiblePlayers.addAll(warCalculationContext.getPlayersInClanAtDate());
        return eligiblePlayers;


    }

    /**
     * Calculates the stats of a league
     *
     * @param warCalculationContext contains all data needed for the calculation.
     */
    List<PlayerAggregationWarStats> calculateStats(WarCalculationContext warCalculationContext);

}
