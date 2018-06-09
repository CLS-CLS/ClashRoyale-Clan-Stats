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
@Entity
@NamedEntityGraphs(@NamedEntityGraph(name = "playerAggregationService.graph", attributeNodes = @NamedAttributeNode("player")))
@SequenceGenerator(name = "wars_sequence", sequenceName = "PWARS_SEQUENCE")
public class PlayerAggregationWarStats {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wars_sequence")
    Long id;

    @ManyToOne
    Player player;

    LocalDate date;

    Integer leagueSpan;

    Double avgWins;
    Integer avgCards;
    Integer totalCards;
    Integer gamesWon;
    Integer gamesGranted;
    Integer gamesNotPlayed;
    Integer warsParticipated;
    Integer warsEligibleForParticipation;
    Integer score;

}
