package org.lytsiware.clash.domain.playerweeklystats;

import java.io.Serializable;

import org.lytsiware.clash.domain.player.Player;


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
