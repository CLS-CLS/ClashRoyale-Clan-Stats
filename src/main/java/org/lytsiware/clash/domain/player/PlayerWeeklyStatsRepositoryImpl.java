package org.lytsiware.clash.domain.player;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaQuery;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Repository
@Transactional
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
        TypedQuery<PlayerWeeklyStats> query = em.createQuery("select s from PlayerWeeklyStats s join fetch s.player p where s.week = :week", PlayerWeeklyStats.class);
        Query nquery = em.createNamedQuery("findByWeek");
        nquery.setParameter("week", week);
        List<PlayerWeeklyStats> list = nquery.getResultList();
        return list;
    }


    @Override
    public void saveOrUpdateAll(List<PlayerWeeklyStats> playerWeeklyStats) {

    }
}
