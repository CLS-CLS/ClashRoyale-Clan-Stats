package org.lytsiware.clash;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class Application {


    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        System.out.println("ACTIVE PROFILES : " + Arrays.toString(context.getEnvironment().getActiveProfiles()));
    }

    @Configuration
    public static class Config {

        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper;

        }


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


//        @PostConstruct
//        public void initProxySettings() throws MalformedURLException {
//            URL proxyUrl = new URL(System.getenv("QUOTAGUARDSTATIC_URL"));
//            String userInfo = proxyUrl.getUserInfo();
//            String user = userInfo.substring(0, userInfo.indexOf(':'));
//            String password = userInfo.substring(userInfo.indexOf(':') + 1);
//
//            System.setProperty("http.proxyHost", proxyUrl.getHost());
//            System.setProperty("http.proxyPort", Integer.toString(proxyUrl.getPort()));
//
//            Authenticator.setDefault(new Authenticator() {
//                protected PasswordAuthentication getPasswordAuthentication() {
//                    return new PasswordAuthentication(user, password.toCharArray());
//                }
//            });
//        }

        @Bean
        @Qualifier("fixie")
        public Proxy fixieProxy() {
            return createProxy(getFixieUrl());
        }

//        @Bean
//        @Qualifier("quotaGuard")
//        public Proxy quotaGuardProxy() {
//            return createProxy(getQuotaGuardUrl());
//        }

        private Proxy createProxy(String url) {
            String[] urlValues = url.split("[/(:\\/@)/]+");
            if (urlValues.length < 4) {
                log.warn("proxy url {} is not correct - Defaulting to NO_PROXY", url);
                return Proxy.NO_PROXY;
            }
            String user = urlValues[1];
            String password = urlValues[2];
            String host = urlValues[3];
            int port = Integer.parseInt(urlValues[4]);
            log.info("Creating proxy with host {} : ", host);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));

            Authenticator authenticator = new Authenticator() {
                public PasswordAuthentication getPasswordAuthentication() {
                    return (new PasswordAuthentication(user, password.toCharArray()));
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
