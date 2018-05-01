package org.lytsiware.clash.domain.war;

import javax.persistence.Embeddable;

@Embeddable
public class WarPhaseStats {

    private boolean participated;

    private Integer gamesGranted;

    private Integer gamesPlayed;

    private Integer gamesWon;

    private Integer gamesLost;

}
