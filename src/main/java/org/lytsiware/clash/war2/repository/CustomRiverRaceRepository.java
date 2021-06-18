package org.lytsiware.clash.war2.repository;

import org.lytsiware.clash.war2.domain.RiverRace;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomRiverRaceRepository {
    List<RiverRace> getRiverRaces(Pageable pageable);
}
