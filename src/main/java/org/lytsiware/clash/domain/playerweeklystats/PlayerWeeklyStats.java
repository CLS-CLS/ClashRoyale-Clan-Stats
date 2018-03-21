package org.lytsiware.clash.domain.playerweeklystats;

import org.lytsiware.clash.domain.player.Player;

import javax.persistence.*;

@Entity
@SequenceGenerator(name = "PWS_SEQUENCE", sequenceName = "PWS_SEQUENCE", initialValue = 100, allocationSize = 10)
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"playerFK", "week"})})
@NamedQueries({
        @NamedQuery(name = "findByWeek", query = "select s from PlayerWeeklyStats s join fetch s.player where s.week = :week"),

        @NamedQuery(name = "findBetweenWeeks", query = "select s from PlayerWeeklyStats s join fetch s.player where s.week between :startWeek AND :endWeek order by s.week desc"),

        @NamedQuery(name = "findByWeekAndTag", query = "select s from PlayerWeeklyStats s join fetch s.player where s.week between :startWeek and :endWeek and s.player.tag = :tag order by s.week desc")

})
// @IdClass(PlayerWeeklyStatsPK.class)
public class PlayerWeeklyStats {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PWS_SEQUENCE")
    private Long id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "playerFK")
    private Player player;

    private Integer week;
    private Integer chestContribution;
    private Integer cardDonation;
    private Integer cardsReceived;
    private double avgChestContribution;
    private double avgCardDonation;

    protected PlayerWeeklyStats(Player player, Integer week, Integer chestContribution,
                                Integer cardDonation, Integer cardsReceived, double avgChestContribution, double avgCardDonation) {
        super();
        this.player = player;
        this.week = week;
        this.chestContribution = chestContribution;
        this.cardDonation = cardDonation;
        this.cardsReceived = cardsReceived;
        this.avgChestContribution = avgChestContribution;
        this.avgCardDonation = avgCardDonation;
    }


    public PlayerWeeklyStats(Player player, int week, Integer chestContribution, Integer cardDonation, double avgChestContribution, double avgCardDonation) {
        super();
        this.player = player;
        this.week = week;
        this.chestContribution = chestContribution;
        this.cardDonation = cardDonation;
        this.avgChestContribution = avgChestContribution;
        this.avgCardDonation = avgCardDonation;
    }

    public PlayerWeeklyStats(Player player, Integer chestContribution, Integer cardDonation, double avgChestContribution, double avgCardDonation) {
        super();
        this.player = player;
        this.chestContribution = chestContribution;
        this.cardDonation = cardDonation;
        this.avgChestContribution = avgChestContribution;
        this.avgCardDonation = avgCardDonation;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getAvgChestContribution() {
        return avgChestContribution;
    }

    public void setAvgChestContribution(double avgChestContribution) {
        this.avgChestContribution = avgChestContribution;
    }

    public double getAvgCardDonation() {
        return avgCardDonation;
    }

    public void setAvgCardDonation(double avgCardDonation) {
        this.avgCardDonation = avgCardDonation;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Integer getWeek() {
        return week;
    }

    public void setWeek(Integer week) {
        this.week = week;
    }

    public Integer getChestContribution() {
        return chestContribution;
    }

    public void setChestContribution(Integer chestContrubition) {
        this.chestContribution = chestContrubition;
    }

    public Integer getCardDonation() {
        return cardDonation;
    }

    public void setCardDonation(Integer donation) {
        this.cardDonation = donation;
    }

    public Integer getCardsReceived() {
        return cardsReceived;
    }

    public void setCardsReceived(Integer cardsReceived) {
        this.cardsReceived = cardsReceived;
    }

    public static class Builder {
        private Player player;
        private Integer week;
        private Integer chestContribution;
        private Integer cardDonation;
        private Integer cardsReceived;
        private double avgChestContribution;
        private double avgCardDonation;

        public Builder withPlayer(Player player) {
            this.player = player;
            return this;
        }

        public Builder withWeek(Integer week) {
            this.week = week;
            return this;
        }

        public Builder withChestContribution(Integer chestContribution) {
            this.chestContribution = chestContribution;
            return this;
        }

        public Builder withCardDonation(Integer cardDonation) {
            this.cardDonation = cardDonation;
            return this;
        }

        public Builder withCardsReceived(Integer cardsReceived) {
            this.cardsReceived = cardsReceived;
            return this;
        }

        public Builder withAvgChestContribution(double avgChestContribution) {
            this.avgChestContribution = avgChestContribution;
            return this;
        }

        public Builder withAvgCardDonation(double avgCardDonation) {
            this.avgCardDonation = avgCardDonation;
            return this;
        }

        public PlayerWeeklyStats build() {
            return new PlayerWeeklyStats(player, week, chestContribution, cardDonation, cardsReceived, avgChestContribution, avgCardDonation);
        }
    }


}
