package org.lytsiware.clash.domain.war.league;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WarLeagueRepository extends Repository<WarLeague, Long>, WarLeagueRepositoryCustom {

    Optional<WarLeague> findOneByStartDate(LocalDate startDate);

    List<WarLeague> findByStartDateBetween(LocalDate from, LocalDate to);

    @Query("select l from WarLeague l join fetch l.playerWarStats ps where l.startDate between :fromDate and :toDate")
    List<WarLeague> findAllBetweenStartDateEagerFetchPlayerStats(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);


}
