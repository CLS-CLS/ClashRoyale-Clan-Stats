package org.lytsiware.clash.war.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.war.calculation.LeagueAvgsCalculationService;
import org.lytsiware.clash.war.calculation.WarLeagueAvgCalculationContext;
import org.lytsiware.clash.war.domain.league.WarLeague;
import org.lytsiware.clash.war.domain.league.WarLeagueRepository;
import org.lytsiware.clash.war.dto.WarLeagueDto;
import org.lytsiware.clash.war.dto.WarLeagueWithParticipantsDto;
import org.springframework.stereotype.Service;

import javax.persistence.EntityExistsException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class WarLeagueServiceImpl implements WarLeagueService {

    private final WarLeagueRepository warLeagueRepository;

    private final LeagueAvgsCalculationService leagueAvgsCalculationService;


    @Override
    public WarLeague calculateLeagueAvgsAndSave(WarLeague warLeague) throws EntityExistsException {

        // also returns the league to be persisted (before means "<=" in the custom query)
        // But for this case we want the leagues before this one (this one is excluded, so we make sure
        // 1. we return two leagues and 2. we filter out this one in case it exists)
        Integer currentTrophies = warLeagueRepository.findFirstNthWarLeaguesBeforeDate(warLeague.getStartDate(), 2).stream()
                .filter(streamedWarLeague -> !streamedWarLeague.getStartDate().isEqual(warLeague.getStartDate()))
                .findFirst().map(WarLeague::getTotalTrophies).orElse(0);

        WarLeagueAvgCalculationContext context = new WarLeagueAvgCalculationContext();
        context.setCurrentTrophies(currentTrophies);
        context.setStatsList(warLeague.getPlayerWarStats());
        context.setTrophiesDelta(warLeague.getTrophies());

        WarLeague calculationResults = leagueAvgsCalculationService.calculateLeagueAvgs(context);
        warLeague.setTeamCardAvg(calculationResults.getTeamCardAvg());
        warLeague.setTeamWinRatio(calculationResults.getTeamWinRatio());
        warLeague.setTeamTotalCards(calculationResults.getTeamTotalCards());
        warLeague.setTeamScore(calculationResults.getTeamScore());
        warLeague.setTotalTrophies(calculationResults.getTotalTrophies());
        warLeague.setName(warLeague.getStartDate().atTime(warLeague.getTime()).toString());

        return warLeagueRepository.save(warLeague);
    }


    @Override
    public List<WarLeagueDto> findFirstNthWarLeagueBeforeDate(LocalDate startDate, int n) {
        return warLeagueRepository.findFirstNthWarLeaguesBeforeDate(startDate, n).stream().map(WarLeagueDto::new).collect(Collectors.toList());
    }

    @Override
    public WarLeagueWithParticipantsDto findStatsForWarLeague(int deltaWar) {
        Optional<WarLeague> result = warLeagueRepository.findNthWarLeague(deltaWar);
        return result.map(WarLeagueWithParticipantsDto::new).orElse(null);
    }


}
