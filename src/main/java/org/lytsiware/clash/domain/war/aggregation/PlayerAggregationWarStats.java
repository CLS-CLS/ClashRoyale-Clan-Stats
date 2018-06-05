package org.lytsiware.clash.domain.war.aggregation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lytsiware.clash.domain.player.Player;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PlayerAggregationWarStatsPK.class)
@Entity
@NamedEntityGraphs(@NamedEntityGraph(name = "playerAggregationService.graph", attributeNodes = @NamedAttributeNode("player")))
public class PlayerAggregationWarStats {

    @Id
    @ManyToOne
    Player player;

    @Id
    LocalDate date;

    @Id
    Integer leagueSpan;

    Double avgWins;
    Double avgCards;
    Integer totalCards;
    Integer gamesWon;
    Integer gamesGranted;
    Integer gamesNotPlayed;
    Integer warsParticipated;
    Integer warsEligibleForParticipation;
    Integer score;

}
