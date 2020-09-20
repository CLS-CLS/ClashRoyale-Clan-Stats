package org.lytsiware.clash.war2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.war2.domain.RiverRace;
import org.lytsiware.clash.war2.repository.RiverRaceRepository;
import org.lytsiware.clash.war2.service.integration.War2CRLIntegrationService;
import org.lytsiware.clash.war2.service.integration.dto.RiverRaceCurrentDto;
import org.lytsiware.clash.war2.service.integration.dto.RiverRaceLogDto;
import org.lytsiware.clash.war2.transformation.RiverRaceInternalMapper;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiverRaceInternalService {


    private final War2CRLIntegrationService integrationService;
    private final RiverRaceRepository riverRaceClanRepository;


    /**
     * finds the latest active race in the repository or creates a new if there is not one.
     * Gets the new data from CLR-api and updates the entity accordingly.
     */
    @Transactional
    public RiverRace updateActiveRace() {
        RiverRaceCurrentDto dto = integrationService.getCurrentRiverRace();
        Optional<RiverRace> activeRaceOptional = riverRaceClanRepository.activeRace();

        return doUpdateActiveRace(dto, activeRaceOptional.orElse(null));
    }

    public RiverRace doUpdateActiveRace(RiverRaceCurrentDto dto, RiverRace activeRace) {
        log.info("updating active race");
        if (activeRace != null && activeRace.getSectionIndex() != dto.getSectionIndex()) {
            log.error("Error while updating race");
            throw new SectionIndexMissmatchException(activeRace.getSectionIndex(), dto.getSectionIndex());
        }
        if (activeRace == null) {
            log.info("active race not found, creating new");
        }
        activeRace = (activeRace != null ? activeRace : new RiverRace());

        RiverRaceInternalMapper.INSTANCE.update(dto, activeRace);
        if (!activeRace.isFinished()) {
            RiverRaceInternalMapper.INSTANCE.updateActiveFame(dto, activeRace);
        }
        if (activeRace.getClan().getFinishTime() != null) {
            activeRace.setFinished(true);
        }

        return riverRaceClanRepository.saveAndFlush(activeRace);
    }

    /**
     * Update the active race and sets its active status to false.
     *
     * @throws RaceNotFoundException          if there is not an active race to finalize
     * @throws SectionIndexMissmatchException if the section index of the active race and the update race
     *                                        are not the same
     */
    @Transactional
    public void finalizeRace(String clanTag) {
        log.info("Finalizing race");
        RiverRace activeRace = riverRaceClanRepository.activeRace().orElse(null);
        if (activeRace == null) {
            throw new RaceNotFoundException("Active Race not found");
        }

        RiverRaceLogDto riverRaceLogDto = integrationService.getRiverRaceLog(1);

        if (activeRace.getSectionIndex() != riverRaceLogDto.getItems().get(0).getSectionIndex()) {
            log.error("Error while finalizing race");
            throw new SectionIndexMissmatchException(activeRace.getSectionIndex(),
                    riverRaceLogDto.getItems().get(0).getSectionIndex());
        }

        RiverRaceLogDto.RiverRaceWeekDto riverRaceWeekDto = riverRaceLogDto.getItems().get(0);
        activeRace.setActive(false);
        RiverRaceInternalMapper.INSTANCE.updateFromRiverRaceWeekDto(riverRaceWeekDto, activeRace, clanTag);
        riverRaceClanRepository.saveAndFlush(activeRace);
    }
}


