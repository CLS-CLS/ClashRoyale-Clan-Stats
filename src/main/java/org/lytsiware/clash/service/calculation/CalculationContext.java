package org.lytsiware.clash.service.calculation;

import java.util.HashMap;
import java.util.Map;

public class CalculationContext {

    Map<String, Object> context = new HashMap<>();

    Map<String, Object> getContext() {
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
