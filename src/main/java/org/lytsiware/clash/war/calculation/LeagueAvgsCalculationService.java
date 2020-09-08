package org.lytsiware.clash.war.calculation;

import org.lytsiware.clash.war.domain.league.WarLeague;
import org.lytsiware.clash.war.domain.playerwarstat.PlayerWarStat;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class LeagueAvgsCalculationService {

    /**
     * Calculates the <ul>
     * <li>Average number of cards collected per player
     * <li>the total collection cards collected</li>
     * <li>The win ratio of the team</li>
     * <li>the team score for this war</li>
     * <li>the trophies of the clan after this war</li>
     * </ul>
     *
     * @param context the data needed for the calculations
     * @return the calculation results;
     */
    public WarLeague calculateLeagueAvgs(WarLeagueAvgCalculationContext context) {

        Set<PlayerWarStat> statsList = context.getStatsList();
        int cardAvg = (int) statsList.stream().mapToInt(pws -> pws.getCollectionPhaseStats().getCardsWon()).filter(cardsWon -> cardsWon != 0).average().orElse(0);
        double winPercentage = (double) statsList.stream().mapToInt(pws -> pws.getWarPhaseStats().getGamesWon()).sum() /
                Math.max(statsList.stream().mapToInt(pws -> pws.getWarPhaseStats().getGamesGranted()).sum(), 1);
        Integer collectedCards = statsList.stream().mapToInt(pws -> pws.getCollectionPhaseStats().getCardsWon()).sum();

        //just a placeholder for the calculations, to avoid creating another dto
        WarLeague warLeague = new WarLeague(LocalDateTime.now());
        warLeague.setTeamCardAvg(cardAvg);
        warLeague.setTeamWinRatio(winPercentage);
        warLeague.setTeamTotalCards(collectedCards);
        warLeague.setTeamScore((int) ((0.5 + 0.5 * winPercentage) * collectedCards));
        warLeague.setTotalTrophies(context.getCurrentTrophies() + context.getTrophiesDelta());

        return warLeague;

    }
}
