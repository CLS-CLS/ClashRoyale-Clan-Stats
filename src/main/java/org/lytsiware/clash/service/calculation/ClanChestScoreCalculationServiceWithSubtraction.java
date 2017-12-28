package org.lytsiware.clash.service.calculation;

import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.lytsiware.clash.service.calculation.ClanChestScoreCalculationServiceWithSubtraction.SubractCalculationContext;

@Service
public class ClanChestScoreCalculationServiceWithSubtraction implements ClanChestScoreCalculationService<SubractCalculationContext> {

    private double calculateChestScoreFromData(List<Integer> data) {
        if (data.size() == 0) {
            return 1;
        }
        int numberOfPlayers = data.size();
        int crownsCollected = data.stream().reduce((r, e) -> r += e).get();
        Collections.sort(data);

        int lowerOptimalValue = crownsCollected / numberOfPlayers;
        int upperOptimalValue = (int) Math.ceil((double) crownsCollected / numberOfPlayers);

        double lowerValue = ((double) lowerOptimalValue) / crownsCollected;
        double upperValue = Math.ceil((double) upperOptimalValue) / crownsCollected;

        int upperValueMultitude = crownsCollected % numberOfPlayers;
        int lowerValueMultitude = numberOfPlayers - upperValueMultitude;

        BiFunction<Integer, Double, Double> completionPercentageFunction = (x, p) -> Math.min((double) x / crownsCollected, p);

        double lowerCompletionScoreScore = data.subList(0, lowerValueMultitude).stream().mapToDouble(x -> completionPercentageFunction.apply(x, lowerValue)).sum();
        double upperCompletionScoreScore = data.subList(lowerValueMultitude, data.size()).stream().mapToDouble(x -> completionPercentageFunction.apply(x, upperValue)).sum();

        double completionScore = lowerCompletionScoreScore + upperCompletionScoreScore;

        List<Integer> overflowData = data.subList(0, lowerValueMultitude).stream()
                .filter(x -> x > lowerOptimalValue)
                .map(d -> d - lowerOptimalValue)
                .collect(Collectors.toList());
        overflowData.addAll(data.subList(lowerValueMultitude, data.size()).stream()
                .filter(x -> x > upperOptimalValue)
                .map(d -> d - upperOptimalValue)
                .collect(Collectors.toList()));

        double overflowDeviationPercentage = calculateChestScoreFromData(overflowData);

        completionScore = completionScore * overflowDeviationPercentage;

        return completionScore;
    }


    @Override
    public double calculateChestScore(List<PlayerWeeklyStats> playerWeeklyStats) {
        SubractCalculationContext context = initContext(playerWeeklyStats);
        calculateCrownScore(context);
        calculateDeviation(context);
        calculateFinalDeviation(context);
        return context.getFinalDeviation();
    }

    @Override
    public void calculateCrownScore(SubractCalculationContext context) {
        context.set(SubractCalculationContext.CROWN_SCORE_PERC, (double)context.getCollectedCrowns() / 1600);

    }

    @Override
    public void calculateFinalDeviation(SubractCalculationContext context) {
        context.set(SubractCalculationContext.FINAL_DEVIATION, context.getPlayerDeviationPercentage() * context.get(SubractCalculationContext.CROWN_SCORE_PERC, Double.class));
    }

    @Override
    public void calculateDeviation(SubractCalculationContext context) {
        double deviationPercentage = calculateChestScoreFromData(context.getData());
        context.set(SubractCalculationContext.PLAYER_DEVIATION_PERC, deviationPercentage);
    }

    @Override
    public SubractCalculationContext initContext(List<PlayerWeeklyStats> playerWeeklyStats) {
        List<Integer> data = playerWeeklyStats.stream().map(PlayerWeeklyStats::getChestContribution).filter(Objects::nonNull).collect(Collectors.toList());
        SubractCalculationContext context = new SubractCalculationContext();
        context.set(SubractCalculationContext.DATA, data);
        context.set(SubractCalculationContext.COLLECTED_CROWNS, data.stream().reduce((r, d) -> r += d).get());
        return context;
    }

    public static class SubractCalculationContext extends CalculationContext {

        public static final String DATA = "data";
        public static final String COLLECTED_CROWNS = "collectedCrowns";
        public static final String PLAYER_DEVIATION_PERC = "playerDeviationPerc";
        public static final String FINAL_DEVIATION = "finalDeviation";
        public static final String CROWN_SCORE_PERC = "crownScorePercentage";

        public int getCollectedCrowns() {
            return get(COLLECTED_CROWNS, Integer.class);
        }

        public List<Integer> getData() {
            return get(DATA, List.class);
        }

        public double getPlayerDeviationPercentage() {
            return get(PLAYER_DEVIATION_PERC, Double.class);
        }

        public double getFinalDeviation() {
            return get(FINAL_DEVIATION, Double.class);
        }


    }


    public static void main(String[] args) {
        Function<List<Integer>, List<PlayerWeeklyStats>> mapper = intList -> intList.stream().map(i -> new PlayerWeeklyStats(new Player("","",""), 0,
                i,0,0,0 )).collect(Collectors.toList());

        double score = new ClanChestScoreCalculationServiceWithSubtraction().calculateChestScoreFromData(Arrays.asList(2, 2, 2, 1));
        System.out.println(score);
        score = new ClanChestScoreCalculationServiceWithSubtraction().calculateChestScoreFromData(Arrays.asList(400, 400, 0, 0));
        System.out.println(score);
        score = new ClanChestScoreCalculationServiceWithSubtraction().calculateChestScoreFromData(Arrays.asList(800, 400, 0, 0));
        System.out.println(score);
        score = new ClanChestScoreCalculationServiceWithSubtraction().calculateChestScoreFromData(Arrays.asList(700, 500, 0, 0));
        System.out.println(score);
        score = new ClanChestScoreCalculationServiceWithSubtraction().calculateChestScoreFromData(Arrays.asList(400, 500, 300, 0));
        System.out.println(score);

        score = new ClanChestScoreCalculationServiceWithSubtraction().calculateChestScore(mapper.apply(Arrays.asList(2, 2, 2, 1)));
        System.out.println(score);
        score = new ClanChestScoreCalculationServiceWithSubtraction().calculateChestScore(mapper.apply(Arrays.asList(400, 400, 0, 0)));
        System.out.println(score);
        score = new ClanChestScoreCalculationServiceWithSubtraction().calculateChestScore(mapper.apply(Arrays.asList(800, 400, 0, 0)));
        System.out.println(score);
        score = new ClanChestScoreCalculationServiceWithSubtraction().calculateChestScore(mapper.apply(Arrays.asList(700, 500, 0, 0)));
        System.out.println(score);
        score = new ClanChestScoreCalculationServiceWithSubtraction().calculateChestScore(mapper.apply(Arrays.asList(400, 500, 300, 0)));
        System.out.println(score);
        score = new ClanChestScoreCalculationServiceWithSubtraction().calculateChestScore(mapper.apply(Arrays.asList(400, 500, 300, 400)));
        System.out.println(score);
        score = new ClanChestScoreCalculationServiceWithSubtraction().calculateChestScore(mapper.apply(Arrays.asList(350, 350, 150, 100, 100, 100, 50, 40, 40, 40, 30, 30, 30, 30, 20, 20, 20, 20, 10, 10, 8, 8, 8, 8, 5, 5, 3, 2, 1, 1, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
        System.out.println(score);

    }


}
