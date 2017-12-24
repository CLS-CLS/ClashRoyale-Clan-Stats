package org.lytsiware;

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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
@TestPropertySource(locations = "classpath:test.properties")
public class ClanStatsControllerTest {

    @PersistenceContext
    EntityManager em;


    @Autowired
    PlayerWeeklyStatsRepository pwsRepo;

    @Autowired
    ClanStatsRestController clanStatRestController;


    @Before
    public void initDb() {
        EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) em.getEntityManagerFactory();
        DataSource datasource = info.getDataSource();
        Flyway flyway = new Flyway();
        flyway.setDataSource(datasource);
        flyway.clean();
        flyway.migrate();
    }


    @Test
    public void getPlaylerStatisticsTest() throws SQLException {
        EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) em.getEntityManagerFactory();
        DataSource datasource = info.getDataSource();
        Statement statement = datasource.getConnection().createStatement();
        statement.execute("INSERT INTO PLAYER (TAG, NAME, ROLE) VALUES ('2JGGY989R', 'Hellspawn', 'Co-leader')");
        String query = String.format("INSERT INTO PLAYER_WEEKLY_STATS(ID, CARD_DONATION, CHEST_CONTRIBUTION, WEEK, PLAYERFK) VALUES " +
                " (-10 ,481, 184, %s, '2JGGY989R')," +
                " (-20 , 482, 185, %s, '2JGGY989R'), " +
                " (-30, 483, 186, %s, '2JGGY989R'), " +
                " (-40, 484, 187, %s, '2JGGY989R');", Week.now(), Week.now().minusWeeks(1), Week.now().minusWeeks(2), Week.now().minusWeeks(4));

        statement.execute(query);

        List<PlayerWeeklyStats> resultsFromRepo = pwsRepo.findByWeeksAndTag("2JGGY989R", Week.now().minusWeeks(3), Week.now());
        Assert.assertEquals(3, resultsFromRepo.size());

        PlayerStatsDto resultsFromController = clanStatRestController.retrievePlayerStats("2JGGY989R");

        Assert.assertEquals("Hellspawn", resultsFromController.getName());
        Assert.assertEquals(3, resultsFromController.getStatsDto().size());
        Assert.assertEquals(Week.now().previous().getWeek(), resultsFromController.getStatsDto().get(0).getWeek());
        Assert.assertEquals(Week.now().minusWeeks(4).getWeek(), resultsFromController.getStatsDto().get(2).getWeek());
        Assert.assertEquals(483, (int) resultsFromController.getStatsDto().get(1).getCardDonation());
        Assert.assertEquals(186, (int) resultsFromController.getStatsDto().get(1).getChestContribution());


    }


}
