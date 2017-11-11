package org.lytsiware;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

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
import org.lytsiware.clash.controller.ClanStatsRestController;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStatsRepository;
import org.lytsiware.clash.dto.PlayerStatsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
@TestPropertySource(locations="classpath:test.properties")
public class ClanStatsControllerTest {
	
	@PersistenceContext
    EntityManager em;
	
	
	@Autowired
	PlayerWeeklyStatsRepository pwsRepo;
	
	@Autowired
	ClanStatsRestController clanStatRestController;

	
	@Before
	public void initDb(){
		EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) em.getEntityManagerFactory();
		DataSource datasource = info.getDataSource();
		Flyway flyway = new Flyway();
		flyway.setDataSource(datasource);
		flyway.clean();
		flyway.migrate();
	}
	
	
	@Test
	public void getPlaylerStatisticsTest() throws SQLException{
		EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) em.getEntityManagerFactory();
		DataSource datasource = info.getDataSource();
		Statement statement = datasource.getConnection().createStatement();
		statement.execute(
				"INSERT INTO PLAYER_WEEKLY_STATS(ID, CARD_DONATION, CHEST_CONTRIBUTION, WEEK, PLAYERFK) VALUES " +
						" (-10 ,481, 184, 33, '2JGGY989R'),"	+
						" (-20 , 482, 185, 34, '2JGGY989R'), " + 
						" (-30, 483, 186, 36, '2JGGY989R'), " +
						" (-40, 484, 187, 32, '2JGGY989R'); "
		);
		
		 List<PlayerWeeklyStats> resultsFromRepo = pwsRepo.findByWeeksAndTag("2JGGY989R", new Week(33), new Week(36));
		 Assert.assertEquals(4, resultsFromRepo.size());
		 
		 PlayerStatsDto resultsFromController = clanStatRestController.retrievePlayerStats("2JGGY989R");
		 
		 Assert.assertEquals("Hellspawn", resultsFromController.getName());
		 Assert.assertEquals(5, resultsFromController.getStatsDto().size());
		 Assert.assertEquals(36, resultsFromController.getStatsDto().get(0).getWeek());
		 Assert.assertEquals(32, resultsFromController.getStatsDto().get(4).getWeek());
		 Assert.assertEquals(481, (int)resultsFromController.getStatsDto().get(3).getCardDonation());
		 Assert.assertEquals(184, (int)resultsFromController.getStatsDto().get(3).getChestContribution());
		 
		
	}
	
	
	

}
