package org.lytsiware.clash.war.domain;

import lombok.*;
import org.lytsiware.clash.war.domain.league.WarLeague;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarSeason {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private LocalDate startDate;

    private Integer rank;

    @OneToMany(mappedBy = "warSeason")
    @Setter(AccessLevel.PACKAGE)
    private List<WarLeague> warLeagues;

    public WarSeason(LocalDate startDate) {
        this.startDate = startDate;
    }

}
