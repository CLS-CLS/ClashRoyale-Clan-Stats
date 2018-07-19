package org.lytsiware.clash.domain.player;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
public class PlayerCheckInCheckOutRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    PlayerCheckInCheckOutRepository playerCheckInCheckOutRepository;

    @Test
    @Transactional
    public void findCheckedInAtDate() throws Exception {
        Player x = new Player("x", "", "");
        Player x2 = new Player("x2", "", "");
        entityManager.persist(x);
        entityManager.persist(x2);
        PlayerInOut playerInOut = PlayerInOut.builder()
                .checkIn(LocalDate.now().minusDays(5))
                .checkOut(LocalDate.now().minusDays(2))
                .tag("x")
                .build();
        entityManager.persist(playerInOut);

        playerInOut = PlayerInOut.builder().tag("x2").checkIn(LocalDate.now().minusDays(5)).build();
        entityManager.persist(playerInOut);

        entityManager.flush();
        entityManager.clear();

        List<PlayerInOut> checkedIn = playerCheckInCheckOutRepository.findCheckedInAtDate(LocalDate.now());
        assertEquals(1, checkedIn.size());
        checkedIn = playerCheckInCheckOutRepository.findCheckedInAtDate(LocalDate.now().minusDays(3));
        assertEquals(2, checkedIn.size());
        checkedIn = playerCheckInCheckOutRepository.findCheckedInAtDate(LocalDate.now().minusDays(7));
        assertEquals(0, checkedIn.size());
    }

}