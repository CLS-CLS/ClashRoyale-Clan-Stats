package org.lytsiware.clash.domain.player;

import java.util.Collection;
import java.util.Map;


public interface PlayerRepository {
    void persist(Player player);

    void saveOrUpdate(Collection<Player> players);

    Player findByTag(String tag);

    Map<String, Player> loadAll();


}
