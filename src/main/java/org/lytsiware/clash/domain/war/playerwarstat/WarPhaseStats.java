package org.lytsiware.clash.domain.war.playerwarstat;

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


    //TODO fix it . Should not return null
    //  In the worst case scenario:
    //  1. throw exception if not applicable
    //  2. rename to not standard bean method name, as this does not confort to bean naming defination
    //  (it is not a pure getter)
    //

    /**
     * @return the games not played or null if it is not applicable (aka the player has not participated in the war)
     */
    public Integer getGamesNotPlayed() {
        return gamesGranted > 0 ? gamesGranted - getGamesPlayed() : null;
    }


}
