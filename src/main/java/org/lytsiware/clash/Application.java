package org.lytsiware.clash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

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

//    @Configuration
//    static class MvcConfiguration extends WebMvcConfigurerAdapter {
//
//    	@Autowired
//    	BaseUrlInterceptor baseUrlInterceptor;
//
//    	@Override
//    	public void addInterceptors(InterceptorRegistry registry) {
//    	    registry.addInterceptor(baseUrlInterceptor);
//    	}
//    }

    @Configuration
    static class Config {
        @Bean
        public Clock clock() {
            return Clock.systemDefaultZone();
        }
    }
}
