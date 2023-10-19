package com.backbase.modelbank;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.backbase.modelbank"})
@EnableConfigurationProperties
@EnableScheduling
@EnableCaching
public class MarketNewsApplication extends SpringBootServletInitializer {

    public static void main(final String[] args) {
        SpringApplication.run(MarketNewsApplication.class, args);
    }

}