package org.lytsiware.clash.domain.war;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import java.util.Optional;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionPhaseStats {


    @Transient
    @Setter(AccessLevel.NONE)
    private Integer gamesPlayed;

    @Column(name = "COLLECTION_GAMES_WON")
    private Integer gamesWon;

    @Column(name = "COLLECTION_GAMES_LOST")
    private Integer gamesLost;

    @Column(nullable = false)
    private Integer cardsWon;

    public Integer getGamesPlayed() {
        return Optional.ofNullable(gamesWon).orElse(0) + Optional.ofNullable(gamesLost).orElse(0);
    }
}
