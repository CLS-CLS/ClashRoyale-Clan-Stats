package org.lytsiware.clash.war2.migration;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.war2.domain.RiverRace;
import org.lytsiware.clash.war2.service.RiverRaceInternalService;
import org.lytsiware.clash.war2.service.integration.War2CRLIntegrationService;
import org.lytsiware.clash.war2.service.integration.dto.RiverRaceLogDto;
import org.lytsiware.clash.war2.transformation.RiverRaceInternalMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@Service
@Profile("war2migration")
@Slf4j
public class MigrateWar2Runner implements CommandLineRunner {

    @Autowired
    War2CRLIntegrationService integrationService;

    @Autowired
    RiverRaceInternalService riverRaceService;

    @Autowired
    EntityManager em;


    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting migration");
        RiverRaceLogDto riverRaceLogDto = integrationService.getRiverRaceLog(null);
        for (RiverRaceLogDto.RiverRaceWeekDto rrwDto : riverRaceLogDto.getItems()) {
            RiverRace riverRace = RiverRace.builder()
                    .active(false)
                    .build();
            RiverRaceInternalMapper.INSTANCE.updateFromRiverRaceWeekDto(rrwDto, riverRace, "PG2000PL");
            riverRace.setSectionIndex(rrwDto.getSectionIndex());
            riverRace.getClan().getParticipants().forEach(p ->
                    p.setActiveFame(p.getFame()));
            em.persist(riverRace);
        }
        log.info("log migrated\r\nmigrating current");
        riverRaceService.updateActiveRace();
        em.flush();
        log.info("Migration end");
    }
}
