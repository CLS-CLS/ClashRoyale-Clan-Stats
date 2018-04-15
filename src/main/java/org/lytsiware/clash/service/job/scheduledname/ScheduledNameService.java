package org.lytsiware.clash.service.job.scheduledname;

import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public interface ScheduledNameService {

    void register(String value, Class<?> beanClass, Method m);

    List<Map<String, String>> getScheduledNames();

    Object runScheduler(String name);

    void markTime(String name);

    ZonedDateTime getLastRun(String name);
}
