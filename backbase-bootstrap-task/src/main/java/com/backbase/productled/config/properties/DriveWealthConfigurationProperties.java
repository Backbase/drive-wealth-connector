package com.backbase.productled.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "drive-wealth")
public record DriveWealthConfigurationProperties(boolean bootstrapResourcesEnabled, String baseUrl,
                                                 String dwClientAppKey, String clientID, String clientSecret) {

}
