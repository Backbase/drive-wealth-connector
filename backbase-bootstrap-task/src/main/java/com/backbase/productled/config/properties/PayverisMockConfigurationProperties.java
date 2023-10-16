package com.backbase.productled.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for integrating with Payveris Mock API
 */
@Component
@ConfigurationProperties(prefix = "payveris")
@Data
public class PayverisMockConfigurationProperties {

    private String payverisBaseUrl;
    private Boolean bootstrapPayverisMockEnabled;
}
