package org.lytsiware.clash.domain.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class PlayerRepositoryImpl implements PlayerRepository {

    Logger logger = LoggerFactory.getLogger(PlayerRepositoryImpl.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public Player persist(Player player) {
        em.persist(player);
        return player;
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRED)
    public void saveOrUpdate(Collection<Player> players) {
        logger.info("persist players");
        players.stream().forEach(em::merge);
    }

    @Override
    public Player findByTag(String tag) {
        logger.info("findByTag {}", tag);
        return em.find(Player.class, tag);
    }

    @Override
    public Map<String, Player> loadAll() {
        logger.info("loadAll");
        TypedQuery<Player> query = em.createQuery("select p from Player p", Player.class);
        List<Player> result = query.getResultList();
        return result.stream().collect(Collectors.toMap(Player::getTag, Function.identity()));

    }

}
