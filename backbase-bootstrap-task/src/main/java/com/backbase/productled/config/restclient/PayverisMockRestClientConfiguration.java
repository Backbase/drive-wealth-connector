package com.backbase.productled.config.restclient;

import com.backbase.payveris.mock.clients.ApiClient;
import com.backbase.payveris.mock.clients.api.MockApi;
import com.backbase.productled.config.properties.PayverisMockConfigurationProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.DateFormat;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Initializes and injects beans for 'Payveris Mock Api' using configuration properties
 */
@Configuration
public class PayverisMockRestClientConfiguration {

    @Bean
    public MockApi mockApi(@Qualifier("payverisMockClient") ApiClient apiClient) {
        return new MockApi(apiClient);
    }

    @Bean
    @Qualifier("payverisMockClient")
    public ApiClient mockClient(PayverisMockConfigurationProperties configurationProperties,
                                @Qualifier("interServiceWebClient") WebClient dbsWebClient, ObjectMapper objectMapper, DateFormat dateFormat) {
        ApiClient apiClient = new ApiClient(dbsWebClient, objectMapper, dateFormat);
        apiClient.setBasePath(configurationProperties.getPayverisBaseUrl());
        return apiClient;
    }
}
