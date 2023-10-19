package com.backbase.productled.config.restclient;

import com.backbase.buildingblocks.webclient.WebClientConstants;
import com.backbase.productled.config.properties.PushIntegrationServiceConfigurationProperties;
import com.backbase.services.mobile.push.rest.clientapi.spec.ApiClient;
import com.backbase.services.mobile.push.rest.clientapi.spec.v2.ClientPushApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.text.DateFormat;

/**
 * @copyright (C) 2022, Backbase
 * @Version 1.0
 * @Since 03. Nov 2022 3:17 pm
 */
@Configuration
@RequiredArgsConstructor
public class PushIntegrationRestClientConfig {

    private final PushIntegrationServiceConfigurationProperties properties;

    @Bean
    public ApiClient pushIntegrationApicApiClient() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(properties.getPushIntegrationUrl());
        return apiClient;
    }

    @Bean
    public ClientPushApi servicePushApi(ApiClient apiclient) {
        return new ClientPushApi(apiclient);
    }
}
