package com.backbase.productled.config.restclient;

import com.backbase.marqeta.clients.ApiClient;
import com.backbase.marqeta.clients.api.CardProductsApi;
import com.backbase.marqeta.clients.api.CardsApi;
import com.backbase.marqeta.clients.api.UsersApi;
import com.backbase.marqeta.clients.api.VelocityControlsApi;
import com.backbase.productled.config.properties.MarqetaConfigurationProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Initializes and injects beans for 'CardProductsApi' and 'VelocityControlsApi' (in Marqeta) using configuration properties
 */
@Configuration
public class MarqetaRestClientConfiguration {

    @Bean
    public CardsApi cardsApi(@Qualifier("marqetaApiClient") ApiClient apiClient) {
        return new CardsApi(apiClient);
    }

    @Bean
    public UsersApi marqetaUsersApi(@Qualifier("marqetaApiClient") ApiClient apiClient) {
        return new UsersApi(apiClient);
    }

    @Bean
    public CardProductsApi cardProductsApi(@Qualifier("marqetaApiClient") ApiClient apiClient) {
        return new CardProductsApi(apiClient);
    }

    @Bean
    public VelocityControlsApi velocityControlsApi(@Qualifier("marqetaApiClient") ApiClient apiClient) {
        return new VelocityControlsApi(apiClient);
    }

    @Bean
    @Qualifier("marqetaApiClient")
    public ApiClient marqetaApiClient(MarqetaConfigurationProperties configurationProperties) {
        ApiClient apiClient = new ApiClient();
        apiClient.setUsername(configurationProperties.getUsername());
        apiClient.setPassword(configurationProperties.getPassword());
        apiClient.setBasePath(configurationProperties.getBaseUrl());
        apiClient.setDebugging(configurationProperties.isDebugEnabled());
        return apiClient;
    }
}
