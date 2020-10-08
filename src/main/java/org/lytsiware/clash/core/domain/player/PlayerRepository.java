package org.lytsiware.clash.core.domain.player;

import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;


@Repository
public interface PlayerRepository {
    Player persist(Player player);

    void saveOrUpdate(Collection<Player> players);

    Player findByTag(String tag);

    Map<String, Player> loadAll();

    List<Player> findInClan();
}
