package org.lytsiware.clash.service.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Service
public class ScheduledNameServiceImpl implements ScheduledNameService {

    @Autowired
    private ApplicationContext applicationContext;

    private Logger logger = LoggerFactory.getLogger(ScheduledNameServiceImpl.class);

    private HashMap<String, ScheduledNameContext> scheduledMethods = new HashMap<>();

    @Override
    public void register(String value, Class<?> beanClass, Method m) {
        if (scheduledMethods.containsKey(value)) {
            throw new IllegalArgumentException(String.format("A scheduled method is already registered under the name %s", value));
        }
        logger.info("Registering scheduled method {} under name {}", beanClass, m.getName(), value);
        Class<?> returnType = m.getReturnType();
        scheduledMethods.put(value, new ScheduledNameServiceImpl.ScheduledNameContext(beanClass, m));
    }

    @Override
    public List<String> getScheduledNames() {
        return new ArrayList<>(scheduledMethods.keySet());
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
            throw new IllegalArgumentException("No scheduler is registered under the name %s" + name);
        }
        return scheduledNameContext.invoke(applicationContext);
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

        ScheduledNameContext(Class<?> clazz, Method method) {
            this.clazz = clazz;
            this.method = method;
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
