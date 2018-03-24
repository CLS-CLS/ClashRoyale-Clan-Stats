package org.lytsiware.clash.domain.player;


import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Player {

    @Id
    String tag;

    String name;

    String role;

    public Player() {
    }

    public Player(String tag, String name, String role) {
        this.tag = tag;
        this.name = name;
        this.role = role;
    }

    public String getTag() {
        return tag;
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        return tag.equals(player.tag);
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

    @Override
    public String toString() {
        return "Player{" +
                "tag='" + tag + '\'' +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
