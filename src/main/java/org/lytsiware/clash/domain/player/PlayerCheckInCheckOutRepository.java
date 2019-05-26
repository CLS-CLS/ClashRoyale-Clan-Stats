package org.lytsiware.clash.domain.player;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PlayerCheckInCheckOutRepository extends JpaRepository<PlayerInOut, Long> {

    Optional<PlayerInOut> findByTag(String tag);

    @Query(value = "select p from PlayerInOut p where p.checkIn <= :date and (p.checkOut is null or p.checkOut > :date)")
    List<PlayerInOut> findPlayersInClanAtDate(@Param("date") LocalDateTime date);
}
