package com.backbase.productled.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "global-limits")
@Data
public class GlobalLimitsConfiguration {

    private Boolean bootstrapResourcesEnabled = false;
    private String accessControlUrl;
    private String limitsUrl;

}
