package org.lytsiware.clash.war.domain.playerwarstat;

import lombok.*;
import org.lytsiware.clash.core.domain.player.Player;
import org.lytsiware.clash.war.domain.league.WarLeague;

import javax.persistence.*;
import java.util.Optional;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"PLAYER_TAG", "WAR_LEAGUE_ID"}))
@NamedEntityGraphs({@NamedEntityGraph(name = "playerWarStat.graph", attributeNodes = {@NamedAttributeNode("player"), @NamedAttributeNode("warLeague")})})
@SequenceGenerator(name = "wars_sequence", sequenceName = "PWARS_SEQUENCE")
@ToString
public class PlayerWarStat {

    @Id
    @Setter(AccessLevel.PACKAGE)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wars_sequence")
    private Long id;

    @ManyToOne
    private Player player;

    @ManyToOne(optional = false)
    private WarLeague warLeague;

    @Embedded
    private WarPhaseStats warPhaseStats;

    @Embedded
    private CollectionPhaseStats collectionPhaseStats;

    @Column(nullable = false)
    @Builder.Default
    private boolean warEligible = true;


    public void setWarLeague(WarLeague warLeague) {
        this.warLeague = warLeague;
        if (warLeague != null) {
            warLeague.addPlayerWarStats(this);
        }
    }


    public static class PlayerWarStatBuilder {
        private WarLeague warLeague;

        /**
         * @deprecated Do not use this method as it does not bind the reverse relationship.
         * Use the {@link PlayerWarStat#setWarLeague(WarLeague)} instead on the object created by the builder
         */
        @Deprecated
        public PlayerWarStatBuilder warLeague(WarLeague warLeague) {
            this.warLeague = warLeague;
            return this;
        }
    }

    public boolean hasParticipated() {
        return Optional.of(collectionPhaseStats).map(CollectionPhaseStats::getCardsWon).orElse(0) > 0;
    }

    public boolean hasAbandonedWar() {
        return hasParticipated() && (warPhaseStats == null || (
                warPhaseStats.getGamesGranted() > 0 && warPhaseStats.getGamesNotPlayed() > 0));
    }
}
