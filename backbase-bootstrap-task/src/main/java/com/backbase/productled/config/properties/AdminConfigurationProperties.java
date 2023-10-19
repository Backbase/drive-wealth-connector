package com.backbase.productled.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for integrating with Admin(Keycloak)
 */
@Component
@ConfigurationProperties(prefix = "admin")
@Data
public class AdminConfigurationProperties {

    private String baseUrl;
    private String username;
    private String password;
    private Boolean bootstrapManualStepsEnabled;
    private String defaultUserPassword;
    private String remoteConfigUsersLocation;

}
