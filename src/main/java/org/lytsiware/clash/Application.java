package org.lytsiware.clash;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.annotation.PostConstruct;
import java.time.Clock;
import java.util.Arrays;
import java.util.concurrent.Executor;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableRetry
@EnableJpaRepositories(basePackages = "org.lytsiware.clash.domain")
@Slf4j
public class Application {


    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        System.out.println("ACTIVE PROFILES : " + Arrays.toString(context.getEnvironment().getActiveProfiles()));
    }

    @Configuration
    public static class Config {

        public static final String WAR_INPUT_EXECUTOR = "WAR_INPUT_EXECUTOR";


        @Autowired
        ObjectMapper objectMapper;

        @PostConstruct
        public ObjectMapper configureSpringObjectMapper() {
            objectMapper.findAndRegisterModules();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper;

        }


        @Bean
        public Clock clock() {
            return Clock.systemDefaultZone();
        }

        @Bean
        public TaskScheduler taskScheduler() {
            ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
            taskScheduler.setPoolSize(1);
            return taskScheduler;

        }

        /**
         * allow only one task to be executed, reject the others.
         * Warstats input should only be done once per time
         */
        @Bean(name = Config.WAR_INPUT_EXECUTOR)
        public Executor warStatInputExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setQueueCapacity(0);
            executor.setCorePoolSize(1);
            executor.setMaxPoolSize(1);
            return executor;
        }


    }
}
