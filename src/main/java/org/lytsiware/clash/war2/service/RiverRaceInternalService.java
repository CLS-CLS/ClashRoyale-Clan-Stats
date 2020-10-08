package org.lytsiware.clash.war2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.core.domain.player.Player;
import org.lytsiware.clash.core.domain.player.PlayerRepository;
import org.lytsiware.clash.war2.domain.RiverRace;
import org.lytsiware.clash.war2.domain.RiverRaceClan;
import org.lytsiware.clash.war2.domain.RiverRaceParticipant;
import org.lytsiware.clash.war2.repository.RiverRaceRepository;
import org.lytsiware.clash.war2.service.integration.War2CRLIntegrationService;
import org.lytsiware.clash.war2.service.integration.dto.ClanDto;
import org.lytsiware.clash.war2.service.integration.dto.RiverRaceCurrentDto;
import org.lytsiware.clash.war2.service.integration.dto.RiverRaceLogDto;
import org.lytsiware.clash.war2.transformation.RiverRaceInternalMapper;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiverRaceInternalService {


    private final War2CRLIntegrationService integrationService;
    private final RiverRaceRepository riverRaceClanRepository;
    private final PlayerRepository playerRepository;


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

        List<ClanDto> clansDto = Stream.concat(Stream.of(dto.getClan()), dto.getClans().stream()).collect(Collectors.toList());
        List<RiverRaceClan> clans = Stream.concat(Stream.of(activeRace.getClan()), activeRace.getClans().stream()).collect(Collectors.toList());

        for (ClanDto clanDto : clansDto) {
            clans.stream()
                    .filter(rrc -> rrc.getTag().equals(clanDto.getTag()))
                    .findFirst()
                    .ifPresent(rrc -> updateActiveFame(rrc, clanDto));
        }

        insertGhostPlayers(activeRace.getClan(), playerRepository.findInClan());

        //some times the riverrace entity is not updated (only the clans) but we still want to
        //set the updatedOn
        activeRace.setUpdatedOn(LocalDateTime.now());

        return riverRaceClanRepository.saveAndFlush(activeRace);
    }

    /**
     * So it seems integration service does not return all the players in clan. I.e the inactives are not returned in the results
     * So we manually insert them with 0 stats (as it should have been by the intergation service)
     *
     * @param clan
     * @param playersInClan
     */
    private void insertGhostPlayers(RiverRaceClan clan, List<Player> playersInClan) {
        List<Player> ghostPlayers = playersInClan.stream().filter(p -> isNotPresent(clan, p)).collect(Collectors.toList());
        if (ghostPlayers.size() > 0) {
            log.info("Found ghost players  :  {}", ghostPlayers.stream().map(Player::getName).collect(Collectors.toList()));
        }
        for (Player player : playersInClan) {
            if (isNotPresent(clan, player)) {
                clan.getParticipants().add(RiverRaceParticipant.builder()
                        .fame(0)
                        .activeFame(0)
                        .repairPoints(0)
                        .tag(player.getTag())
                        .name(player.getName())
                        .build()
                );
            }
        }
    }

    private boolean isNotPresent(RiverRaceClan clan, Player player) {
        return !clan.getParticipants().stream()
                .filter(participant -> player.getTag().equals(participant.getTag()))
                .findFirst().isPresent();
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

        List<ClanDto> clansDto = riverRaceWeekDto.getStandings().stream().map(RiverRaceLogDto.StandingsDto::getClan).collect(Collectors.toList());
        List<RiverRaceClan> clans = Stream.concat(Stream.of(activeRace.getClan()), activeRace.getClans().stream()).collect(Collectors.toList());

        for (ClanDto clanDto : clansDto) {
            clans.stream()
                    .filter(rrc -> rrc.getTag().equals(clanDto.getTag()))
                    .findFirst()
                    .ifPresent(rrc -> updateActiveFame(rrc, clanDto));
        }
        insertGhostPlayers(activeRace.getClan(), playerRepository.findInClan());

        riverRaceClanRepository.saveAndFlush(activeRace);
    }

    private void updateActiveFame(RiverRaceClan clan, ClanDto clanDto) {
        if (!clan.isFinished()) {
            RiverRaceInternalMapper.INSTANCE.updateActiveFame(clanDto, clan);
        }
        if (clan.getFinishTime() != null) {
            clan.setFinished(true);
        }
    }
}


