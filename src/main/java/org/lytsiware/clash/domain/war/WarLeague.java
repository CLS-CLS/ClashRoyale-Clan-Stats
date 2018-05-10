package org.lytsiware.clash.domain.war;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private LocalDate startDate;

    private Integer rank;

    private String name;

    private Integer trophies;

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
}
