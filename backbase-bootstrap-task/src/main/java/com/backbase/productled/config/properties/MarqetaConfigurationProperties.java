package com.backbase.productled.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for integrating with Marqeta
 */
@Component
@ConfigurationProperties(prefix = "marqeta")
@Data
public class MarqetaConfigurationProperties {

    private boolean bootstrapResourcesEnabled = false;
    private String baseUrl;
    private String username;
    private String password;
    private boolean debugEnabled;
    private CardConfigurationProperties debitCard;
    private CardConfigurationProperties creditCard;
    private CardUsersProperties cardUsers;

}
