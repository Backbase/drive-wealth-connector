package com.backbase.modelbank;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.aws.autoconfigure.context.ContextInstanceDataAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.context.ContextRegionProviderAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.context.ContextStackAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.backbase.modelbank", "com.backbase.stream"},
    exclude = {ContextInstanceDataAutoConfiguration.class, ContextStackAutoConfiguration.class,
        ContextRegionProviderAutoConfiguration.class
    })
@EnableScheduling
@Slf4j
public class WealthDataSyncApplication extends SpringBootServletInitializer {

    public static void main(final String[] args) {
        SpringApplication.run(WealthDataSyncApplication.class, args);
    }


}