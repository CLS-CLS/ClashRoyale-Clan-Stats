package org.lytsiware.clash.war2.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.war2.service.integration.dto.ClanDto;
import org.lytsiware.clash.war2.service.integration.dto.ParticipantDto;
import org.lytsiware.clash.war2.service.integration.dto.RiverRaceCurrentDto;
import org.lytsiware.clash.war2.service.integration.dto.RiverRaceLogDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Collections;
import java.util.Comparator;

@Service
@Slf4j
@RequiredArgsConstructor
public class War2CRLIntegrationService {

    private final ExchangeHelperService exchangeHelperService;

    @Value("${clanTag}")
    String clanTag;

    @Value("${riverRaceCurrentUrl}")
    private String riverRaceCurrentUrl;

    @Value("${riverRaceLogUrl}")
    private String riverRaceLogUrl;

    public RiverRaceCurrentDto getCurrentRiverRace() {
        log.info("Getting Current RiverRace date from CRL");
        RiverRaceCurrentDto result = exchangeHelperService.exchangeCurrentRiverRace(URI.create(riverRaceCurrentUrl));
        normalizeCurrentRiverRace(result);
        log.debug("{}", result);
        return result;
    }


    public RiverRaceLogDto getRiverRaceLog(Integer limit) {
        log.info("Getting River Race LOG data from CRL");
        RiverRaceLogDto result = exchangeHelperService.exchangeRiverRaceLog(URI
                .create(limit == null ? riverRaceLogUrl : riverRaceLogUrl + "?limit=" + limit));
        removeOtherClans(result);
        log.debug("{}", result);
        return result;
    }

    private void removeOtherClans(RiverRaceLogDto result) {
        for (var item : result.getItems()) {
            item.setStandings(item.getStandings().stream().filter(s -> s.getClan().getTag().equals(clanTag)).toList());
        }

    }


    protected RiverRaceCurrentDto normalizeCurrentRiverRace(RiverRaceCurrentDto raceDto) {
        // Do not persist other clans stats
//        raceDto.setClans(raceDto.getClans().stream()
//                .filter(clanDto -> !clanDto.getTag().equals(raceDto.getClan().getTag()))
//                .collect(Collectors.toList()));
        raceDto.setClans(Collections.emptyList());
        raceDto.getClans().sort(Comparator.comparing(ClanDto::getTag));
        raceDto.getClan().getParticipants().sort(Comparator.comparing(ParticipantDto::getTag));
        raceDto.getClans().forEach(clan -> clan.getParticipants().sort(Comparator.comparing(ParticipantDto::getTag)));
        return raceDto;
    }


}
