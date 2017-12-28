package org.lytsiware.clash.domain.clanweeklystats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClanWeeklyStatRepository extends JpaRepository<ClanWeeklyStats, Integer> {

}
