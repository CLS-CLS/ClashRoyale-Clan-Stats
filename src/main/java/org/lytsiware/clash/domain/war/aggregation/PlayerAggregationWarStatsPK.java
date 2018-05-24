package org.lytsiware.clash.domain.war.aggregation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lytsiware.clash.domain.player.Player;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerAggregationWarStatsPK implements Serializable {
    String player;
    LocalDate date;
    Integer leagueSpan;
}
