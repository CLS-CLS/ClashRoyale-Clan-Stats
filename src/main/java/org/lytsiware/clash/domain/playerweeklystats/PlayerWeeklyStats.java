package org.lytsiware.clash.domain.playerweeklystats;

import javax.persistence.*;

import org.lytsiware.clash.domain.player.Player;

@Entity
@SequenceGenerator(name = "PWS_SEQUENCE", sequenceName = "PWS_SEQUENCE", initialValue = 100, allocationSize = 10)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "playerFK", "week" }) })
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
	@JoinColumn(name = "playerFK")
	private Player player;

	private int week;
	
	
	private Integer chestContribution;
	private Integer cardDonation;
	private double avgChestContribution;
	private double avgCardDonation;

	protected PlayerWeeklyStats() {}
	
	public PlayerWeeklyStats(Player player, int week, Integer chestContribution, Integer cardDonation, double avgChestContribution, double avgCardDonation) {
		super();
		this.player = player;
		this.week = week;
		this.chestContribution = chestContribution;
		this.cardDonation = cardDonation;
		this.avgChestContribution = avgChestContribution;
		this.avgCardDonation = avgCardDonation;
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

	public int getWeek() {
		return week;
	}

	public Integer getChestContribution() {
		return chestContribution;
	}

	public Integer getCardDonation() {
		return cardDonation;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void setWeek(int week) {
		this.week = week;
	}
	
	public void setCardDonation(Integer donation) {
		this.cardDonation = donation;
	}
	
	public void setChestContribution(Integer chestContrubition) {
		this.chestContribution = chestContrubition;
	}
	
}
