package com.backbase.productled;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.integration.config.EnableIntegration;
import reactor.tools.agent.ReactorDebugAgent;

/**
 * Initializer class for Bootstrap Task Spring Boot Application
 */
@SpringBootApplication(scanBasePackages = {"com.backbase.productled", "com.backbase.stream", "com.backbase.mambu"})
@EnableIntegration
@ConfigurationPropertiesScan
public class BootstrapTaskApplication {

    public static void main(String[] args) {
        ReactorDebugAgent.init();
        SpringApplication springApplication = new SpringApplication(BootstrapTaskApplication.class);
        springApplication.setRegisterShutdownHook(false);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }

}
