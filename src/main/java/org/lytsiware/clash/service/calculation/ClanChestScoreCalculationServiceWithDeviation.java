package org.lytsiware.clash.service.calculation;

import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.lytsiware.clash.service.calculation.ClanChestScoreCalculationServiceWithDeviation.DeviationCalculationContext;

/**
 * Deprecated in favor of {@link ClanChestScoreCalculationServiceWithSubtraction} because the results are not satisfactory enough
 */
@Deprecated
public class ClanChestScoreCalculationServiceWithDeviation implements ClanChestScoreCalculationService<DeviationCalculationContext> {

    public static final Logger logger = LoggerFactory.getLogger(ClanChestScoreCalculationServiceWithDeviation.class);

    @Override
    public double calculateChestScore(List<PlayerWeeklyStats> playerWeeklyStats) {
        DeviationCalculationContext context = initContext(playerWeeklyStats);
        calculateDeviation(context);
        calculateCrownScore(context);
        calculateFinalDeviation(context);
        return context.getFinalDeviation();
    }


    @Override
    public void calculateCrownScore(DeviationCalculationContext context) {
        double crownResultPerc = (double) (context.getCollectedCrowns() * 100) / 1600;

        logger.debug("Crown Deviation Percentage =  {}", crownResultPerc);

        context.set(DeviationCalculationContext.CROWN_SCORE_PERC, crownResultPerc);
    }

    @Override
    public void calculateFinalDeviation(DeviationCalculationContext context) {
        double crownResultPerc = context.get(DeviationCalculationContext.CROWN_SCORE_PERC, Double.class);

        double finalResultPerc = (context.getPlayerDeviationPercentage() * crownResultPerc) / 100;

        context.set(DeviationCalculationContext.FINAL_DEVIATION, finalResultPerc);

        logger.debug("final as percentage = {}%", finalResultPerc);
    }

    @Override
    public void calculateDeviation(DeviationCalculationContext context) {

        //find the first {{playerWithOptimal1}} number of players that have the smallest deviation with the optimalChestContribution
        //key = crown, value = deviation
        List<Integer> crownsOrderedByDeviation = context.getData().stream().map(c -> new Pair(c, c - context.getOptimalChestContributions()[0])).sorted(Comparator.comparing(p -> p.value))
                .limit(context.getPlayersWithOptimal()[0]).map(p -> p.key).collect(Collectors.toList());

        List<Integer> remainingCrowns = removeAllDistinct(context.getData(), crownsOrderedByDeviation);

        int deviationResult = 0;

        for (Integer crown : crownsOrderedByDeviation) {
            deviationResult += Math.pow(context.getOptimalChestContributions()[0] - crown, 2);
        }
        for (Integer crown : remainingCrowns) {
            deviationResult += Math.pow(context.getOptimalChestContributions()[1] - crown, 2);
        }

        double deviationResultPerc = 100 - ((double) deviationResult * 100) / context.getMaxDeviation();

        context.set(DeviationCalculationContext.PLAYER_DEVIATION, deviationResult);
        context.set(DeviationCalculationContext.PLAYER_DEVIATION_PERC, deviationResultPerc);

        logger.debug("Players deviation result = {}, as percentage = {}", deviationResult, deviationResultPerc);
    }

    @Override
    public DeviationCalculationContext initContext(List<PlayerWeeklyStats> playerWeeklyStats) {
        return initContext(playerWeeklyStats, null);
    }

    public DeviationCalculationContext initContext(List<PlayerWeeklyStats> playerWeeklyStats, List<Integer> toTest) {
        DeviationCalculationContext context = new DeviationCalculationContext();
        List<Integer> crownsPerPlayer;
        if (toTest != null) {
            context.set(DeviationCalculationContext.DATA, toTest);
            crownsPerPlayer = toTest;
        } else {
            crownsPerPlayer = playerWeeklyStats.stream().filter(p -> p.getChestContribution() != null).map(PlayerWeeklyStats::getChestContribution).collect(Collectors.toList());
            context.set(DeviationCalculationContext.DATA, crownsPerPlayer);
        }

        int numberOfPlayers = crownsPerPlayer.size();
        logger.debug("number of players = {}", numberOfPlayers);

        int crownsCollected = crownsPerPlayer.stream().mapToInt(x -> x).sum();
        context.set(DeviationCalculationContext.COLLECTED_CROWNS, crownsCollected);

        int optimalChestContribution = crownsCollected / numberOfPlayers;
        //if the division is not precise then the optimal is x or x+1
        int[] optimalChestContributions = {optimalChestContribution, (crownsCollected % numberOfPlayers != 0) ? optimalChestContribution + 1 : optimalChestContribution};

        context.set(DeviationCalculationContext.OPTIMAL_CHEST_CONTRIBUTIONS, optimalChestContributions);

        int playersWithOptimal1 = numberOfPlayers - crownsCollected % numberOfPlayers;
        int playersWithOptimal2 = crownsCollected % numberOfPlayers;
        context.set(DeviationCalculationContext.PLAYERS_WITH_OPTIMAL, new int[]{playersWithOptimal1, playersWithOptimal2});

        logger.debug("Optimal chest contributions : {} for {} players and  {} for the rest {} players ", optimalChestContributions[0], playersWithOptimal1, optimalChestContributions[1], playersWithOptimal2);

        //the max deviation happens when one player has gathered all the crowns.
        boolean twoOptimalContributionExist = (optimalChestContributions[0] != optimalChestContributions[1]);
        int maxDeviation;

        if (twoOptimalContributionExist) {
            maxDeviation = (int) (Math.pow(optimalChestContributions[0], 2) * playersWithOptimal1 + Math.pow(optimalChestContributions[1], 2) * (playersWithOptimal2 - 1) +
                    Math.pow(crownsCollected - optimalChestContributions[1], 2));
        } else {
            maxDeviation = (int) (Math.pow(optimalChestContributions[1], 2) * (playersWithOptimal1 - 1) + Math.pow(crownsCollected - optimalChestContributions[0], 2));
        }

        context.set(DeviationCalculationContext.MAX_DEVIATION, maxDeviation);

        logger.debug("Max deviation = {}", maxDeviation);

        return context;
    }

