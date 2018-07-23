package org.lytsiware.clash.domain.player;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerInOutRepository extends JpaRepository<PlayerInOut, Long> {

    Optional<PlayerInOut> findByTag(String tag);

}
