package org.lytsiware.clash.domain.war;

import lombok.*;
import org.lytsiware.clash.domain.war.league.WarLeague;

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
    private LocalDate stardDate;

    private Integer rank;

    @OneToMany(mappedBy = "warSeason")
    @Setter(AccessLevel.PACKAGE)
    private List<WarLeague> warLeagues;

    public WarSeason(LocalDate startDate) {
        this.stardDate = startDate;
    }

}
