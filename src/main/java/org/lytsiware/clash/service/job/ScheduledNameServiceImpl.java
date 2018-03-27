package org.lytsiware.clash.service.job;

import org.lytsiware.clash.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class ScheduledNameServiceImpl implements ScheduledNameService {


    @Autowired
    private ApplicationContext applicationContext;

    private Logger logger = LoggerFactory.getLogger(ScheduledNameServiceImpl.class);

    private Map<String, ScheduledNameContext> scheduledMethods = new HashMap<>();

    @Override
    public void register(String value, Class<?> beanClass, Method m) {
        if (scheduledMethods.containsKey(value)) {
            throw new IllegalArgumentException(String.format("A scheduled method is already registered under the name %s", value));
        }
        logger.info("Registering scheduled method {} under name {}", beanClass, m.getName(), value);
        scheduledMethods.put(value, new ScheduledNameServiceImpl.ScheduledNameContext(beanClass, m));
    }

    @Override
    public List<Map<String, String>> getScheduledNames() {
        Function<Map.Entry<String, ScheduledNameContext>, Map<String, String>> createMap = e -> {
            Map<String, String> map = new HashMap<>();
            map.put("name", e.getKey());
            map.put("last run: ", String.valueOf(e.getValue().getLastRun()));
            return map;
        };
        return scheduledMethods.entrySet().stream().map(e -> createMap.apply(e)).collect(Collectors.toList());

    }

    /**
     * @param name the name of the registered scheduler
     * @throws IllegalArgumentException if there is no scheduler with such name
     * @throws RuntimeException         if the underlying invocation of the method throws an error.
     * @see Method#invoke(Object, Object...)
     */

    @Override
    public Object runScheduler(String name) {
        ScheduledNameContext scheduledNameContext = scheduledMethods.get(name);
        if (scheduledNameContext == null) {
            throw new IllegalArgumentException("No scheduler is registered under the name " + name);
        }
        return scheduledNameContext.invoke(applicationContext);
    }

    @Override
    public void markTime(String name) {
        LocalDateTime now = LocalDateTime.now();
        scheduledMethods.get(name).setLastRun(now);


    }

    @Override
    public LocalDateTime getLastRun(String name) {
        return scheduledMethods.get(name).getLastRun();
    }

    @PostConstruct
    private void postConstruct() {
        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Class<?> beanClass = null;
            try {
                Object bean = applicationContext.getBean(beanName);
                beanClass = AopUtils.getTargetClass(bean);
            } catch (Exception ex) {
                logger.debug("Bean could not be fetched", ex);
            }
            if (beanClass == null) {
                continue;
            }
            for (Method m : beanClass.getDeclaredMethods()) {
                if (m.isAnnotationPresent(ScheduledName.class)) {
                    if (m.isAnnotationPresent(Scheduled.class)) {
                        String value = m.getAnnotation(ScheduledName.class).value();
                        this.register(value, beanClass, m);
                    } else {
                        throw new IllegalArgumentException(
                                String.format("Method %s.%s was annotated with @%s but was not annotated with @%s.",
                                        beanClass, m.getName(), ScheduledName.class.getSimpleName(), Scheduled.class.getSimpleName()));
                    }
                }
            }
        }

    }

    public static class ScheduledNameContext {
        final Class<?> clazz;
        final Method method;
        LocalDateTime lastRun;

        ScheduledNameContext(Class<?> clazz, Method method) {
            this.clazz = clazz;
            this.method = method;
        }

        public LocalDateTime getLastRun() {
            return lastRun;
        }

        public void setLastRun(LocalDateTime lastRun) {
            this.lastRun = lastRun;
        }

        Object invoke(ApplicationContext applicationContext) {
            try {
                return method.invoke(applicationContext.getBean(clazz));
            } catch (Exception e) {
                throw new RuntimeException("Error while invoking the method", e);
            }
        }
    }
}
