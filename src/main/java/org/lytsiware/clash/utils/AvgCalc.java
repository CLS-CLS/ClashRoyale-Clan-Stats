package org.lytsiware.clash.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AvgCalc {
    private static final Logger logger = LoggerFactory.getLogger(AvgCalc.class);


    private static final double REMAINING_CHEST_WEIGHT = 0.80;
    private static final double PLAYER_DIVIATION_WEIGHT = 1 - REMAINING_CHEST_WEIGHT;

    public static double avg(int[] stats) {
        int sum = 0;
        for (int i = 0; i < stats.length; i++) {
            sum += stats[i];
        }
        return sum / stats.length;
    }


    public static double clanChestScore1(List<Integer> crownsPerPlayer) {
        int numberOfPlayers = crownsPerPlayer.size();

        logger.debug("number of players = {}", numberOfPlayers);

        int optimalChestContribution = 1600 / numberOfPlayers;


        //if the devision is not precise then the optimal is x or x+1
        int[] optimalChestContributions = {optimalChestContribution, (1600 % numberOfPlayers != 0) ? optimalChestContribution++ : optimalChestContribution};

        int playersWithOptimal1 = numberOfPlayers - 1600 % numberOfPlayers;
        int playersWithOptimal2 = 1600 % numberOfPlayers;

        logger.debug("Optimal chest contributions : {} for {} players and  {} for the rest {} players ", optimalChestContributions[0], playersWithOptimal1, optimalChestContributions[1], playersWithOptimal2);


        double maxDeviation = 1600 * 1600 * REMAINING_CHEST_WEIGHT +
                (Math.pow(optimalChestContributions[0], 2) * playersWithOptimal1 + Math.pow(optimalChestContributions[1], 2) * playersWithOptimal2) * PLAYER_DIVIATION_WEIGHT;

        logger.debug("Max deviation = {}", maxDeviation);

        //find the first {{playerWithOptimal1}} number of players that have the smallest deviation with the optimalChestContribution
        //key = crown, value = deviation

        List<Integer> crownsOrderedByDeviation = crownsPerPlayer.stream().map(c -> new Pair(c, c - optimalChestContributions[0])).sorted(Comparator.comparing(p -> p.value))
                .limit(playersWithOptimal1).map(p -> p.key).collect(Collectors.toList());

        List<Integer> remainingCrowns = removeAllDistinct(crownsPerPlayer, crownsOrderedByDeviation);

        int deviationResult = 0;

        for (Integer crown : crownsOrderedByDeviation) {
            deviationResult += Math.pow(optimalChestContributions[0] - crown, 2);
        }
        for (Integer crown : remainingCrowns) {
            deviationResult += Math.pow(optimalChestContributions[1] - crown, 2);
        }

        logger.debug("Players deviation result {}", deviationResult);

        int crownResult = (int) Math.pow(1600 - crownsPerPlayer.stream().mapToInt(x -> x).sum(), 2);

        logger.debug("Crown Deviation result {}", crownResult);

        double finalResult = deviationResult * PLAYER_DIVIATION_WEIGHT + crownResult * REMAINING_CHEST_WEIGHT;

        double finalResultPerc = (maxDeviation - finalResult) * 100 / maxDeviation;

        logger.debug("final Result = {}, as percentage = {}%", finalResult, finalResultPerc);

        return finalResultPerc;

    }


    public static double clanChestScore2(List<Integer> crownsPerPlayer) {
        int numberOfPlayers = crownsPerPlayer.size();

        logger.debug("number of players = {}", numberOfPlayers);

        int crownsCollected = crownsPerPlayer.stream().mapToInt(x -> x).sum();

        int optimalChestContribution = crownsCollected / numberOfPlayers;


        //if the devision is not precise then the optimal is x or x+1
        int[] optimalChestContributions = {optimalChestContribution, (crownsCollected % numberOfPlayers != 0) ? optimalChestContribution + 1 : optimalChestContribution};

        int playersWithOptimal1 = numberOfPlayers - crownsCollected % numberOfPlayers;
        int playersWithOptimal2 = crownsCollected % numberOfPlayers;

        logger.debug("Optimal chest contributions : {} for {} players and  {} for the rest {} players ", optimalChestContributions[0], playersWithOptimal1, optimalChestContributions[1], playersWithOptimal2);


        double maxDeviation =  Math.pow(optimalChestContributions[0], 2) * playersWithOptimal1 + Math.pow(optimalChestContributions[1], 2) * playersWithOptimal2;

        logger.debug("Max deviation = {}", maxDeviation);

        //find the first {{playerWithOptimal1}} number of players that have the smallest deviation with the optimalChestContribution
        //key = crown, value = deviation

        List<Integer> crownsOrderedByDeviation = crownsPerPlayer.stream().map(c -> new Pair(c, c - optimalChestContributions[0])).sorted(Comparator.comparing(p -> p.value))
                .limit(playersWithOptimal1).map(p -> p.key).collect(Collectors.toList());

        List<Integer> remainingCrowns = removeAllDistinct(crownsPerPlayer, crownsOrderedByDeviation);

        int deviationResult = 0;

        for (Integer crown : crownsOrderedByDeviation) {
            deviationResult += Math.pow(optimalChestContributions[0] - crown, 2);
        }
        for (Integer crown : remainingCrowns) {
            deviationResult += Math.pow(optimalChestContributions[1] - crown, 2);
        }

        double deviationResultPerc = 100 - (deviationResult * 100) / maxDeviation;

        logger.debug("Players deviation result = {}, as percentage = {}", deviationResult, deviationResultPerc);

        double crownResultPerc = crownsCollected * 100 / 1600;

        logger.debug("Crown Deviation Percentage =  {}", crownResultPerc);

        double finalResultPerc = (deviationResultPerc * crownResultPerc) /100;

        logger.debug("final as percentage = {}%", finalResultPerc);

        return finalResultPerc;

    }

    public static void main(String[] args) {
        List<Integer> data = Arrays.asList(267, 267, 267, 266, 266,266);
        System.out.println(AvgCalc.clanChestScore2(data));
        data = Arrays.asList(300, 300, 300, 300);
        System.out.println(AvgCalc.clanChestScore2(data));
    }

    private static <T> List<T> removeAllDistinct(List<T> list, List<T> listToRemove) {
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


}
