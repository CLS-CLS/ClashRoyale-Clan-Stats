package org.lytsiware.clash.domain.playerweeklystats;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.lytsiware.clash.utils.Utils.collectToMap;

@Repository
@Transactional(value = TxType.REQUIRED)
public class PlayerWeeklyStatsRepositoryImpl implements PlayerWeeklyStatsRepository {

    Logger logger = LoggerFactory.getLogger(PlayerWeeklyStatsRepositoryImpl.class);

    @PersistenceContext
    EntityManager em;

    @Override
    public void save(PlayerWeeklyStats playerWeeklyStats) {
        logger.info("save playerWeeklyStats with tag {}", playerWeeklyStats.getPlayer().getTag());
        em.persist(playerWeeklyStats);
    }

    @Override
    public Map<Player, List<PlayerWeeklyStats>> findByWeek(Week startWeek, Week endWeek) {
        logger.info("findByWeek {} - {}", startWeek, endWeek);

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
    @Cacheable(cacheNames = "weeklyStats", key = "#week")
    public List<PlayerWeeklyStats> findByWeek(Week week) {
        logger.info("findByWeek {}", week);

        Query nquery = em.createNamedQuery("findByWeek");
        nquery.setParameter("week", week.getWeek());
        @SuppressWarnings("unchecked")
        List<PlayerWeeklyStats> list = nquery.getResultList();
        return list;
    }

    @Override
    public void save(List<PlayerWeeklyStats> playerWeeklyStats) {
        //using merge because the player can already exist
        playerWeeklyStats.stream().forEach(em::merge);
    }

    @Override
    @Cacheable("playerStats")
    public List<PlayerWeeklyStats> findByWeeksAndTag(String tag, Week startWeek, Week endWeek) {
        Query nQuery = em.createNamedQuery("findByWeekAndTag").setParameter("tag", tag)
                .setParameter("startWeek", startWeek.getWeek()).setParameter("endWeek", endWeek.getWeek());
        @SuppressWarnings("unchecked")
        List<PlayerWeeklyStats> results = nQuery.getResultList();
        return results;
    }

    @Override
    @CachePut(cacheNames = "weeklyStats", key = "#week")
    public List<PlayerWeeklyStats> updateDonations(List<PlayerWeeklyStats> donations, Week week, final boolean onlyUpdateBiggerDonations) {
        logger.info("updateDonations for week = " + week.toStringWithDates());

        List<PlayerWeeklyStats> dbStats = findByWeek(week);
        Map<String, Integer> donationsMap = donations.stream().collect(collectToMap(s -> s.getPlayer().getTag(), PlayerWeeklyStats::getCardDonation));

        Predicate<PlayerWeeklyStats> siteStatBiggerThanDbStat = isStatBiggerThanDbStatPredicate(donationsMap, PlayerWeeklyStats::getCardDonation);

        dbStats.stream()
                .filter(dbStat -> donationsMap.containsKey(dbStat.getPlayer().getTag()))
                .filter(dbStat -> !onlyUpdateBiggerDonations || siteStatBiggerThanDbStat.test(dbStat))
                .forEach(dbStat ->
                {
                    Integer before = dbStat.getCardDonation(); //just for logging
                    dbStat.setCardDonation(donationsMap.get(dbStat.getPlayer().getTag()));
                    logger.info("updated : before , after - {}, {} ", before, dbStat.getCardDonation());
                    em.merge(dbStat);

                });
        return dbStats;
    }


    @Override
    @CachePut(cacheNames = "weeklyStats", key = "#week")
    public List<PlayerWeeklyStats> updateChestContribution(List<PlayerWeeklyStats> chestContributions, Week week, boolean updateBiggerOnly) {
        logger.info("updateChestContribution for week = " + week.toStringWithDates());

        List<PlayerWeeklyStats> dbStats = findByWeek(week);
        Map<String, Integer> chestContributionsMap = chestContributions.stream().collect(collectToMap(s -> s.getPlayer().getTag(), PlayerWeeklyStats::getChestContribution));

        Predicate<PlayerWeeklyStats> siteStatBiggerThanDbStat = isStatBiggerThanDbStatPredicate(chestContributionsMap, PlayerWeeklyStats::getChestContribution);

        dbStats.stream()
                .filter(dbStat -> chestContributionsMap.containsKey(dbStat.getPlayer().getTag()))
                .filter(dbStat -> !updateBiggerOnly || siteStatBiggerThanDbStat.test(dbStat))
                .forEach(dbStat ->
                {
                    Integer before = dbStat.getChestContribution(); //just for logging
                    dbStat.setChestContribution(chestContributionsMap.get(dbStat.getPlayer().getTag()));
                    logger.info("updated : before , after - {}, {} ", before, dbStat.getChestContribution());
                    em.merge(dbStat);

                });
        return dbStats;

    }

    @Override
    @CachePut(cacheNames = {"weeklyStats"}, key = "#week")
    public List<PlayerWeeklyStats> saveOrUpdate(Collection<PlayerWeeklyStats> playerWeeklyStats, Week week) {
        Map<String, PlayerWeeklyStats> databaseStats = findByWeek(week).stream()
                .collect(Collectors.toMap(p -> p.getPlayer().getTag(), Function.identity()));

        playerWeeklyStats.stream().filter(s -> s.getId() == null && databaseStats.containsKey(s.getPlayer().getTag()))
                .forEach(s -> s.setId(s.getId()));

        List<PlayerWeeklyStats> mergedResult = playerWeeklyStats.stream().map(em::merge).collect(Collectors.toList());

        return mergedResult;
    }

    private Predicate<PlayerWeeklyStats> isStatBiggerThanDbStatPredicate(Map<String, Integer> tagStatMap, Function<PlayerWeeklyStats, Integer> getField) {
        return dbStats -> {
            if (tagStatMap.get(dbStats.getPlayer().getTag()) == null) return false;
            if (getField.apply(dbStats) == null) return true;
            if (tagStatMap.get(dbStats.getPlayer().getTag()) > getField.apply(dbStats)) return true;
            return false;
        };
    }
}
