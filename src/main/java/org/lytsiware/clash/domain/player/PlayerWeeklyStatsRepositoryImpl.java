package org.lytsiware.clash.domain.player;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Repository
@Transactional(value=TxType.REQUIRED)
public class PlayerWeeklyStatsRepositoryImpl implements PlayerWeeklyStatsRepository {

    @PersistenceContext
    EntityManager em;


    @Override
    public PlayerWeeklyStats saveOrUpdate(PlayerWeeklyStats playerWeeklyStats) {
        return em.merge(playerWeeklyStats);

    }

    @Override
    public Map<Player, List<PlayerWeeklyStats>> findByWeek(int startWeek, int endWeek) {
        TypedQuery<PlayerWeeklyStats> query = em.createQuery("select s from PlayerWeeklyStats s where week between :startWeek AND :endWeek", PlayerWeeklyStats.class);
        query.setParameter("startWeek", startWeek).setParameter("endWeek", endWeek);
        List<PlayerWeeklyStats> list = query.getResultList();
        Map<Player, List<PlayerWeeklyStats>> result = new HashMap<>();
        for (PlayerWeeklyStats stat : list) {
            if (result.containsKey(stat.getPlayer())) {
                result.get(stat.getPlayer()).add(stat);
            } else {
                List<PlayerWeeklyStats> statList = new ArrayList<>();
                statList.add(stat);
                result.put(stat.getPlayer(), statList);
            }
        }
        return result;
    }

    @Override
    @Cacheable("playerStats")
    public List<PlayerWeeklyStats> findByWeek(int week) {
        Query nquery = em.createNamedQuery("findByWeek");
        nquery.setParameter("week", week);
        List<PlayerWeeklyStats> list = nquery.getResultList();
        return list;
    }


    @Override
    public List<PlayerWeeklyStats> saveOrUpdateAll(List<PlayerWeeklyStats> playerWeeklyStats) {
    	List<PlayerWeeklyStats> mergedStats = new ArrayList<>();
    	playerWeeklyStats.stream().forEach(s -> mergedStats.add(em.merge(s)));
    	return mergedStats;
    	
    }
}
