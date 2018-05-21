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
public class PlayerAggregationWarStats {

    @Id
    @ManyToOne
    Player player;

    @Id
    LocalDate dateFrom;

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