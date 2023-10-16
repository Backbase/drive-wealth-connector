package com.backbase.productled.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "backbase.stream.identity")
@Data
public class FidoConfigurationProperties {

    private String fidoServiceUrl;
}
