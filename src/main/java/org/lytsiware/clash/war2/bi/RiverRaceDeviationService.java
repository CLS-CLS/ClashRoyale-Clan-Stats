package org.lytsiware.clash.war2.bi;

import lombok.Getter;
import lombok.Setter;
import org.lytsiware.clash.war2.domain.RiverRaceParticipant;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class RiverRaceDeviationService {

    private double calculateChestScoreFromData(List<Integer> data) {
        if (data.size() == 0) {
            return 1;
        }
        int numberOfPlayers = data.size();
        int fameCollected = data.stream().mapToInt(i -> i).sum();
        Collections.sort(data);

        int lowerOptimalValue = fameCollected / numberOfPlayers;
        int upperOptimalValue = (int) Math.ceil((double) fameCollected / numberOfPlayers);

        double lowerValue = ((double) lowerOptimalValue) / fameCollected;
        double upperValue = Math.ceil(upperOptimalValue) / fameCollected;

        int upperValueMultitude = fameCollected % numberOfPlayers;
        int lowerValueMultitude = numberOfPlayers - upperValueMultitude;

        BiFunction<Integer, Double, Double> completionPercentageFunction = (x, p) -> Math.min((double) x / fameCollected, p);

        double lowerCompletionScoreScore = data.subList(0, lowerValueMultitude).stream().mapToDouble(x -> completionPercentageFunction.apply(x, lowerValue)).sum();
        double upperCompletionScoreScore = data.subList(lowerValueMultitude, data.size()).stream().mapToDouble(x -> completionPercentageFunction.apply(x, upperValue)).sum();

        double completionScore = lowerCompletionScoreScore + upperCompletionScoreScore;

        List<Integer> overflowData = Stream.concat(
                data.subList(0, lowerValueMultitude).stream()
                        .filter(x -> x > lowerOptimalValue)
                        .map(d -> d - lowerOptimalValue),
                data.subList(lowerValueMultitude, data.size()).stream()
                        .filter(x -> x > upperOptimalValue)
                        .map(d -> d - upperOptimalValue)
        ).collect(Collectors.toList());


        double overflowDeviationPercentage = calculateChestScoreFromData(overflowData);

        completionScore = completionScore * overflowDeviationPercentage;

        return completionScore;
    }

    public SubractCalculationContext calculateChestScore(List<RiverRaceParticipant> riverRaceParticipants) {
        SubractCalculationContext context = initContext(riverRaceParticipants);
        calculateFameCompletionPerc(context);
        calculateDeviation(context);
        calculateFinalDeviation(context);
        return context;
    }

    public void calculateFameCompletionPerc(SubractCalculationContext context) {
        context.setCrownScorePercentage((double) Math.min(50000, context.getActiveFame()) / 50000);
    }

    public void calculateFinalDeviation(SubractCalculationContext context) {
        context.setFinalDeviation(context.getPlayerDeviationPercentage() * context.getCrownScorePercentage());
    }

    public void calculateDeviation(SubractCalculationContext context) {
        double deviationPercentage = calculateChestScoreFromData(context.getData());
        context.setPlayerDeviationPercentage(deviationPercentage);
    }

    public SubractCalculationContext initContext(List<RiverRaceParticipant> riverRaceParticipants) {
        List<Integer> data = riverRaceParticipants.stream().map(RiverRaceParticipant::getActiveFame)
                .filter(Objects::nonNull)
                .sorted(Comparator.reverseOrder())
                .limit(50)
                .collect(Collectors.toList());
        SubractCalculationContext context = new SubractCalculationContext();
        context.setData(data);
        context.setActiveFame(data.stream().reduce((r, d) -> r += d).orElse(0));
        return context;
    }

    @Getter
    @Setter
    public static class SubractCalculationContext {
        private Integer activeFame;
        private List<Integer> data;
        private double playerDeviationPercentage;
        private double finalDeviation;
        private double crownScorePercentage;
    }

    //TODO move this as a test
    public static void main(String[] args) {
        Function<List<Integer>, List<RiverRaceParticipant>> mapper = intList -> intList.stream()
                .map(i -> RiverRaceParticipant.builder().activeFame(i).build())
                .collect(Collectors.toList());

        double score = new RiverRaceDeviationService().calculateChestScoreFromData(Arrays.asList(2, 2, 2, 1));
        score = new RiverRaceDeviationService().calculateChestScoreFromData(Arrays.asList(400, 400, 0, 0));
        score = new RiverRaceDeviationService().calculateChestScoreFromData(Arrays.asList(25000, 25000, 0, 0));
        System.out.println(score);
        score = new RiverRaceDeviationService().calculateChestScoreFromData(Arrays.asList(700, 500, 0, 0));
        System.out.println(score);
        score = new RiverRaceDeviationService().calculateChestScoreFromData(Arrays.asList(400, 500, 300, 0));
        System.out.println(score);

        score = new RiverRaceDeviationService().calculateChestScore(mapper.apply(Arrays.asList(2, 2, 2, 1))).getFinalDeviation();
        System.out.println(score);
        score = new RiverRaceDeviationService().calculateChestScore(mapper.apply(Arrays.asList(400, 400, 0, 0))).getFinalDeviation();
        System.out.println(score);
        score = new RiverRaceDeviationService().calculateChestScore(mapper.apply(Arrays.asList(800, 400, 0, 0))).getFinalDeviation();
        System.out.println(score);
        score = new RiverRaceDeviationService().calculateChestScore(mapper.apply(Arrays.asList(700, 500, 0, 0))).getFinalDeviation();
        System.out.println(score);
        score = new RiverRaceDeviationService().calculateChestScore(mapper.apply(Arrays.asList(400, 500, 300, 0))).getFinalDeviation();
        System.out.println(score);
        score = new RiverRaceDeviationService().calculateChestScore(mapper.apply(Arrays.asList(400, 500, 300, 400))).getFinalDeviation();
        System.out.println(score);
        score = new RiverRaceDeviationService().calculateChestScore(mapper.apply(Arrays.asList(350, 350, 150, 100, 100, 100, 50, 40, 40, 40, 30, 30, 30, 30, 20, 20, 20, 20, 10, 10, 8, 8, 8, 8, 5, 5, 3, 2, 1, 1, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)))
                .getFinalDeviation();
        System.out.println(score);

        score = new RiverRaceDeviationService().calculateChestScore(mapper.apply(Arrays.asList(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)))
                .getPlayerDeviationPercentage();
        System.out.println(score);

    }


}
