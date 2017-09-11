package org.lytsiware.clash.domain.player;


import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;


@Entity
@Table(uniqueConstraints={@UniqueConstraint(columnNames = {"playerFK", "week"})})
@NamedQueries(@NamedQuery(name="findByWeek", query="select s from PlayerWeeklyStats s join fetch s.player p where s.week = :week"))
//@IdClass(PlayerWeeklyStatsPK.class)
public class PlayerWeeklyStats {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name="playerFK")
    private Player player;

    private int week;

    private int chestContribution;

    private int cardDonation;

    public Player getPlayer() {
        return player;
    }

    public int getWeek() {
        return week;
    }

    public int getChestContribution() {
        return chestContribution;
    }

    public int getCardDonation() {
        return cardDonation;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public void setChestContribution(int chestContribution) {
        this.chestContribution = chestContribution;
    }

    public void setCardDonation(int cardDonation) {
        this.cardDonation = cardDonation;
    }
}
