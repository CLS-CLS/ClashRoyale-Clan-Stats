package org.lytsiware.clash;

import org.junit.Assert;
import org.junit.Test;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStatsRepository;
import org.lytsiware.clash.dto.PlayerOverallStats;
import org.lytsiware.clash.service.donations.DonationAggregationService;
import org.lytsiware.clash.service.donations.DonationStatsService;
import org.lytsiware.clash.service.donations.UpdateStatService;
import org.lytsiware.clash.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Transactional
public class DonationStatsServiceTest extends AbstractSpringBootTest {

    @Autowired
    DonationStatsService donationStatsService;

    @Autowired
    PlayerWeeklyStatsRepository pwsRepo;

    @Autowired
    private DonationAggregationService donationAggregationService;

    @Autowired
    private UpdateStatService updateStatsService;

    @Test
    public void calculateAvgsWithNull() {
        Player player = new Player("1", "Chris", "Elder");
        PlayerWeeklyStats pws1 = new PlayerWeeklyStats(player, 1, 10, 10, 10, 10);
        PlayerWeeklyStats pws2 = new PlayerWeeklyStats(player, 2, null, null, 10, 10);
        PlayerWeeklyStats pws3 = new PlayerWeeklyStats(player, 3, null, 50, 20, 20);
        PlayerWeeklyStats pws4 = new PlayerWeeklyStats(player, 4, 60, null, 0, 0);
        pwsRepo.saveOrUpdateAll(Arrays.asList(pws1, pws2, pws3, pws4));
        List<PlayerWeeklyStats> results = donationAggregationService.calculateAvgs(Week.fromWeek(4));
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

        Player splayer = new Player("tag1", "Chris1", "Elder");
        Player splayer2 = new Player("tag2", "Chris2", "Elder");
        Player splayer3 = new Player("tag3", "Chris3", "Elder");
        PlayerWeeklyStats s1 = new PlayerWeeklyStats(splayer, 1, 0, 10, 10, 10);
        PlayerWeeklyStats s2 = new PlayerWeeklyStats(splayer2, 1, 0, 20, 10, 10);
        PlayerWeeklyStats s3 = new PlayerWeeklyStats(splayer3, 1, 0, 5, 10, 10);
        Player anotherPlayer = new Player("tag4", "Chris4", "Elder");
        PlayerWeeklyStats s4 = new PlayerWeeklyStats(anotherPlayer, 1, 0, 30, 10, 10);
        pwsRepo.saveOrUpdateAll(Arrays.asList(db1, db2, db3));

        updateStatsService.updatePlayerWeeklyStats(Arrays.asList(s1, s2, s3, s4), Week.fromWeek(1), true);


        Map<String, Integer> results = pwsRepo.findByWeek(Week.fromWeek(1)).stream()
                .collect(Collectors.toMap(s -> s.getPlayer().getTag(), PlayerWeeklyStats::getCardDonation));

        Assert.assertEquals(10, (int) results.get("tag1"));
        Assert.assertEquals(20, (int) results.get("tag2"));
        Assert.assertEquals(20, (int) results.get("tag3"));
        Assert.assertEquals(30, (int) results.get("tag4"));
    }

    @Test
    public void updateDonationsNextWeek() {
        Player player = new Player("tag1", "Chris1", "Elder");
        PlayerWeeklyStats db1 = new PlayerWeeklyStats(player, 1, 0, null, 10, 10);
        Player player2 = new Player("tag2", "Chris2", "Elder");
        PlayerWeeklyStats db2 = new PlayerWeeklyStats(player2, 1, 0, 10, 10, 10);
        Player player3 = new Player("tag3", "Chris3", "Elder");
        PlayerWeeklyStats db3 = new PlayerWeeklyStats(player3, 1, 0, 20, 10, 10);

        pwsRepo.saveOrUpdateAll(Arrays.asList(db1, db2, db3));

        Player splayer = new Player("tag1", "Chris1", "Elder");
        PlayerWeeklyStats s1 = new PlayerWeeklyStats(splayer, 1, 0, 10, 10, 10);

        updateStatsService.updatePlayerWeeklyStats(Arrays.asList(s1), Week.fromWeek(2), true);

        List<PlayerWeeklyStats> results = pwsRepo.findByWeek(Week.fromWeek(2));
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(2, (int) results.get(0).getWeek());

        results = pwsRepo.findByWeek(Week.fromWeek(1));
        Assert.assertEquals(3, results.size());
    }


    @Test
    public void updateContributions() {
        Player player = new Player("tag1", "Chris1", "Elder");
        PlayerWeeklyStats db1 = new PlayerWeeklyStats(player, 1, null, 0, 0, 0);
        Player player2 = new Player("tag2", "Chris2", "Elder");
        PlayerWeeklyStats db2 = new PlayerWeeklyStats(player2, 1, 10, 0, 0, 0);

        PlayerWeeklyStats s1 = new PlayerWeeklyStats(player, 1, 100, 0, 0, 0);
        PlayerWeeklyStats s2 = new PlayerWeeklyStats(player2, 1, 200, 0, 0, 0);
        PlayerWeeklyStats s3 = new PlayerWeeklyStats(new Player("3", "nope", "whatever"), 1, 200, 0, 0, 0);

        pwsRepo.saveOrUpdateAll(Arrays.asList(db1, db2));

        updateStatsService.updateChestContibutionAndRole(Arrays.asList(s1, s2, s3), Week.fromWeek(1), false);

        Map<String, Integer> results = pwsRepo.findByWeek(Week.fromWeek(1)).stream()
                .collect(Utils.collectToMap(s -> s.getPlayer().getTag(), PlayerWeeklyStats::getChestContribution));


        Assert.assertEquals(2, results.size());
        Assert.assertEquals(100, (int) results.get("tag1"));
        Assert.assertEquals(200, (int) results.get("tag2"));

    }

    @Test
    public void getNewPlayersAtWeeks() {
        Player player1 = new Player("Player1", "p1", "Elder");
        Player player2 = new Player("Player2", "p2", "Elder");
        Player player3 = new Player("Player3", "p3", "Elder");
        PlayerWeeklyStats p1w1 = new PlayerWeeklyStats(player1, 10, 10, 20, 0, 0);
        PlayerWeeklyStats p1w2 = new PlayerWeeklyStats(player1, 11, 20, 30, 0, 0);
        PlayerWeeklyStats p2w2 = new PlayerWeeklyStats(player2, 11, 30, 40, 0, 0);
        PlayerWeeklyStats p3w2 = new PlayerWeeklyStats(player3, 11, 40, 50, 0, 0);

        pwsRepo.saveOrUpdateAll(Arrays.asList(p1w1, p1w2, p2w2, p3w2));

        List<PlayerOverallStats> newPlayers = donationStatsService.findNewPlayersOfWeeks(Week.fromWeek(10), Week.fromWeek(11))
                .getNewPlayers();

        Assert.assertEquals(2, newPlayers.size());

        //convert to map for easier testing
        Map<String, PlayerOverallStats> newPlayersMap = newPlayers.stream().collect(Collectors.toMap(PlayerOverallStats::getTag, Function.identity()));

        Assert.assertEquals(40, (int) newPlayersMap.get("Player3").getChestContribution());
        Assert.assertEquals(40, (int) newPlayersMap.get("Player2").getCardDonation());

    }

}
