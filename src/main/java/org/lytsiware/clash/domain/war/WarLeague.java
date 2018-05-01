package org.lytsiware.clash.domain.war;

import javax.persistence.CascadeType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.time.LocalDate;
import java.util.List;

public class WarLeague {

    @Id
    private Long id;

    private LocalDate startDate;

    @ManyToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private WarSeason warSeason;

    @OneToMany(mappedBy = "warLeague")
    private List<PlayerWarStats> playerWarStats;





}
