package org.lytsiware.clash.service.job;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ScheduledNameService {

    void register(String value, Class<?> beanClass, Method m);

    List<Map<String, String>> getScheduledNames();

    Object runScheduler(String name);

    void markTime(String name);

    LocalDateTime getLastRun(String name);
}
