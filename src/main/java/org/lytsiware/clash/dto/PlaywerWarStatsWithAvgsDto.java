package org.lytsiware.clash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStats;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class PlaywerWarStatsWithAvgsDto implements Serializable {

    private String tag;
    private String name;
    private boolean inClan;
    private String role;

    //    @Builder.Default
    private List<StatsDto> stats = new ArrayList<>();

    public PlaywerWarStatsWithAvgsDto(Map<LocalDate, PlayerWarStat> warStats, Map<LocalDate, PlayerAggregationWarStats> aggrWarStats) {
        Player player = warStats.values().stream().map(PlayerWarStat::getPlayer).findFirst().orElse(null);
        if (player == null) {
            return;
        }
        tag = player.getTag();
        name = player.getName();
        inClan = player.getInClan();
        role = player.getRole();

        warStats.keySet().stream().sorted(Comparator.reverseOrder()).forEach(localDate -> {
            PlayerWarStat pws = warStats.get(localDate);
            PlayerAggregationWarStats paws = aggrWarStats.getOrDefault(localDate, new PlayerAggregationWarStats());

            StatsDto statsDto = StatsDto.builder()
                    .leagueDate(pws.getWarLeague().getStartDate())
                    .gamesGranted(pws.getWarPhaseStats().getGamesGranted())
                    .gamesPlayed(pws.getWarPhaseStats().getGamesPlayed())
                    .gamesWon(pws.getWarPhaseStats().getGamesWon())
                    .gamesLost(pws.getWarPhaseStats().getGamesLost())
                    .gamesNotPlayed(pws.getWarPhaseStats().getGamesNotPlayed())
                    .cards(pws.getCollectionPhaseStats().getCardsWon())
                    .avgCards(paws.getAvgCards())
                    .avgScore(paws.getScore())
                    .avgWins(paws.getAvgWins())
                    .totalCards(paws.getTotalCards()).build();
            stats.add(statsDto);
        });
    }

    @Data
    @Builder
    public static class StatsDto implements Serializable {
        private LocalDate leagueDate;

        private Integer gamesGranted;
        private Integer gamesPlayed;
        private Integer gamesWon;
        private Integer gamesLost;
        private Integer gamesNotPlayed;
        private Integer cards;

        //aggr
        private Double avgWins;
        private Double avgCards;
        private Integer avgScore;
        private Integer totalCards;
    }
}
