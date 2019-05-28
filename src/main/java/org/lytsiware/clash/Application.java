package org.lytsiware.clash;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.time.Clock;
import java.util.Arrays;

@SpringBootApplication
//@EnableCaching
@EnableScheduling
@EnableRetry
@EnableJpaRepositories(basePackages = "org.lytsiware.clash.domain")
public class Application {


    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        System.out.println("ACTIVE PROFILES : " + Arrays.toString(context.getEnvironment().getActiveProfiles()));
    }

    @Configuration
    public static class Config {
        @Bean
        public Clock clock() {
            return Clock.systemDefaultZone();
        }


        @Value("${FIXIE_URL}")
        private String fixieUrl;


        @Bean
        public TaskScheduler taskScheduler() {
            ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
            taskScheduler.setPoolSize(1);
            return taskScheduler;

        }

        @Bean
        @Qualifier("fixie")
        public Proxy fixieProxy() {
            String[] fixieValues = getFixieUrl().split("[/(:\\/@)/]+");
            if (fixieValues.length < 4) {
                return Proxy.NO_PROXY;
            }
            String fixieUser = fixieValues[1];
            String fixiePassword = fixieValues[2];
            String fixieHost = fixieValues[3];
            int fixiePort = Integer.parseInt(fixieValues[4]);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(fixieHost, fixiePort));

            Authenticator authenticator = new Authenticator() {
                public PasswordAuthentication getPasswordAuthentication() {
                    return (new PasswordAuthentication(fixieUser, fixiePassword.toCharArray()));
                }
            };
            Authenticator.setDefault(authenticator);
            return proxy;
        }

        public String getFixieUrl() {
            return fixieUrl;
        }
    }
}
