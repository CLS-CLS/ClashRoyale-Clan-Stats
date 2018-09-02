package org.lytsiware.clash.domain.war.league;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.function.BiFunction;

@Repository
@Slf4j
public class WarLeagueRepositoryCustomImpl implements WarLeagueRepositoryCustom {

    @PersistenceContext
    EntityManager em;

    @Override
    public List<WarLeague> findLeaguesAroundDate(LocalDate date, int span) {
        String baseQuery = "select l from WarLeague l where l.startDate %s :date order by l.startDate %s";
        BiFunction<String, String, Query> createQuery = (op, sort) -> em.createQuery(String.format(baseQuery, op, sort), WarLeague.class)
                .setMaxResults(span)
                .setParameter("date", date);
        List<WarLeague> leagues = createQuery.apply(">=", "ASC").getResultList();
        leagues.addAll(createQuery.apply("<", "DESC").getResultList());
        return leagues;
    }

    @Override
    public List<WarLeague> findFirstNthWarLeaguesBeforeDate(LocalDate date, int n) {
        log.info("START findFirstNthWarLeaguesBeforeDate for date {}", date);
        return em.createQuery("select l from WarLeague l where l.startDate <= :date order by l.startDate DESC", WarLeague.class)
                .setMaxResults(n)
                .setParameter("date", date).getResultList();
    }

    @Override
    public List<WarLeague> findFirstNthWarLeaguesAfterDate(LocalDate date, int n) {
        return em.createQuery("select l from WarLeague l where l.startDate >= :date order by l.startDate ASC", WarLeague.class)
                .setMaxResults(n)
                .setParameter("date", date).getResultList();
    }

    @Override
    public void persistAndFlush(WarLeague warLeague) throws EntityExistsException {
        em.persist(warLeague);
    }
}
