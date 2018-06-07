package org.lytsiware.clash.domain.war.league;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface WarLeagueRepository extends JpaRepository<WarLeague, LocalDate>, WarLeagueRepositoryCustom {


    List<WarLeague> findByStartDateBetween(LocalDate from, LocalDate to);

    @Query("select l from WarLeague l join fetch l.playerWarStats ps where l.startDate between :fromDate and :toDate")
    List<WarLeague> findAllBetweenStartDateEagerFetchPlayerStats(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);


}
