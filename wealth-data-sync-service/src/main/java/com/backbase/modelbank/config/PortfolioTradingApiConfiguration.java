package com.backbase.modelbank.config;


import com.backbase.portfolio.instrument.integration.api.service.v1.MarketManagementApi;
import com.backbase.portfolio.integration.api.service.v1.PortfolioTradingAccountsManagementApi;
import com.backbase.portfolio.trading.integration.api.service.ApiClient;
import com.backbase.portfolio.trading.integration.api.service.v1.ExternalTradeOrderApi;
import com.backbase.stream.clients.config.PortfolioApiConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.DateFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PortfolioTradingApiConfiguration extends PortfolioApiConfiguration {

    @Bean
    public ApiClient externalTradeOrderApiClient(ObjectMapper objectMapper, DateFormat dateFormat) {
        return new ApiClient(getWebClient(), objectMapper, dateFormat).setBasePath(createBasePath());
    }

    @Bean
    public com.backbase.portfolio.instrument.integration.api.service.ApiClient marketManagementApiClient(
        ObjectMapper objectMapper, DateFormat dateFormat) {
        return new com.backbase.portfolio.instrument.integration.api.service.ApiClient(getWebClient(), objectMapper,
            dateFormat).setBasePath(createBasePath());
    }

    @Bean
    public ExternalTradeOrderApi externalTradeOrderApi(ApiClient apiClient) {
        return new ExternalTradeOrderApi(apiClient);
    }

    @Bean
    public com.backbase.portfolio.integration.api.service.ApiClient portfolioTradingAccountsManagementApiClient(
        ObjectMapper objectMapper, DateFormat dateFormat) {
        return new com.backbase.portfolio.integration.api.service.ApiClient(getWebClient(), objectMapper,
            dateFormat).setBasePath(createBasePath());
    }

    @Bean
    public PortfolioTradingAccountsManagementApi portfolioTradingAccountsManagementApi(
        com.backbase.portfolio.integration.api.service.ApiClient portfolioTradingAccountsManagementApiClient) {
        return new PortfolioTradingAccountsManagementApi(portfolioTradingAccountsManagementApiClient);
    }

    @Bean
    public MarketManagementApi marketManagementApi(
        com.backbase.portfolio.instrument.integration.api.service.ApiClient marketManagementApiClient) {
        return new MarketManagementApi(marketManagementApiClient);
    }

}
