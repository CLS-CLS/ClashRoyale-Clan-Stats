package org.lytsiware.clash.runners;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.war2.domain.RiverRaceParticipant;
import org.lytsiware.clash.war2.repository.RiverRaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@Slf4j
//@Service
public class UpdateActiveRaceRunner implements CommandLineRunner {

    @Autowired
    EntityManager em;

    @Autowired
    RiverRaceRepository riverRaceRepository;

    @Override
    @Transactional
    public void run(String... args) {


        List<RiverRaceParticipant> participants =
                em.createNativeQuery("select * from river_race_participant where river_race_clan_fk=11302", RiverRaceParticipant.class)
                        .getResultList();


        for (RiverRaceParticipant participant : participants) {
            RiverRaceParticipant backup = participants.stream().filter(b -> b.getId().equals(participant.getId()))
                    .findFirst().orElseThrow(() ->
                            new RuntimeException("Could not find backup for participant " + participant.getId() + " " + participant.getName()));
            participant.setActiveFame(backup.getFame());
            log.info("participant {}@{} active fame set from {} to {}", participant.getName(), participant.getTag(),
                    backup.getActiveFame(), backup.getFame());
        }

        for (RiverRaceParticipant participant : participants) {
            em.merge(participant);
        }
    }
}
