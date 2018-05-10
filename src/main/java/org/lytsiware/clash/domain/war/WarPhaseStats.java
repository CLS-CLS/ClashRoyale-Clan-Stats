package org.lytsiware.clash.domain.war;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Optional;

@Embeddable
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarPhaseStats {

    private Integer gamesGranted;

    @Column(name = "WAR_GAMES_WON")
    private Integer gamesWon;

    @Column(name = "WAR_GAMES_LOST")
    private Integer gamesLost;

    public Integer getGamesPlayed() {
        return Optional.ofNullable(gamesWon).orElse(0) + Optional.ofNullable(gamesLost).orElse(0);
    }

    public Integer getGamesNotPlayed() {
        return Optional.ofNullable(gamesGranted).orElse(0) - getGamesPlayed();
    }

    public boolean hasParticipated() {
        return Optional.of(gamesWon).orElse(0) + Optional.ofNullable(gamesLost).orElse(0) > 0;
    }

}
