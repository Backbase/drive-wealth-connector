package com.backbase.modelbank.config;

import com.backbase.drivewealth.clients.accounts.api.AccountsApi;
import com.backbase.drivewealth.clients.login.ApiClient;
import com.backbase.drivewealth.clients.login.api.LoginApi;
import com.backbase.drivewealth.clients.marketdata.api.MarketDataApi;
import com.backbase.drivewealth.clients.money.api.DepositsApi;
import com.backbase.drivewealth.clients.money.api.WithdrawalsApi;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Configuration
public class DriveWealthAPIConfig {

    private final DriveWealthConfigurationProperties driveWealthConfigurationProperties;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .defaultHeader("dw-client-app-key", driveWealthConfigurationProperties.getDwClientAppKey())
                .requestCustomizers(request -> request.getHeaders().add(HttpHeaders.AUTHORIZATION, ""))
                .build();
    }

    @Bean
    public ApiClient getLoginApiClient(RestTemplate restTemplate) {
        var apiclient = new ApiClient(restTemplate);
        apiclient.setBasePath(driveWealthConfigurationProperties.getBaseUrl());
        return apiclient;
    }

    @Bean
    public com.backbase.drivewealth.clients.accounts.ApiClient getAccountsApiClient(RestTemplate restTemplate) {
        var apiclient = new com.backbase.drivewealth.clients.accounts.ApiClient(restTemplate);
        apiclient.setBasePath(driveWealthConfigurationProperties.getBaseUrl());
        apiclient.setOffsetDateTimeFormatter(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return apiclient;
    }

    @Bean
    public com.backbase.drivewealth.clients.marketdata.ApiClient getMarketDataApiClient(RestTemplate restTemplate) {
        var apiclient = new com.backbase.drivewealth.clients.marketdata.ApiClient(restTemplate);
        apiclient.setBasePath(driveWealthConfigurationProperties.getBaseUrl());
        apiclient.setOffsetDateTimeFormatter(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss'Z'"));
        return apiclient;
    }

    @Bean
    public com.backbase.drivewealth.clients.money.ApiClient getMoneyApiClient(RestTemplate restTemplate) {
        var apiclient = new com.backbase.drivewealth.clients.money.ApiClient(restTemplate);
        apiclient.setBasePath(driveWealthConfigurationProperties.getBaseUrl());
        return apiclient;
    }


    @Bean
    public LoginApi loginApi(ApiClient apiClient) {
        return new LoginApi(apiClient);
    }

    @Bean
    public AccountsApi getAccountsApi(com.backbase.drivewealth.clients.accounts.ApiClient apiClient) {
        return new AccountsApi(apiClient);
    }

    @Bean
    public MarketDataApi getMarketDataApi(com.backbase.drivewealth.clients.marketdata.ApiClient apiClient) {
        return new MarketDataApi(apiClient);
    }

    @Bean
    public DepositsApi getDepositsApi(com.backbase.drivewealth.clients.money.ApiClient apiClient) {
        return new DepositsApi(apiClient);
    }


    @Bean
    public WithdrawalsApi getwWithdrawalsApi(com.backbase.drivewealth.clients.money.ApiClient apiClient) {
        return new WithdrawalsApi(apiClient);
    }
}
