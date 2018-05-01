package org.lytsiware.clash.domain.war;

import javax.persistence.Embeddable;

@Embeddable
public class CollectionPhaseStats {

    private boolean participated;

    private Integer gamesPlayed;

    private Integer gamesWon;

    private Integer gamesLost;

    private Integer cardsWon;
}
