package org.lytsiware.clash.core.service.war;

import org.junit.Test;
import org.lytsiware.clash.AbstractSpringBootTest;
import org.lytsiware.clash.core.domain.player.Player;
import org.lytsiware.clash.donation.domain.PlayerWeeklyStats;
import org.lytsiware.clash.war.domain.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.war.domain.playerwarstat.PlayerWarStatsRepository;
import org.lytsiware.clash.war.service.WarUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertEquals;

public class PlayerUploadServiceImplTest extends AbstractSpringBootTest {

    @Autowired
    WarUploadService warUploadService;

    @Autowired
    PlayerWarStatsRepository playerWarStatsRepository;

    @Test
    @Transactional
    public void testUpload() throws Exception {

        Player player1 = new Player("HellSpawn", "HellSpawn", "Member");
        Player player2 = new Player("LastSaram", "LastSaram", "Member");
        Player player3 = new Player("Moufas1", "Moufas1", "Member");
        Player player4 = new Player("Moufas2", "Moufas2", "Member");
        Player player5 = new Player("noobas1", "noobas1", "Member");
        Player player6 = new Player("noobas2", "noobas2", "Member");


        PlayerWeeklyStats weekStat1 = PlayerWeeklyStats.builder()
                .withWeek(69).withPlayer(player1).build();
        PlayerWeeklyStats weekStat2 = PlayerWeeklyStats.builder()
                .withWeek(69).withPlayer(player2).build();
        PlayerWeeklyStats weekStat3 = PlayerWeeklyStats.builder()
                .withWeek(69).withPlayer(player3).build();
        PlayerWeeklyStats weekStat4 = PlayerWeeklyStats.builder()
                .withWeek(69).withPlayer(player4).build();
        PlayerWeeklyStats weekStat5 = PlayerWeeklyStats.builder()
                .withWeek(69).withPlayer(player5).build();
        PlayerWeeklyStats weekStat6 = PlayerWeeklyStats.builder()
                .withWeek(69).withPlayer(player6).build();

        em.persist(player1);
        em.persist(player2);
        em.persist(player3);
        em.persist(player4);
        em.persist(player5);
        em.persist(player6);
        em.persist(weekStat1);
        em.persist(weekStat2);
        em.persist(weekStat3);
        em.persist(weekStat4);
        em.persist(weekStat5);
        em.persist(weekStat6);

        em.flush();
        warUploadService.upload(new MultipartFile[]{new MockMultipartFile("04-05-2018.csv", new FileInputStream(ResourceUtils.getFile("classpath:04-05-2018.csv")))});

        Map<String, PlayerWarStat> warStats = StreamSupport.stream(playerWarStatsRepository.findAll().spliterator(), false)
                .collect(Collectors.toMap(warStat -> warStat.getPlayer().getTag(), Function.identity()));

        assertEquals(6, warStats.size());
        assertEquals(0, (int) warStats.get("noobas2").getCollectionPhaseStats().getCardsWon());
        assertEquals(0, warStats.get("noobas2").getCollectionPhaseStats().getGamesPlayed());
        assertEquals(0, (int) warStats.get("noobas2").getWarPhaseStats().getGamesPlayed());
        assertEquals(0, (int) warStats.get("noobas2").getWarPhaseStats().getGamesGranted());

    }


}