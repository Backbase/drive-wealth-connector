package com.backbase.productled.config.restclient;

import com.backbase.admin.clients.ApiClient;
import com.backbase.admin.clients.api.AdminApi;
import com.backbase.productled.config.properties.AdminConfigurationProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Initializes and injects beans for 'AdminApi' using configuration properties
 */
@Configuration
public class AdminRestClientConfiguration {

    @Bean
    public AdminApi adminApi(@Qualifier("adminClient") ApiClient apiClient) {
        return new AdminApi(apiClient);
    }

    @Bean
    @Qualifier("adminClient")
    public ApiClient adminClient(AdminConfigurationProperties configurationProperties) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(configurationProperties.getBaseUrl());
        return apiClient;
    }
}
