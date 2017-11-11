package org.lytsiware;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lytsiware.clash.Application;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStatsRepository;
import org.lytsiware.clash.service.ClanStatsService;
import org.lytsiware.clash.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
@TestPropertySource(locations = "classpath:test.properties")
@ActiveProfiles("statsRoyale")
@Transactional
public class ClanStatsServiceTest {

	@PersistenceContext
	EntityManager em;
	
	@Autowired
	ClanStatsService clanStatsService;

	@Autowired
	PlayerWeeklyStatsRepository pwsRepo;

	@Before
	public void initDb() {
		EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) em.getEntityManagerFactory();
		DataSource datasource = info.getDataSource();
		Flyway flyway = new Flyway();
		flyway.setDataSource(datasource);
		flyway.clean();
		flyway.migrate();
		em.createNativeQuery("DELETE FROM PLAYER_WEEKLY_STATS").executeUpdate();
		em.createNativeQuery("DELETE FROM PLAYER").executeUpdate();

	}

	@Test
	public void calculateAvgsWithNull() {
		Player player = new Player("1", "Chris", "Elder");
		PlayerWeeklyStats pws1 = new PlayerWeeklyStats(player, 1, 10, 10, 10, 10);
		PlayerWeeklyStats pws2 = new PlayerWeeklyStats(player, 2, null, null, 10, 10);
		PlayerWeeklyStats pws3 = new PlayerWeeklyStats(player, 3, null, 50, 20, 20);
		PlayerWeeklyStats pws4 = new PlayerWeeklyStats(player, 4, 60, null, 0, 0);
		pwsRepo.save(Arrays.asList(pws1, pws2, pws3, pws4));
		List<PlayerWeeklyStats> results = clanStatsService.calculateAvgs(new Week(4));
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(35, results.get(0).getAvgChestContribution(), 0.000001);
		Assert.assertEquals(30, results.get(0).getAvgCardDonation(), 0.000001);
	}

	@Test
	public void updateDonations() {
		Player player = new Player("tag1", "Chris1", "Elder");
		PlayerWeeklyStats db1 = new PlayerWeeklyStats(player, 1, 0, null, 10, 10);
		Player player2 = new Player("tag2", "Chris2", "Elder");
		PlayerWeeklyStats db2 = new PlayerWeeklyStats(player2, 1, 0, 10, 10, 10);
		Player player3 = new Player("tag3", "Chris3", "Elder");
		PlayerWeeklyStats db3 = new PlayerWeeklyStats(player3, 1, 0, 20, 10, 10);

		PlayerWeeklyStats s1 = new PlayerWeeklyStats(player, 1, 0, 10, 10, 10);
		PlayerWeeklyStats s2 = new PlayerWeeklyStats(player2, 1, 0, 20, 10, 10);
		PlayerWeeklyStats s3 = new PlayerWeeklyStats(player3, 1, 0, 5, 10, 10);
		Player anotherPlayer = new Player("tag4", "Chris4", "Elder");
		PlayerWeeklyStats s4 = new PlayerWeeklyStats(anotherPlayer, 1, 0, 30, 10, 10);
		pwsRepo.save(Arrays.asList(db1, db2, db3));

		clanStatsService.updateOrInsertNewDonations(Arrays.asList(s1, s2, s3, s4), new Week(1), true);
		
		
		Map<String, Integer> results = pwsRepo.findByWeek(new Week(1)).stream()
				.collect(Collectors.toMap(s -> s.getPlayer().getTag(), PlayerWeeklyStats::getCardDonation));

		Assert.assertEquals(10, (int) results.get("tag1"));
		Assert.assertEquals(20, (int) results.get("tag2"));
		Assert.assertEquals(20, (int) results.get("tag3"));
		Assert.assertEquals(30, (int) results.get("tag4"));
	}
	
	
	@Test
	public void updateContributions(){
		Player player = new Player("tag1", "Chris1", "Elder");
		PlayerWeeklyStats db1 = new PlayerWeeklyStats(player, 1, null, 0, 0, 0);
		Player player2 = new Player("tag2", "Chris2", "Elder");
		PlayerWeeklyStats db2 = new PlayerWeeklyStats(player2, 1, 10, 0, 0, 0);
		
		PlayerWeeklyStats s1 = new PlayerWeeklyStats(player, 1, 100, 0, 0, 0);
		PlayerWeeklyStats s2 = new PlayerWeeklyStats(player2, 1, 200, 0, 0, 0);
		PlayerWeeklyStats s3 = new PlayerWeeklyStats(new Player("3", "nope", "whatever"), 1, 200, 0, 0, 0);
		
		pwsRepo.save(Arrays.asList(db1, db2));
		
		clanStatsService.updateChestContributions(Arrays.asList(s1, s2, s3), new Week(1));
		
		Map<String, Integer> results = pwsRepo.findByWeek(new Week(1)).stream()
				.collect(Utils.collectToMap(s -> s.getPlayer().getTag(), PlayerWeeklyStats::getChestContribution));
		
			
		Assert.assertEquals(2, results.size());
		Assert.assertEquals(100, (int)results.get("tag1"));
		Assert.assertEquals(200, (int)results.get("tag2"));
		
	}

}
