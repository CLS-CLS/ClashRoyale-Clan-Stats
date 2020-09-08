package org.lytsiware.clash.war.domain.aggregation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lytsiware.clash.core.domain.player.Player;

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
    Integer collectionGamesMissed;
    Integer totalGamesMissed;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerAggregationWarStats that = (PlayerAggregationWarStats) o;

        if (!player.equals(that.player)) return false;
        if (!date.equals(that.date)) return false;
        return leagueSpan.equals(that.leagueSpan);
    }

    @Override
    public int hashCode() {
        int result = player.hashCode();
        result = 31 * result + date.hashCode();
        result = 31 * result + leagueSpan.hashCode();
        return result;
    }


}
