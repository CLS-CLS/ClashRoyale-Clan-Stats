package org.lytsiware.clash.service.war;

import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.league.WarLeagueRepository;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.dto.ClanWarStatsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityExistsException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
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
        warLeague.setTeamScore((int) (0.5 + 0.5 * winPercentage) * warLeague.getTeamTotalCards());
    }

    @Override
    public WarLeague calculateLeagueAvgsAndSave(WarLeague warLeague) throws EntityExistsException {
        calculateLeagueAvgs(warLeague);
        warLeagueRepository.persistAndFlush(warLeague);
        return warLeague;
    }

    @Override
    public List<WarLeague> findFirstNthWarLeaguesAfterDate(LocalDate startDate, int n) {
        return warLeagueRepository.findFirstNthWarLeaguesAfterDate(startDate, n);
    }

    @Override
    public ClanWarStatsDto findStatsForWarLeague(int deltaWar) {
        Optional<WarLeague> result = warLeagueRepository.findNthWarLeague(deltaWar);
        return result.map(ClanWarStatsDto::new).orElse(null);
    }


}
