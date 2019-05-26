package org.lytsiware.clash.domain.war.league;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.lytsiware.clash.domain.war.WarSeason;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.service.war.WarConstants;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@SequenceGenerator(name = "wars_sequence", sequenceName = "PWARS_SEQUENCE")
public class WarLeague {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wars_sequence")
    private Long id;

    private LocalDate startDate;

    private LocalTime time;

    private Integer rank;

    private String name;

    private Integer trophies;

    private Integer teamCardAvg;

    private Integer teamTotalCards;

    private Double teamWinRatio;

    private Integer teamScore;

    private Integer totalTrophies;


    @Transient
    @Setter(AccessLevel.NONE)
    private LocalDate endDate;

    //TODO war season
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private WarSeason warSeason;

    @OneToMany(mappedBy = "warLeague")
    @Setter(AccessLevel.PACKAGE)
    private Set<PlayerWarStat> playerWarStats = new HashSet<>();

    WarLeague() {
        this.startDate = LocalDate.now();
    }

    public WarLeague(LocalDateTime startDate) {
        this.startDate = startDate.toLocalDate();
        this.time = startDate.toLocalTime();

    }

    public void clearPlayerWarStats() {
        playerWarStats.stream().forEach(playerWarStat -> playerWarStat.setWarLeague(null));
        playerWarStats.clear();
    }

    public void addPlayerWarStats(PlayerWarStat playerWarStat) {
        this.playerWarStats.add(playerWarStat);
    }

    public LocalDate getEndDate() {
        return getStartDate().plusDays(WarConstants.WAR_DURATION - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WarLeague warLeague = (WarLeague) o;

        return startDate.equals(warLeague.startDate);
    }

    @Override
    public int hashCode() {
        return startDate.hashCode();
    }
}
