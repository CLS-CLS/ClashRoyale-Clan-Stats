package org.lytsiware.clash.service.war;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.league.WarLeagueRepository;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.dto.WarLeagueDto;
import org.lytsiware.clash.dto.WarLeagueWithParticipantsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityExistsException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WarLeagueServiceImpl implements WarLeagueService {

    @Autowired
    private WarLeagueRepository warLeagueRepository;

    @Override
    public void calculateLeagueAvgs(WarLeague warLeague) {
        List<PlayerWarStat> statsList = new ArrayList<>(warLeague.getPlayerWarStats());
        int cardAvg = (int) statsList.stream().mapToInt(pws -> pws.getCollectionPhaseStats().getCardsWon()).filter(cardsWon -> cardsWon != 0).average().orElse(0);
        double winPercentage = (double) statsList.stream().mapToInt(pws -> pws.getWarPhaseStats().getGamesWon()).sum() /
                Math.max(statsList.stream().mapToInt(pws -> pws.getWarPhaseStats().getGamesGranted()).sum(), 1);
        warLeague.setTeamCardAvg(cardAvg);
        warLeague.setTeamWinRatio(winPercentage);
        warLeague.setTeamTotalCards(statsList.stream().mapToInt(pws -> pws.getCollectionPhaseStats().getCardsWon()).sum());
        warLeague.setTeamScore((int) ((0.5 + 0.5 * winPercentage) * warLeague.getTeamTotalCards()));
        warLeagueRepository.findFirstNthWarLeaguesBeforeDate(warLeague.getStartDate(), 1).stream()
                .findFirst().map(WarLeague::getTotalTrophies).map(tt -> tt + warLeague.getTrophies()).ifPresent(warLeague::setTotalTrophies);
    }

    @Override
    public WarLeague calculateLeagueAvgsAndSave(WarLeague warLeague) throws EntityExistsException {
        calculateLeagueAvgs(warLeague);
        warLeagueRepository.persistAndFlush(warLeague);
        return warLeague;
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
