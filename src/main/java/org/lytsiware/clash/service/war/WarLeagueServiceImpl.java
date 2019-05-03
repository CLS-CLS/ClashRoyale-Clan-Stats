package org.lytsiware.clash.service.war;

import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.league.WarLeagueRepository;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityExistsException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class WarLeagueServiceImpl implements WarLeagueService {

    @Autowired
    WarLeagueRepository warLeagueRepository;

    @Override
    public void calculateLeagueAvgs(WarLeague warLeague) {
        List<PlayerWarStat> statsList = new ArrayList<>(warLeague.getPlayerWarStats());
        int cardAvg = (int) statsList.stream().mapToInt(pws -> pws.getCollectionPhaseStats().getCardsWon()).filter(cardsWon -> cardsWon != 0).average().orElse(0);
        double winPercentage = (double) statsList.stream().mapToInt(pws -> pws.getWarPhaseStats().getGamesWon()).sum() /
                Math.max(statsList.stream().mapToInt(pws -> pws.getWarPhaseStats().getGamesGranted()).sum(), 1);
        warLeague.setTeamCardAvg(cardAvg);
        warLeague.setTeamWinRatio(winPercentage);
    }

    @Override
    public WarLeague calculateLeagueAvgsAndSave(WarLeague warLeague) throws EntityExistsException {
        calculateLeagueAvgs(warLeague);
        warLeagueRepository.persistAndFlush(warLeague);
        return warLeague;
    }

    @Override
    public List<WarLeague> findFirstNthWarLeaguesAfterDate(LocalDate startDate, int leagueSpan) {
        return warLeagueRepository.findFirstNthWarLeaguesAfterDate(startDate, leagueSpan);
    }


}
