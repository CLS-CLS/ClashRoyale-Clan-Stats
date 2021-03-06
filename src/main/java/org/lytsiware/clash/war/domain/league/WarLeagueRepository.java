package org.lytsiware.clash.war.domain.league;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WarLeagueRepository extends JpaRepository<WarLeague, Long>, WarLeagueRepositoryCustom {


    List<WarLeague> findByStartDateBetween(LocalDate from, LocalDate to);

    @Query("select l from WarLeague l join fetch l.playerWarStats ps where l.startDate between :fromDate and :toDate order by l.startDate asc")
    List<WarLeague> findAllBetweenStartDateEagerFetchPlayerStats(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    Optional<WarLeague> findByStartDate(LocalDate date);

    @Query("select l from WarLeague l join fetch l.playerWarStats ps join fetch ps.player p where l.startDate= :date ")
    Optional<WarLeague> findByStartDateEager(LocalDate date);


}
