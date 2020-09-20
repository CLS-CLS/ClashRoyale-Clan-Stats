package org.lytsiware.clash.war2.repository;

import org.lytsiware.clash.war2.domain.RiverRace;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;


public interface RiverRaceRepository extends JpaRepository<RiverRace, Long>, PagingAndSortingRepository<RiverRace, Long> {

    @Query("select riverRace from RiverRace riverRace where riverRace.active = true ")
    Optional<RiverRace> activeRace();

    @Query("select riverRace from RiverRace riverRace where riverRace.active = false " +
            "order by riverRace.superCellCreatedDate ")
    List<RiverRace> logRace(Pageable pageable);

    @Query("select riverRace from RiverRace riverRace order by superCellCreatedDate desc")
    List<RiverRace> getRiverRace(Pageable pageable);


}
