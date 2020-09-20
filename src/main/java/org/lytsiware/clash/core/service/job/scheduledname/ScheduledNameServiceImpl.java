package org.lytsiware.clash.core.service.job.scheduledname;

import org.lytsiware.clash.core.domain.job.Job;
import org.lytsiware.clash.core.domain.job.JobRepository;
import org.lytsiware.clash.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.PropertyResolver;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class ScheduledNameServiceImpl implements ScheduledNameService, ApplicationContextAware,
        ApplicationListener<ApplicationStartedEvent> {

    private static final DateTimeFormatter DATE_TIME_FORMATER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final PropertyResolver propertyResolver;

    private final JobRepository jobRepository;

    private final Logger logger = LoggerFactory.getLogger(ScheduledNameServiceImpl.class);

    private final Map<String, ScheduledNameContext> scheduledMethods = new HashMap<>();

    private ApplicationContext applicationContext;

    @Autowired
    public ScheduledNameServiceImpl(PropertyResolver propertyResolver, JobRepository jobRepository) {
        this.propertyResolver = propertyResolver;
        this.jobRepository = jobRepository;
    }

    @Override
    public void register(String value, Class<?> beanClass, Method m) {
        if (scheduledMethods.containsKey(value)) {
            throw new IllegalArgumentException(String.format("A scheduled method is already registered under the name %s", value));
        }
        logger.info("Registering scheduled method {} under name {}", beanClass, m.getName());
        scheduledMethods.put(value, new ScheduledNameServiceImpl.ScheduledNameContext(beanClass, m));
    }

    @Override
    public List<Map<String, String>> getScheduledInfo() {
        Function<Map.Entry<String, ScheduledNameContext>, Map<String, String>> createMap = scheduledNameContextEntry -> {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("name", scheduledNameContextEntry.getKey());

            LocalDateTime lastRun = getLastRun(scheduledNameContextEntry.getKey());
            map.put("last run ", lastRun != null ? lastRun.format(DATE_TIME_FORMATER) : "no record");

            String cronExpression = propertyResolver.resolvePlaceholders(
                    Optional.ofNullable(scheduledNameContextEntry.getValue().getMethod().getAnnotation(Scheduled.class)).map(Scheduled::cron)
                            .orElse(""));

            if (StringUtils.isEmpty(cronExpression)) {
                return map;
            }

            ZonedDateTime nextExecutionLocalDateTime = Utils.getNextExecutionDate(cronExpression, ZonedDateTime.now(ZoneId.systemDefault()));

            int minutesRemaining = (int) ChronoUnit.MINUTES.between(LocalDateTime.now(ZoneId.systemDefault()), nextExecutionLocalDateTime);
            int daysRemaining = minutesRemaining / 1440;
            int hoursRemaining = (minutesRemaining % 1440) / 60;
            minutesRemaining = minutesRemaining % 60;
            map.put("next run in", daysRemaining + " days, " + hoursRemaining + " hours, " + minutesRemaining + " minutes");

            return map;
        };

        List<Map<String, String>> result = scheduledMethods.entrySet().stream().map(createMap).collect(Collectors.toList());
        Map<String, String> serverTime = new HashMap<>();
        serverTime.put("Server Time now: ", LocalDateTime.now().format(DATE_TIME_FORMATER) + " " + ZoneId.systemDefault());
        result.add(serverTime);
        return result;

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
        try {
            return scheduledNameContext.invoke(applicationContext);
        } catch (Exception e) {
            logger.error("Error invoking scheduler", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void markTime(String name) {
        Job executedJob = jobRepository.findById(name).orElse(new Job(name, null));
        executedJob.setLatestExecution(LocalDateTime.now());
        jobRepository.save(executedJob);
    }

    @Override
    public LocalDateTime getLastRun(String name) {
        return jobRepository.findById(name).map(Job::getLatestExecution).orElse(null);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
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
                    this.register(m.getAnnotation(ScheduledName.class).name(), beanClass, m);
                    if (!m.isAnnotationPresent(Scheduled.class) && !m.getAnnotation(ScheduledName.class).missingScheduled()) {
                        throw new IllegalArgumentException(
                                String.format("Method %s.%s was annotated with @%s but was not annotated with @%s and missingScheduled was false.",
                                        beanClass, m.getName(), ScheduledName.class.getSimpleName(), Scheduled.class.getSimpleName()));
                    }
                }
            }
        }
    }


    static class ScheduledNameContext {
        final Class<?> clazz;
        final Method method;

        ScheduledNameContext(Class<?> clazz, Method method) {
            this.clazz = clazz;
            this.method = method;
        }

        public Class<?> getClazz() {
            return clazz;
        }

        public Method getMethod() {
            return method;
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
