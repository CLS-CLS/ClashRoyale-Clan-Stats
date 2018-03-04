package org.lytsiware.clash;

import java.util.Arrays;

import org.lytsiware.clash.service.interceptor.BaseUrlInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
//@EnableCaching
@EnableScheduling
@EnableRetry
public class Application {
	

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        System.out.println("ACTIVE PROFILES : " + Arrays.toString(context.getEnvironment().getActiveProfiles()));
    }
    
    @Configuration
    static class MvcConfiguration extends WebMvcConfigurerAdapter {
    	
    	@Autowired
    	BaseUrlInterceptor baseUrlInterceptor;
    	
    	@Override
    	public void addInterceptors(InterceptorRegistry registry) {
//    	    registry.addInterceptor(baseUrlInterceptor);
    	}
    }
}
