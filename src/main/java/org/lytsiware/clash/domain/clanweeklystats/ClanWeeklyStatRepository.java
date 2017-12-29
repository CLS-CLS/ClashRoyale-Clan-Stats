package org.lytsiware.clash.domain.clanweeklystats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClanWeeklyStatRepository extends JpaRepository<ClanWeeklyStats, Integer> {

    List<ClanWeeklyStats> findByWeekBetween(int from, int to);

}
