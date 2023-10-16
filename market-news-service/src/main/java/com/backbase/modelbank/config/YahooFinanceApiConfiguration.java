package com.backbase.modelbank.config;

import com.backbase.yahoofinance.news.clients.ApiClient;
import com.backbase.yahoofinance.news.clients.api.YahooMarketNewsApi;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Initializes and injects beans for YahooMarketNewsApi's API using configuration properties.
 */
@Configuration
@RequiredArgsConstructor
public class YahooFinanceApiConfiguration {

    private final YahooFinanceConfigurationProperties configurationProperties;

    @Bean
    public YahooMarketNewsApi clientApi(ApiClient apiClient) {
        return new YahooMarketNewsApi(apiClient);
    }

    @Bean
    public ApiClient apiClient() {
        var apiClient = new ApiClient();
        apiClient.setBasePath(configurationProperties.getBaseUrl());
        return apiClient;
    }
}
