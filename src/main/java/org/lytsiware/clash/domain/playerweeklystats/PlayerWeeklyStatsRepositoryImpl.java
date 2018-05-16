package org.lytsiware.clash.domain.playerweeklystats;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PlayerWeeklyStatsRepositoryImpl implements PlayerWeeklyStatsRepository {

    private Logger logger = LoggerFactory.getLogger(PlayerWeeklyStatsRepositoryImpl.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional(value = TxType.MANDATORY)
    public void save(PlayerWeeklyStats playerWeeklyStats) {
        logger.info("save playerWeeklyStats with tag {}", playerWeeklyStats.getPlayer().getTag());
        em.persist(playerWeeklyStats);
    }

    @Override
    @Transactional(value = TxType.MANDATORY)
    public void saveOrUpdateAll(List<PlayerWeeklyStats> playerWeeklyStats) {
        //using merge because the player can already exist
        List<PlayerWeeklyStats> saved = new ArrayList<>();
        for (PlayerWeeklyStats pws : playerWeeklyStats) {
            saved.add(em.merge(pws));
        }
    }

    @Override
    public Map<Player, List<PlayerWeeklyStats>> findBetweenWeeks(Week startWeek, Week endWeek) {
        logger.info("findBetweenWeeks {} - {}", startWeek, endWeek);

        Query nquery = em.createNamedQuery("findBetweenWeeks");
        nquery.setParameter("startWeek", startWeek.getWeek()).setParameter("endWeek", endWeek.getWeek());
        @SuppressWarnings("unchecked")
        List<PlayerWeeklyStats> list = nquery.getResultList();
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
    public List<PlayerWeeklyStats> findByWeek(Week week) {
        logger.info("findByWeek {}", week);

        Query nquery = em.createNamedQuery("findByWeek");
        nquery.setParameter("week", week.getWeek());
        @SuppressWarnings("unchecked")
        List<PlayerWeeklyStats> list = nquery.getResultList();
        return list;
    }


    @Override
    public List<PlayerWeeklyStats> findByWeeksAndTag(String tag, Week startWeek, Week endWeek) {
        Query nQuery = em.createNamedQuery("findByWeekAndTag").setParameter("tag", tag)
                .setParameter("startWeek", startWeek.getWeek()).setParameter("endWeek", endWeek.getWeek());
        @SuppressWarnings("unchecked")
        List<PlayerWeeklyStats> results = nQuery.getResultList();
        return results;
    }

}
