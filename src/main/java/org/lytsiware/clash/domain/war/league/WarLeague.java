package org.lytsiware.clash.domain.war.league;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lytsiware.clash.domain.war.WarSeason;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.service.war.WarConstants;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class WarLeague {

    @Id
    private LocalDate startDate;

    private Integer rank;

    private String name;

    private Integer trophies;

    @Transient
    @Setter(AccessLevel.NONE)
    private LocalDate endDate;

    //TODO war season
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private WarSeason warSeason;

    @OneToMany(mappedBy = "warLeague")
    @Setter(AccessLevel.PACKAGE)
    private Set<PlayerWarStat> playerWarStats = new HashSet<>();

    public WarLeague(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void addPlayerWarStats(PlayerWarStat playerWarStat) {
        this.playerWarStats.add(playerWarStat);
    }

    public LocalDate getEndDate() {
        return getStartDate().plusDays(WarConstants.leagueDays - 1);
    }

}
