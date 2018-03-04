package org.lytsiware.clash.service.job;


import org.hibernate.criterion.Example;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class ScheduledRegistrator implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledRegistrator.class);

    private Map<String, ScheduledNameContext> scheduledMethods = new HashMap<>();

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        for (String beanName: applicationContext.getBeanDefinitionNames()) {
            Class<?> beanClass = applicationContext.getBean(beanName).getClass();
            for (Method m : beanClass.getDeclaredMethods()) {
                if (m.isAnnotationPresent(ScheduledName.class)) {
                    if (m.isAnnotationPresent(Scheduled.class)) {
                        String value = m.getAnnotation(ScheduledName.class).value();
                        if (scheduledMethods.containsKey(value)) {
                            throw new IllegalArgumentException(String.format("A scheduled method is already registered under the name %s", value));
                        }
                        logger.info("Registering scheduled method {} under name {}", beanClass, m.getName(), value);
                        Class<?> returnType = m.getReturnType();
                        scheduledMethods.put(value, new ScheduledNameContext(beanClass, m));
                    }else {
                        throw new IllegalArgumentException(
                                String.format("Method %s.%s was annotated with @%s but was not annotated with @%s.",
                                beanClass, m.getName(), ScheduledName.class.getSimpleName(), Scheduled.class.getSimpleName()));
                    }
                }
            }
        }
    }

    public List<String> getScheduledNames() {
        return new ArrayList<>(scheduledMethods.keySet());
    }

    /**
     *
     * @param name the name of the registered scheduler
     * @exception IllegalArgumentException if there is no scheduler with such name
     * @exception RuntimeException if the underlying invocation of the method throws an error.
     * @see Method#invoke(Object, Object...)
     */
    public Object runScheduler(String name){
        ScheduledNameContext scheduledNameContext = scheduledMethods.get(name);
        if (scheduledNameContext == null){
            throw new IllegalArgumentException("No scheduler is registered under the name %s" + name);
        }
        return scheduledNameContext.invoke(applicationContext);


    }

    private static class ScheduledNameContext {
        final Class<?> clazz;
        final Method method;

        ScheduledNameContext(Class<?> clazz, Method method){
            this.clazz = clazz;
            this.method = method;
        }

        Object invoke(ApplicationContext applicationContext)  {
            try {
                return method.invoke(applicationContext.getBean(clazz));
            } catch (Exception e) {
                throw new RuntimeException("Error while invoking the method", e);
            }
        }

    }
}
