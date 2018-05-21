package org.lytsiware.clash.domain.war.aggregation;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.lytsiware.clash.domain.player.Player;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class PlayerAggregationWarStatsPK implements Serializable {

    Player player;
    LocalDate dateFrom;
    Integer leagueSpan;
}
