package com.backbase.modelbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EnableRetry
public class PortfolioOutboundIntegrationApplication extends SpringBootServletInitializer {

    public static void main(final String[] args) {
        SpringApplication.run(PortfolioOutboundIntegrationApplication.class, args);
    }

}
