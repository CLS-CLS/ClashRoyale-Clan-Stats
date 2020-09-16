package org.lytsiware.clash.core.service.gobalstats;

import lombok.AllArgsConstructor;
import org.lytsiware.clash.core.domain.player.GlobalPlayerStat;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.List;


@Service
@AllArgsConstructor
public class GlobalStatsServiceImpl implements GlobalStatsService {

    EntityManager entityManager;

    @Override
    public List<GlobalPlayerStat> globalPlayerStats() {
        return entityManager.createQuery("select g from GlobalPlayerStat g", GlobalPlayerStat.class).getResultList();
    }
}
