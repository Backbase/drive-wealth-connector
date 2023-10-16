package com.backbase.productled.config.properties;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for environment information
 */
@Data
@Component
@ConfigurationProperties(prefix = "environment")
public class EnvironmentConfigurationProperties {

    private Enrichment enrichment;
    private String installation;
    private String runtime;

    @Data
    public static class Enrichment {
        private List<String> excludeList;
    }

}
