package org.lytsiware.clash.war2.repository;

import org.lytsiware.clash.war2.domain.RiverRaceParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RiverRaceParticipantRepository extends JpaRepository<RiverRaceParticipant, Long> {


    @Query(value = "select p.* from river_race_participant p" +
            " join river_race_clan c on p.river_race_clan_fk = c.id " +
            " join river_race r on r.clan_id = c.id" +
            " where p.tag= :playerTag and c.tag = :clanTag order by r.super_cell_created_date desc nulls first", nativeQuery = true)
    List<RiverRaceParticipant> findByTagOrderByRace(@Param("playerTag") String playerTag, @Param("clanTag") String clanTag);
}
