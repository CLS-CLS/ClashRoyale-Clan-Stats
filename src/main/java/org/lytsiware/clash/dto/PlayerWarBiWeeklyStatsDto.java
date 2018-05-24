package org.lytsiware.clash.dto;

import lombok.Data;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStats;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class PlayerWarBiWeeklyStatsDto implements Serializable {
    private String name;
    private String tag;
    private int numberOfWars;
    private Double averageCardsWon;
    private int warsParticipated;
    private Integer crownsWon;
    private Integer crownsLost;
    private Integer gamesNotPlayed;
    private Double winRatio;
    private Integer score;
    private boolean inClan;


    public PlayerWarBiWeeklyStatsDto(PlayerAggregationWarStats playerWarStats) {
        name = playerWarStats.getPlayer().getName();
        tag = playerWarStats.getPlayer().getTag();
        numberOfWars = playerWarStats.getWarsEligibleForParticipation();
        warsParticipated = playerWarStats.getWarsParticipated();
        averageCardsWon = playerWarStats.getAvgCards();
        crownsWon = playerWarStats.getGamesWon();
        gamesNotPlayed = playerWarStats.getGamesNotPlayed();
        crownsLost = playerWarStats.getGamesGranted() - playerWarStats.getGamesWon();
        winRatio = playerWarStats.getAvgWins();
        score =  playerWarStats.getScore();
        inClan = playerWarStats.getPlayer().getInClan();
    }

}
