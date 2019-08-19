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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.annotation.PostConstruct;
import java.time.Clock;
import java.util.Arrays;

@SpringBootApplication
//@EnableCaching
@EnableScheduling
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


    }
}
