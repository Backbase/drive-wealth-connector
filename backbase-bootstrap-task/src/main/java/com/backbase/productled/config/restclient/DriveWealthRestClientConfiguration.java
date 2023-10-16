package com.backbase.productled.config.restclient;


import static com.backbase.productled.utils.DriveWealthConstants.DW_CLIENT_APP_KEY;

import com.backbase.drivewealth.clients.accounts.api.AccountsApi;
import com.backbase.drivewealth.clients.deposits.api.DepositsApi;
import com.backbase.drivewealth.clients.instrument.api.InstrumentApi;
import com.backbase.drivewealth.clients.login.ApiClient;
import com.backbase.drivewealth.clients.login.api.LoginApi;
import com.backbase.drivewealth.clients.marketdata.api.MarketDataApi;
import com.backbase.drivewealth.clients.orders.api.OrdersApi;
import com.backbase.drivewealth.clients.users.api.UsersApi;
import com.backbase.productled.config.properties.DriveWealthConfigurationProperties;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DriveWealthRestClientConfiguration {

    private final DriveWealthConfigurationProperties driveWealthConfigurationProperties;

    @Bean
    public LoginApi loginApi(ApiClient loginApiClient) {
        return new LoginApi(loginApiClient);
    }

    @Bean
    public MarketDataApi marketDataApi(com.backbase.drivewealth.clients.marketdata.ApiClient marketDataApiClient) {
        return new MarketDataApi(marketDataApiClient);
    }

    @Bean("dwUsersApi")
    public UsersApi usersApi(com.backbase.drivewealth.clients.users.ApiClient usersApiClient) {
        return new UsersApi(usersApiClient);
    }

    @Bean
    public InstrumentApi instrumentApi(com.backbase.drivewealth.clients.instrument.ApiClient instrumentsApiClient) {
        return new InstrumentApi(instrumentsApiClient);
    }

    @Bean
    public DepositsApi depositsApi(com.backbase.drivewealth.clients.deposits.ApiClient depositsApiClient) {
        return new DepositsApi(depositsApiClient);
    }

    @Bean
    public AccountsApi accountsApi(com.backbase.drivewealth.clients.accounts.ApiClient accountsApiClient) {
        return new AccountsApi(accountsApiClient);
    }

    @Bean
    public OrdersApi ordersApi(com.backbase.drivewealth.clients.orders.ApiClient ordersApiClient) {
        return new OrdersApi(ordersApiClient);
    }

    @Bean
    public ApiClient loginApiClient() {
        var apiClient = new ApiClient();
        apiClient.setBasePath(driveWealthConfigurationProperties.baseUrl());
        apiClient.addDefaultHeader(DW_CLIENT_APP_KEY, driveWealthConfigurationProperties.dwClientAppKey());
        return apiClient;
    }

    @Bean
    public com.backbase.drivewealth.clients.marketdata.ApiClient marketDataApiClient() {
        var apiClient = new com.backbase.drivewealth.clients.marketdata.ApiClient();
        apiClient.setBasePath(driveWealthConfigurationProperties.baseUrl());
        apiClient.addDefaultHeader(DW_CLIENT_APP_KEY, driveWealthConfigurationProperties.dwClientAppKey());
        apiClient.setOffsetDateTimeFormatter(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return apiClient;
    }

    @Bean
    public com.backbase.drivewealth.clients.users.ApiClient usersApiClient() {
        var apiClient = new com.backbase.drivewealth.clients.users.ApiClient();
        apiClient.setBasePath(driveWealthConfigurationProperties.baseUrl());
        apiClient.addDefaultHeader(DW_CLIENT_APP_KEY, driveWealthConfigurationProperties.dwClientAppKey());
        return apiClient;
    }

    @Bean("instrumentsApiClient")
    public com.backbase.drivewealth.clients.instrument.ApiClient instrumentsApiClient(
        DriveWealthConfigurationProperties config) {
        var apiClient = new com.backbase.drivewealth.clients.instrument.ApiClient();
        apiClient.setBasePath(config.baseUrl());
        apiClient.setOffsetDateTimeFormatter(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        apiClient.addDefaultHeader(DW_CLIENT_APP_KEY, config.dwClientAppKey());
        return apiClient;
    }

    @Bean
    public com.backbase.drivewealth.clients.deposits.ApiClient depositsApiClient() {
        var apiClient = new com.backbase.drivewealth.clients.deposits.ApiClient();
        apiClient.setBasePath(driveWealthConfigurationProperties.baseUrl());
        apiClient.addDefaultHeader(DW_CLIENT_APP_KEY, driveWealthConfigurationProperties.dwClientAppKey());
        return apiClient;
    }

    @Bean
    public com.backbase.drivewealth.clients.accounts.ApiClient accountsApiClient() {
        var apiClient = new com.backbase.drivewealth.clients.accounts.ApiClient();
        apiClient.setBasePath(driveWealthConfigurationProperties.baseUrl());
        apiClient.addDefaultHeader(DW_CLIENT_APP_KEY, driveWealthConfigurationProperties.dwClientAppKey());
        return apiClient;
    }

    @Bean
    public com.backbase.drivewealth.clients.orders.ApiClient ordersApiClient() {
        var apiClient = new com.backbase.drivewealth.clients.orders.ApiClient();
        apiClient.setBasePath(driveWealthConfigurationProperties.baseUrl());
        apiClient.addDefaultHeader(DW_CLIENT_APP_KEY, driveWealthConfigurationProperties.dwClientAppKey());
        return apiClient;
    }

}