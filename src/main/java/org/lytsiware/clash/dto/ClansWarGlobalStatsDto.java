package org.lytsiware.clash.dto;

import lombok.Data;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStats;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ClansWarGlobalStatsDto implements Serializable {

    private int avgCards;
    private double avgWins;

    List<PlayerWarBiWeeklyStatsDto> playerWarStats = new ArrayList<>();

    public ClansWarGlobalStatsDto(List<PlayerAggregationWarStats> playerAggregationWarStats) {
        this.playerWarStats = playerAggregationWarStats.stream().map(PlayerWarBiWeeklyStatsDto::new).collect(Collectors.toList());
        int totalGamesGranted = playerWarStats.stream().mapToInt(PlayerWarBiWeeklyStatsDto::getGamesGranted).sum();
        if (totalGamesGranted != 0) {
            avgWins = playerWarStats.stream().mapToInt(PlayerWarBiWeeklyStatsDto::getCrownsWon).sum() / (double) totalGamesGranted;
        }
        int totalWarsParticipated = playerWarStats.stream().mapToInt(PlayerWarBiWeeklyStatsDto::getWarsParticipated).sum();
        if (totalGamesGranted != 0) {
            avgCards = (int) playerWarStats.stream().mapToDouble(pws -> pws.getAverageCardsWon() * pws.getWarsParticipated()).sum() / totalWarsParticipated;
        }
    }

}
