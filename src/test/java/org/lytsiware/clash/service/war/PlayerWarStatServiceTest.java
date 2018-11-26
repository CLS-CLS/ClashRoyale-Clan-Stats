package org.lytsiware.clash.service.war;

import org.junit.Assert;
import org.junit.Test;
import org.lytsiware.clash.AbstractSpringBootTest;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.player.PlayerInOut;
import org.lytsiware.clash.dto.war.input.WarStatsInputDto;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

public class PlayerWarStatServiceTest extends AbstractSpringBootTest {

    private static LocalDateTime localDateTime = LocalDateTime.of(2018, 12, 12, 12, 0);

    @Autowired
    PlayerWarStatsService playerWarStatsService;


    @Test
    @Transactional
    public void findPlayersNotParticipatedInWarTest() {
        //when
        createPlayerInOutData();
        WarStatsInputDto inputDto = WarStatsInputDto.builder().playerWarStats(Collections.emptyList()).build();

        Map<Player, PlayerInOut> notParticipated = playerWarStatsService.findPlayersNotParticipatedInWar(inputDto, localDateTime, 60);
        Assert.assertEquals(3, notParticipated.size());


        notParticipated = playerWarStatsService.findPlayersNotParticipatedInWar(inputDto, localDateTime, 40);
        Assert.assertEquals(2, notParticipated.size());

        notParticipated = playerWarStatsService.findPlayersNotParticipatedInWar(inputDto, localDateTime, null);
        Assert.assertEquals(1, notParticipated.size());

        notParticipated = playerWarStatsService.findPlayersNotParticipatedInWar(inputDto, localDateTime.plusHours(2), 60);
        Assert.assertEquals(5, notParticipated.size());

        notParticipated = playerWarStatsService.findPlayersNotParticipatedInWar(inputDto, localDateTime.plusHours(2), 40);
        Assert.assertEquals(4, notParticipated.size());

        notParticipated = playerWarStatsService.findPlayersNotParticipatedInWar(inputDto, localDateTime.plusHours(2), 0);
        Assert.assertEquals(3, notParticipated.size());

    }


    private void createPlayerInOutData() {
        for (int i = 0; i < 10; i++) {
            String tag = "p" + i;
            em.persist(new Player(tag, tag, ""));

            //for each 3 players we checkout 30 mins after
            LocalDateTime checkout = (i % 3 == 0 ? localDateTime.plusMinutes(30 * (i + 1)) : null);
            PlayerInOut pIO = new PlayerInOut(tag, localDateTime.plusMinutes(30 * i), checkout);
            em.persist(pIO);
        }

    }


}
