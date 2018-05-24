package org.lytsiware.clash.dto;

import lombok.Data;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStats;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class ClansWarGlobalStatsDto implements Serializable {

    private int avgCards;
    private double avgWins;

    List<PlayerWarBiWeeklyStatsDto> playerWarStats = new ArrayList<>();

    public ClansWarGlobalStatsDto(List<PlayerAggregationWarStats> playerAggregationWarStats) {
        this.playerWarStats = playerAggregationWarStats.stream().map(PlayerWarBiWeeklyStatsDto::new).collect(Collectors.toList());
       //TODO avgCards, avfWins
    }


}
