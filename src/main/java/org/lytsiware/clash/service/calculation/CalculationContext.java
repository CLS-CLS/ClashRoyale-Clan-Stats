package org.lytsiware.clash.service.calculation;

import java.util.HashMap;
import java.util.Map;

public class CalculationContext {

    public static final String DATA = "data";
    public static final String COLLECTED_CROWNS = "collectedCrowns";
    public static final String PLAYER_DEVIATION_PERC = "playerDeviationPerc";
    public static final String FINAL_DEVIATION = "finalDeviation";
    public static final String CROWN_SCORE_PERC = "crownScorePercentage";

    private Map<String, Object> context = new HashMap<>();

    private Map<String, Object> getContext() {
        return context;
    }

    public final <Y> Y get(String key, Class<Y> cast) {
        Object value = context.get(key);
        if (value == null) {
            throw new IllegalStateException(String.format("key %s does not exist", key));
        }
        return cast.cast(value);
    }

    public void set(String key, Object value) {
        context.put(key, value);
    }
}
