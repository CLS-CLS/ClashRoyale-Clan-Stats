package org.lytsiware.clash.domain.player;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class Player {

    @Id
    String tag;

    String name;

    double avgChestContribution;

    double avgCardDonation;

    public Player() {
    }

    public Player(String tag, String name, double avgChestContribution, double avgCardDonation) {
        this.tag = tag;
        this.name = name;
        this.avgChestContribution = avgChestContribution;
        this.avgCardDonation = avgCardDonation;
    }

    public Player(String tag, String name) {
        this.tag = tag;
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public String getName() {
        return name;
    }

    public double getAvgChestContribution() {
        return avgChestContribution;
    }

    public double getAvgCardDonation() {
        return avgCardDonation;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvgChestContribution(double avgChestContribution) {
        this.avgChestContribution = avgChestContribution;
    }

    public void setAvgCardDonation(double avgCardDonation) {
        this.avgCardDonation = avgCardDonation;
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
}