    public static class DeviationCalculationContext extends CalculationContext{

        public static final String DATA = "data";
        public static final String COLLECTED_CROWNS = "collectedCrowns";
        public static final String OPTIMAL_CHEST_CONTRIBUTIONS = "optimalChestContributions";
        public static final String PLAYERS_WITH_OPTIMAL = "playersWithOptimal";
        public static final String MAX_DEVIATION = "maxDeviation";
        public static final String PLAYER_DEVIATION = "playerDeviation";
        public static final String PLAYER_DEVIATION_PERC = "playerDeviationPerc";
        public static final String FINAL_DEVIATION = "finalDeviation";
        public static final String CROWN_SCORE_PERC = "crownScorePercentage";

        public int getCollectedCrowns() {
            return get(COLLECTED_CROWNS, Integer.class);
        }

        public int[] getOptimalChestContributions() {
            return get(OPTIMAL_CHEST_CONTRIBUTIONS, int[].class);
        }

        public int[] getPlayersWithOptimal() {
            return get(PLAYERS_WITH_OPTIMAL, int[].class);
        }

        public int getMaxDeviation() {
            return get(MAX_DEVIATION, Integer.class);
        }

        public List<Integer> getData() {
            return get(DATA, List.class);
        }

        public int getPlayerDeviation() {
            return get(PLAYER_DEVIATION, Integer.class);
        }

        public double getPlayerDeviationPercentage() {
            return get(PLAYER_DEVIATION_PERC, Double.class);
        }

        public double getFinalDeviation() {
            return get(FINAL_DEVIATION, Double.class);
        }

    }

    private <T> List<T> removeAllDistinct(List<T> list, List<T> listToRemove) {
        List<T> result = new ArrayList<>(list);
        for (Object objectToRemove : listToRemove) {
            result.remove(objectToRemove);
        }
        return result;
    }

    private static class Pair {
        public final Integer key;
        public final Integer value;

        public Pair(Integer key, Integer value) {
            this.key = key;
            this.value = value;
        }
    }


    public double calculateChestScoreTest(List<Integer> playerWeeklyStats) {
        if (playerWeeklyStats.size() == 0) {
            return 0d;
        }

        DeviationCalculationContext context = initContext(null, playerWeeklyStats);
        calculateDeviation(context);
        calculateCrownScore(context);
        calculateFinalDeviation(context);
        return context.getFinalDeviation();
    }

    public static void main(String[] args) {
        List<Integer> data = Arrays.asList(267, 267, 267, 266, 266, 266);
        System.out.println(new ClanChestScoreCalculationServiceWithDeviation().calculateChestScoreTest(data));
        data = Arrays.asList(300, 300, 300, 300);
        System.out.println(new ClanChestScoreCalculationServiceWithDeviation().calculateChestScoreTest(data));

        data = Arrays.asList(350, 350, 150, 100, 100, 100, 50, 40, 40, 40, 30, 30, 30, 30, 20, 20, 20, 20, 10, 10, 8, 8, 8, 8, 5, 5, 3, 2, 1, 1, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        System.out.println(new ClanChestScoreCalculationServiceWithDeviation().calculateChestScoreTest(data));

        data = Arrays.asList(1600, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        System.out.println(new ClanChestScoreCalculationServiceWithDeviation().calculateChestScoreTest(data));

        //not satisfactory result. Should have been 50%
        data = Arrays.asList(800, 800, 0, 0);
        System.out.println(new ClanChestScoreCalculationServiceWithDeviation().calculateChestScoreTest(data));
    }
}
