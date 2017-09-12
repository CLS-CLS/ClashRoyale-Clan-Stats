package org.lytsiware.clash.domain.player;


import javax.persistence.*;


@Entity
@Table(uniqueConstraints={@UniqueConstraint(columnNames = {"playerFK", "week"})})
@NamedQueries({@NamedQuery(name="findByWeek", query="select s from PlayerWeeklyStats s join fetch s.player where s.week = :week"),
              @NamedQuery(name="findBetweenWeeks", query = "select s from PlayerWeeklyStats s join fetch s.player where week between :startWeek AND :endWeek")})
//@IdClass(PlayerWeeklyStatsPK.class)
public class PlayerWeeklyStats {
	
	public PlayerWeeklyStats() {
	
	}

    public PlayerWeeklyStats(Player player, int week, int chestContribution, int cardDonation) {
		super();
		this.player = player;
		this.week = week;
		this.chestContribution = chestContribution;
		this.cardDonation = cardDonation;
	}

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
