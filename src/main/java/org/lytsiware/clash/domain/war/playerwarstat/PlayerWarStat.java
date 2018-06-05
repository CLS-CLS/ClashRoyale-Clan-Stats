package org.lytsiware.clash.domain.war.playerwarstat;

import lombok.*;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.war.league.WarLeague;

import javax.persistence.*;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"PLAYER_TAG", "WAR_LEAGUE_START_DATE"}))
@NamedEntityGraphs({@NamedEntityGraph(name = "playerWarStat.graph", attributeNodes = {@NamedAttributeNode("player"), @NamedAttributeNode("warLeague")})})
@ToString
@SequenceGenerator(name = "PWARS_SEQUENCE", sequenceName = "PWARS_SEQUENCE", initialValue = 100)
public class PlayerWarStat {

    @Id
    @Setter(AccessLevel.PACKAGE)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PWARS_SEQUENCE")
    private Long id;

    @ManyToOne
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
