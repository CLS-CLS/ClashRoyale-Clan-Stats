package org.lytsiware.clash.domain.war;

import lombok.*;
import org.lytsiware.clash.domain.player.Player;

import javax.persistence.*;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"PLAYER_TAG", "WAR_LEAGUE_ID"}))
@NamedEntityGraph(name = "PlayerWarStat.detail", attributeNodes = @NamedAttributeNode("player"))
public class PlayerWarStat {

    @Id
    @Setter(AccessLevel.PACKAGE)
    @GeneratedValue
    private Long id;

    @OneToOne
    private Player player;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, optional = false)
    private WarLeague warLeague;

    @Embedded
    private WarPhaseStats warPhaseStats;

    @Embedded
    private CollectionPhaseStats collectionPhaseStats;

    @Column(nullable = false)
    @Builder.Default
    private boolean warEligible = true;

    public PlayerWarStat(Long id, Player player, WarLeague warLeague, WarPhaseStats warPhaseStats, CollectionPhaseStats collectionPhaseStats, boolean warEligible) {
        this.player = player;
        this.warPhaseStats = warPhaseStats;
        this.collectionPhaseStats = collectionPhaseStats;
        this.warEligible = warEligible;
        setWarLeague(warLeague);
    }

    public void setWarLeague(WarLeague warLeague) {
        this.warLeague = warLeague;
        warLeague.addPlayerWarStats(this);
    }
}
