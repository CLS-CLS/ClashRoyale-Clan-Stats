package org.lytsiware.clash.domain.player;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class PlayerRepositoryImpl implements PlayerRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void saveOrUpdate(Player player) {
        em.persist(player);
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRED)
    public void saveOrUpdate(List<Player> players) {
        players.stream().forEach(em::merge);
    }

    @Override
    public Player findByTag(String tag) {
        return em.find(Player.class, tag);
    }

    @Override
    public Map<String, Player> loadAll() {
        TypedQuery query =  em.createQuery("select p from Player p", Player.class);
        List<Player> result = query.getResultList();
        return result.stream().collect(Collectors.toMap(Player::getTag,  s -> s));

    }

}
