package org.lytsiware.clash.dto;

import lombok.Data;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.war.PlayerWarStat;

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

    public ClansWarGlobalStatsDto(Map<Player, List<PlayerWarStat>> playerWarStats) {
        this.playerWarStats = playerWarStats.values().stream().map(PlayerWarBiWeeklyStatsDto::new).collect(Collectors.toList());
        int totalWarsParticipated = 0;
        int totalGamesGranted = 0;
        for (PlayerWarBiWeeklyStatsDto pws : this.playerWarStats) {
            avgCards += (int) (pws.getAverageCardsWon() * pws.getWarsParticipated());
            totalWarsParticipated += pws.getWarsParticipated();
        }
        if (totalWarsParticipated != 0) {
            avgCards = avgCards / totalWarsParticipated;
        }
        for (PlayerWarStat pws : playerWarStats.values().stream().flatMap(List::stream).collect(Collectors.toList())) {
            avgWins += pws.getWarPhaseStats().getGamesWon();
            totalGamesGranted += pws.getWarPhaseStats().getGamesGranted();
        }
        if (totalGamesGranted != 0) {
            avgWins = avgWins / (double) totalGamesGranted;
        }
    }


}
