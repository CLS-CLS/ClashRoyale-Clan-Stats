package org.lytsiware.clash.service.job;

import java.lang.reflect.Method;
import java.util.List;

public interface ScheduledNameService {

    void register(String value, Class<?> beanClass, Method m);

    List<String> getScheduledNames();

    Object runScheduler(String name);
}
