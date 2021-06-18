package org.lytsiware.clash.war2.repository;

import org.hibernate.graph.GraphSemantic;
import org.lytsiware.clash.war2.domain.RiverRace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class CustomRiverRaceRepositoryImpl implements CustomRiverRaceRepository {

    @Autowired
    EntityManager em;

    @Override
    public List<RiverRace> getRiverRaces(Pageable pageable) {
        List<Long> result = em.createQuery("select riverRace.id from RiverRace riverRace order by superCellCreatedDate desc", Long.class)
                .setMaxResults(pageable.getPageSize())
                .setFirstResult((int) pageable.getOffset())
                .getResultList();

        return em.createQuery("select distinct riverRace from RiverRace riverRace where riverRace.id in :ids order by superCellCreatedDate desc", RiverRace.class)
                .setParameter("ids", result)
                .setHint(GraphSemantic.LOAD.getJpaHintName(), em.getEntityGraph("RiverRace.eager"))
                .getResultList();
    }
}
