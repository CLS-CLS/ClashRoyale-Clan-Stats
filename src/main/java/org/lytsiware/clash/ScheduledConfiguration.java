package org.lytsiware.clash;


import org.lytsiware.clash.service.job.ScheduledName;
import org.lytsiware.clash.service.job.ScheduledNameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Method;

@Configuration
public class ScheduledConfiguration implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledConfiguration.class);

    @Autowired
    ScheduledNameService scheduledNameService;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Class<?> beanClass = applicationContext.getBean(beanName).getClass();
            for (Method m : beanClass.getDeclaredMethods()) {
                if (m.isAnnotationPresent(ScheduledName.class)) {
                    if (m.isAnnotationPresent(Scheduled.class)) {
                        String value = m.getAnnotation(ScheduledName.class).value();
                        scheduledNameService.register(value, beanClass, m);
                    } else {
                        throw new IllegalArgumentException(
                                String.format("Method %s.%s was annotated with @%s but was not annotated with @%s.",
                                        beanClass, m.getName(), ScheduledName.class.getSimpleName(), Scheduled.class.getSimpleName()));
                    }
                }
            }
        }
    }
}
