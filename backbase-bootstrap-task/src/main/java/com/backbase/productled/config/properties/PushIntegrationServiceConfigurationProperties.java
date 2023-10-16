package com.backbase.productled.config.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @copyright (C) 2022, Backbase
 * @Version 1.0
 * @Since 01. Nov 2022 3:36 pm
 */
@Getter
@Configuration
@RequiredArgsConstructor
public class PushIntegrationServiceConfigurationProperties {

    @Value("${backbase.stream.mobile.push-integration-service-url}")
    private final String pushIntegrationUrl;

}
