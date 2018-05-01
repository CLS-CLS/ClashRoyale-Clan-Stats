package org.lytsiware.clash.domain.war;

import org.lytsiware.clash.domain.player.Player;

import javax.persistence.*;


public class PlayerWarStats {

    @Id
    private Long id;

    @ManyToOne
    private Player player;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private WarLeague warLeague;

    @Embedded
    private WarPhaseStats warPhaseStats;

    @Embedded
    private CollectionPhaseStats collectionPhaseStats;

    private boolean warEligible;





}
