package org.lytsiware.clash.service;

import org.lytsiware.clash.domain.player.GlobalPlayerStat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.List;


@Service
public class GlobalStatsServiceImpl implements GlobalStatsService {

    @Autowired
    EntityManager entityManager;

    @Override
    public List<GlobalPlayerStat> globalPlayerStats() {
        return entityManager.createQuery("select g from GlobalPlayerStat g", GlobalPlayerStat.class).getResultList();
    }
}
