package org.lytsiware.clash.domain.player;

import java.util.List;
import java.util.Map;


public interface PlayerRepository {
    void saveOrUpdate(Player player);

    void saveOrUpdate(List<Player> players);

    Player findByTag(String tag);

    Map<String, Player> loadAll();


}
