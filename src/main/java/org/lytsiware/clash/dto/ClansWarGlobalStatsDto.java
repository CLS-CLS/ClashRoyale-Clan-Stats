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
	private int avgClanScore;

	List<PlayerWarStatsDto> playerWarStats = new ArrayList<>();

	public ClansWarGlobalStatsDto(List<PlayerAggregationWarStats> playerAggregationWarStats) {
		this.playerWarStats = playerAggregationWarStats.stream().map(PlayerWarStatsDto::new).collect(Collectors.toList());
		int totalGamesGranted = playerWarStats.stream().mapToInt(PlayerWarStatsDto::getGamesGranted).sum();
		if (totalGamesGranted != 0) {
			avgWins = playerWarStats.stream().mapToInt(PlayerWarStatsDto::getCrownsWon).sum() / (double) totalGamesGranted;
		}
		int totalWarsParticipated = playerWarStats.stream().mapToInt(PlayerWarStatsDto::getWarsParticipated).sum();
		if (totalGamesGranted != 0) {
			avgCards = (int) playerWarStats.stream().mapToDouble(pws -> pws.getAverageCardsWon() * pws.getWarsParticipated()).sum() / totalWarsParticipated;
		}
		avgClanScore = playerAggregationWarStats.stream().mapToInt(playerWarStats -> playerWarStats.getScore() * playerWarStats.getWarsParticipated()).sum() / totalWarsParticipated;

	}

}
