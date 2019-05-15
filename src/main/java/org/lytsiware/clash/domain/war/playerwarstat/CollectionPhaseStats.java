package org.lytsiware.clash.domain.war.playerwarstat;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionPhaseStats {

    @Column(name = "COLLECTION_GAMES_PLAYED")
    private Integer gamesPlayed;

    @Column(name = "COLLECTION_GAMES_WON")
    private Integer gamesWon;

    @Column(name = "COLLECTION_GAMES_LOST")
    private Integer gamesLost;

    @Column(nullable = false)
    private Integer cardsWon;

}
