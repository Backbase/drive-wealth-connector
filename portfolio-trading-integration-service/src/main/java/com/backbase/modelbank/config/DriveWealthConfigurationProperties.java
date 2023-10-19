package com.backbase.modelbank.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "drive-wealth")
@Data
public class DriveWealthConfigurationProperties {

    private String baseUrl;
    private String dwClientAppKey;
    private String clientID;
    private String clientSecret;

}
