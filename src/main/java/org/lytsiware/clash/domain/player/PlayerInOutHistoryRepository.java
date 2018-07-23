package org.lytsiware.clash.domain.player;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerInOutHistoryRepository extends JpaRepository<PlayerInOutHistory, Long> {

    List<PlayerInOutHistory> findByTagOrderByCheckInDesc(String tag);


}
