package org.lytsiware.clash.domain.war;

import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.time.LocalDate;
import java.util.List;

public class WarSeason {
    @Id
    private Long id;

    private LocalDate stardDate;

    @OneToMany(mappedBy = "warSeason")
    private List<WarLeague> seasonLeagues;


}
