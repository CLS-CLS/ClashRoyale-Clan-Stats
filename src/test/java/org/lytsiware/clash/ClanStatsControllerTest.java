package org.lytsiware.clash;

import org.junit.Assert;
import org.junit.Test;
import org.lytsiware.clash.controller.ClanStatsRestController;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStatsRepository;
import org.lytsiware.clash.dto.PlayerStatsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;


public class ClanStatsControllerTest extends AbstractSpringBootTest {




    @Autowired
    PlayerWeeklyStatsRepository pwsRepo;

    @Autowired
    ClanStatsRestController clanStatRestController;





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
