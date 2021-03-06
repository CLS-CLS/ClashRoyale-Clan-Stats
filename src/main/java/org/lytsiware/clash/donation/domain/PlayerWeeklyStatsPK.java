package org.lytsiware.clash.donation.domain;

import org.lytsiware.clash.core.domain.player.Player;

import java.io.Serializable;

/**
 * N+1 bug of hibernate. Used classic id instead
 *
 * @author Christos
 */
@Deprecated
public class PlayerWeeklyStatsPK implements Serializable {

  	private static final long serialVersionUID = 1L;
	
  	private Player player;
    private int week;

    public PlayerWeeklyStatsPK(Player player, int week) {
        this.player = player;
        this.week = week;
    }

    public PlayerWeeklyStatsPK() {
    }

    public Player getPlayer() {
        return player;
    }

    public int getWeek() {
        return week;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerWeeklyStatsPK that = (PlayerWeeklyStatsPK) o;

        if (week != that.week) return false;
        return player.equals(that.player);
    }

    @Override
    public int hashCode() {
        int result = player.hashCode();
        result = 31 * result + week;
        return result;
    }
}
