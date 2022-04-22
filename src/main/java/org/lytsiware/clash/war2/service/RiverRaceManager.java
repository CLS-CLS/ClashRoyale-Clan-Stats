package org.lytsiware.clash.war2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.war2.domain.RiverRace;
import org.lytsiware.clash.war2.repository.RiverRaceRepository;
import org.lytsiware.clash.war2.service.integration.War2CRLIntegrationService;
import org.lytsiware.clash.war2.service.integration.dto.RiverRaceCurrentDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiverRaceManager {

    @Value("${clanTag}")
    private String clanTag;

    private final RiverRaceInternalService riverRaceService;
    private final RiverRaceRepository riverRaceRepository;
    private final War2CRLIntegrationService integrationService;

    /**
     * Updates the riverrace by gathering data and deciding if the active riverrace should
     * be updated or updated and finalised. For the latter the next active race is created
     */
    @Transactional
    public RiverRace updateRiverRace() {
        log.info("Updating river race");
        RiverRaceCurrentDto current = integrationService.getCurrentRiverRace();
        //do not persist the rest of clans to save space in db (its free so its small)
        current.setClans(new ArrayList<>());
        RiverRace active = riverRaceRepository.activeRace().orElse(null);

        if (active != null && current.getSectionIndex() != active.getSectionIndex()) {
            riverRaceService.finalizeRace(clanTag);
            active = null;
        }

        return riverRaceService.doUpdateActiveRace(current, active);
    }
}
