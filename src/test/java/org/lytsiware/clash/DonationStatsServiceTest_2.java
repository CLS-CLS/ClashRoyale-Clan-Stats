package org.lytsiware.clash;

import org.junit.Before;
import org.junit.Test;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStatsRepository;
import org.lytsiware.clash.service.donations.DonationStatsService;
import org.lytsiware.clash.service.donations.UpdateStatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@Transactional
public class DonationStatsServiceTest_2 extends AbstractSpringBootTest {

    @Autowired
    private PlayerWeeklyStatsRepository playerWeeklyStatsRepository;

    @Autowired
    private DonationStatsService donationStatsService;

    @Autowired
    private UpdateStatService updateStatsService;

    @Before
    public void createData() {
        try {
            EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) em.getEntityManagerFactory();
            DataSource datasource = info.getDataSource();
            Statement statement = datasource.getConnection().createStatement();

            statement.execute("INSERT INTO PLAYER (TAG, NAME, ROLE) VALUES ('0', 'Hellspawn0', 'Co-leader')");
            statement.execute("INSERT INTO PLAYER (TAG, NAME, ROLE) VALUES ('1', 'Hellspawn1', 'Co-leader')");
            statement.execute("INSERT INTO PLAYER (TAG, NAME, ROLE) VALUES ('2', 'Hellspawn2', 'Co-leader')");
            statement.execute("INSERT INTO PLAYER (TAG, NAME, ROLE) VALUES ('3', 'Hellspawn3', 'Co-leader')");

            statement.execute("INSERT INTO PLAYER_WEEKLY_STATS(ID, CHEST_CONTRIBUTION, CARD_DONATION, WEEK, PLAYERFK) VALUES " +
                    " (-10 ,100, 200, 30, '0')," +
                    " (-20 , null, null, 30, '1'), " +
                    " (-30, 120, 220, 30, '2');");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    @Test
    public void updateOrInsertDonationAndContributionsWithBiggerTrue() {
        Player p0 = new Player("0", "1", "irrelevant");
        Player p1 = new Player("1", "1", "irrelevant");
        Player p2 = new Player("2", "1", "irrelevant");
        Player p3 = new Player("3", "1", "irrelevant");

        List<PlayerWeeklyStats> donations = Arrays.asList(
                new PlayerWeeklyStats(p0, 30, 90, 190,0,0),
                new PlayerWeeklyStats(p1, 30, 10, 200,0,0),
                new PlayerWeeklyStats(p2, 30, 123, 230,0,0),
                new PlayerWeeklyStats(p3, 30, 123, 230,0,0)
        );

        updateStatsService.updatePlayerWeeklyStats(donations, Week.fromWeek(30), true);


        List<PlayerWeeklyStats> pwsAll = playerWeeklyStatsRepository.findByWeek(Week.fromWeek(30));

        assertEquals(4, pwsAll.size());

        PlayerWeeklyStats pws = playerWeeklyStatsRepository.findByWeeksAndTag("0", Week.fromWeek(30), Week.fromWeek(30)).get(0);
        assertEquals(200, (int)pws.getCardDonation());
        assertEquals(100, (int)pws.getChestContribution());

        pws = playerWeeklyStatsRepository.findByWeeksAndTag("1", Week.fromWeek(30), Week.fromWeek(30)).get(0);
        assertEquals(200, (int)pws.getCardDonation());
        assertEquals(10, (int)pws.getChestContribution());

        pws = playerWeeklyStatsRepository.findByWeeksAndTag("2", Week.fromWeek(30), Week.fromWeek(30)).get(0);
        assertEquals(230, (int)pws.getCardDonation());
        assertEquals(123, (int)pws.getChestContribution());

        pws = playerWeeklyStatsRepository.findByWeeksAndTag("3", Week.fromWeek(30), Week.fromWeek(30)).get(0);
        assertEquals(230, (int)pws.getCardDonation());
        assertEquals(123, (int)pws.getChestContribution());
    }


    @Test
    public void updateDonationsWithBiggerTrue() {
        Player p0 = new Player("0", "1", "irrelevant");
        Player p1 = new Player("1", "1", "irrelevant");
        Player p2 = new Player("2", "1", "irrelevant");
        Player p3 = new Player("3", "1", "irrelevant");

        List<PlayerWeeklyStats> donations = Arrays.asList(
                new PlayerWeeklyStats(p0, 30, 0, 190,0,0),
                new PlayerWeeklyStats(p1, 30, 0, 200,0,0),
                new PlayerWeeklyStats(p2, 30, 0, 230,0,0),
                new PlayerWeeklyStats(p3, 30, 0, 230,0,0)
        );

        updateStatsService.updatePlayerWeeklyStats(donations, Week.fromWeek(30), true);


        List<PlayerWeeklyStats> pwsAll = playerWeeklyStatsRepository.findByWeek(Week.fromWeek(30));

        assertEquals(4, pwsAll.size());

        PlayerWeeklyStats pws = playerWeeklyStatsRepository.findByWeeksAndTag("0", Week.fromWeek(30), Week.fromWeek(30)).get(0);
        assertEquals(200, (int)pws.getCardDonation());
        assertEquals(100, (int)pws.getChestContribution());

        pws = playerWeeklyStatsRepository.findByWeeksAndTag("1", Week.fromWeek(30), Week.fromWeek(30)).get(0);
        assertEquals(200, (int)pws.getCardDonation());
        assertNull(pws.getChestContribution());

        pws = playerWeeklyStatsRepository.findByWeeksAndTag("2", Week.fromWeek(30), Week.fromWeek(30)).get(0);
        assertEquals(230, (int)pws.getCardDonation());
        assertEquals(120, (int)pws.getChestContribution());
    }


    @Test
    public void updateDonationsWithBiggerFalse() {
        Player p0 = new Player("0", "1", "irrelevant");
        Player p1 = new Player("1", "1", "irrelevant");
        Player p2 = new Player("2", "1", "irrelevant");
        Player p3 = new Player("3", "1", "irrelevant");

        List<PlayerWeeklyStats> donations = Arrays.asList(
                new PlayerWeeklyStats(p0, 30, 0, null,0,0),
                new PlayerWeeklyStats(p1, 30, 0, 10,0,0),
                new PlayerWeeklyStats(p2, 30, 0, 30,0,0),
                new PlayerWeeklyStats(p3, 30, 0, 230,0,0)
        );

        updateStatsService.updatePlayerWeeklyStats(donations, Week.fromWeek(30), false);

        List<PlayerWeeklyStats> pwsAll = playerWeeklyStatsRepository.findByWeek(Week.fromWeek(30));

        assertEquals(4, pwsAll.size());

        PlayerWeeklyStats pws = playerWeeklyStatsRepository.findByWeeksAndTag("0", Week.fromWeek(30), Week.fromWeek(30)).get(0);
        assertNull(pws.getCardDonation());
        assertEquals(100, (int)pws.getChestContribution());

        pws = playerWeeklyStatsRepository.findByWeeksAndTag("1", Week.fromWeek(30), Week.fromWeek(30)).get(0);
        assertEquals(10, (int)pws.getCardDonation());
        assertNull(pws.getChestContribution());

        pws = playerWeeklyStatsRepository.findByWeeksAndTag("2", Week.fromWeek(30), Week.fromWeek(30)).get(0);
        assertEquals(30, (int)pws.getCardDonation());
        assertEquals(120, (int)pws.getChestContribution());
    }


    @Test
    public void updateChestContributionsWithBiggerTrue() {
        Player p0 = new Player("0", "1", "irrelevant");
        Player p1 = new Player("1", "1", "irrelevant");
        Player p2 = new Player("2", "1", "irrelevant");
        Player p3 = new Player("3", "1", "irrelevant");

        List<PlayerWeeklyStats> donations = Arrays.asList(
                new PlayerWeeklyStats(p0, 30, 90, 0,0,0),
                new PlayerWeeklyStats(p1, 30, 100, 0,0,0),
                new PlayerWeeklyStats(p2, 30, 130, 0,0,0),
                new PlayerWeeklyStats(p3, 30, 130, 0,0,0)
        );

        updateStatsService.updateChestContibutionAndRole(donations, Week.fromWeek(30), true);

        List<PlayerWeeklyStats> pwsAll = playerWeeklyStatsRepository.findByWeek(Week.fromWeek(30));

        assertEquals(3, pwsAll.size());

        PlayerWeeklyStats pws = playerWeeklyStatsRepository.findByWeeksAndTag("0", Week.fromWeek(30), Week.fromWeek(30)).get(0);
        assertEquals(100, (int)pws.getChestContribution());
        assertEquals(200, (int)pws.getCardDonation());

        pws = playerWeeklyStatsRepository.findByWeeksAndTag("1", Week.fromWeek(30), Week.fromWeek(30)).get(0);
        assertEquals(100, (int)pws.getChestContribution());
        assertNull(pws.getCardDonation());

        pws = playerWeeklyStatsRepository.findByWeeksAndTag("2", Week.fromWeek(30), Week.fromWeek(30)).get(0);
        assertEquals(130, (int)pws.getChestContribution());
        assertEquals(220, (int)pws.getCardDonation());
    }


    @Test
    public void updateChestContributionsWithBiggerFalse() {
        Player p0 = new Player("0", "1", "irrelevant");
        Player p1 = new Player("1", "1", "irrelevant");
        Player p2 = new Player("2", "1", "irrelevant");
        Player p3 = new Player("3", "1", "irrelevant");

        List<PlayerWeeklyStats> donations = Arrays.asList(
                new PlayerWeeklyStats(p0, 30, null, null,0,0),
                new PlayerWeeklyStats(p1, 30, 1, 10,0,0),
                new PlayerWeeklyStats(p2, 30, 2, 30,0,0),
                new PlayerWeeklyStats(p3, 30, 3, 230,0,0)
        );

        updateStatsService.updateChestContibutionAndRole(donations, Week.fromWeek(30), false);

        List<PlayerWeeklyStats> pwsAll = playerWeeklyStatsRepository.findByWeek(Week.fromWeek(30));

        assertEquals(3, pwsAll.size());

        PlayerWeeklyStats pws = playerWeeklyStatsRepository.findByWeeksAndTag("0", Week.fromWeek(30), Week.fromWeek(30)).get(0);
        assertNull(pws.getChestContribution());
        assertEquals(200, (int)pws.getCardDonation());

        pws = playerWeeklyStatsRepository.findByWeeksAndTag("1", Week.fromWeek(30), Week.fromWeek(30)).get(0);
        assertEquals(1, (int)pws.getChestContribution());
        assertNull(pws.getCardDonation());

        pws = playerWeeklyStatsRepository.findByWeeksAndTag("2", Week.fromWeek(30), Week.fromWeek(30)).get(0);
        assertEquals(2, (int)pws.getChestContribution());
        assertEquals(220, (int)pws.getCardDonation());
    }


}
