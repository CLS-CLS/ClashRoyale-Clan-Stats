package org.lytsiware.clash.core.service.job.scheduledname;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ScheduledNameService {

    void register(String value, Class<?> beanClass, Method m);

    List<Map<String, String>> getScheduledInfo();

    Object runScheduler(String name);

    void markTime(String name);

    LocalDateTime getLastRun(String name);
}
