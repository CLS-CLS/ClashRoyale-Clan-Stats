package org.lytsiware.clash.service.job.scheduledname;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;



@Configuration
@Aspect
public class ScheduledNameAspect {

    Logger logger = LoggerFactory.getLogger(ScheduledNameAspect.class);

    @Autowired
    private ScheduledNameService service;

    @Around("@annotation(org.lytsiware.clash.service.job.scheduledname.ScheduledName)")
    public void afterRunning(ProceedingJoinPoint joinPoint) throws Throwable {
        joinPoint.proceed();
        String value = ((MethodSignature)joinPoint.getSignature()).getMethod().getAnnotation(ScheduledName.class).value();
        logger.info("mark time for Scheduler {}", value );
        service.markTime(value);
    }

}