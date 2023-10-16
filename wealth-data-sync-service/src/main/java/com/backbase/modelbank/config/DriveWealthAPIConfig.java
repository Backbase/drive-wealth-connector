package com.backbase.modelbank.config;

import com.backbase.drivewealth.reactive.clients.accounts.api.AccountsApi;
import com.backbase.drivewealth.reactive.clients.instrument.api.InstrumentApi;
import com.backbase.drivewealth.reactive.clients.login.ApiClient;
import com.backbase.drivewealth.reactive.clients.login.api.LoginApi;
import com.backbase.drivewealth.reactive.clients.marketdata.api.MarketDataApi;
import com.backbase.drivewealth.reactive.clients.money.api.DepositsApi;
import com.backbase.drivewealth.reactive.clients.money.api.WithdrawalsApi;
import com.backbase.drivewealth.reactive.clients.orders.api.OrdersApi;
import com.backbase.drivewealth.reactive.clients.settlements.api.SettlementsApi;
import com.backbase.drivewealth.reactive.clients.transactions.api.TransactionsApi;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Configuration
public class DriveWealthAPIConfig {

    public static final String DW_CLIENT_APP_KEY = "dw-client-app-key";
    public static final String ACCOUNT_DATE_FORMAT = "yyyyMMdd";
    public static final String TRANSACTION_DATE_FORMAT = "yyyy-MM-dd";
    public static final String ISO_DATE_TIME_FORMAT = "uuuu-MM-dd'T'HH:mm:ss'Z'";
    private final DriveWealthConfigurationProperties driveWealthConfigurationProperties;

    @Bean
    public ApiClient getLoginApiClient() {
        var apiclient = new ApiClient();
        apiclient.addDefaultHeader(DW_CLIENT_APP_KEY, driveWealthConfigurationProperties.getDwClientAppKey());
        apiclient.setBasePath(driveWealthConfigurationProperties.getBaseUrl());
        return apiclient;
    }

    @Bean
    public com.backbase.drivewealth.reactive.clients.accounts.ApiClient getAccountsApiClient() {
        var apiclient = new com.backbase.drivewealth.reactive.clients.accounts.ApiClient();
        apiclient.addDefaultHeader(DW_CLIENT_APP_KEY, driveWealthConfigurationProperties.getDwClientAppKey());
        apiclient.setBasePath(driveWealthConfigurationProperties.getBaseUrl());
        apiclient.setOffsetDateTimeFormatter(DateTimeFormatter.ofPattern(ACCOUNT_DATE_FORMAT));
        return apiclient;
    }

    @Bean
    public com.backbase.drivewealth.reactive.clients.marketdata.ApiClient getMarketDataApiClient() {
        var apiclient = new com.backbase.drivewealth.reactive.clients.marketdata.ApiClient();
        apiclient.addDefaultHeader(DW_CLIENT_APP_KEY, driveWealthConfigurationProperties.getDwClientAppKey());
        apiclient.setBasePath(driveWealthConfigurationProperties.getBaseUrl());
        apiclient.setOffsetDateTimeFormatter(DateTimeFormatter.ofPattern(ISO_DATE_TIME_FORMAT));
        return apiclient;
    }

    @Bean
    public com.backbase.drivewealth.reactive.clients.money.ApiClient getMoneyApiClient() {
        var apiclient = new com.backbase.drivewealth.reactive.clients.money.ApiClient();
        apiclient.addDefaultHeader(DW_CLIENT_APP_KEY, driveWealthConfigurationProperties.getDwClientAppKey());
        apiclient.setBasePath(driveWealthConfigurationProperties.getBaseUrl());
        return apiclient;
    }

    @Bean
    public com.backbase.drivewealth.reactive.clients.transactions.ApiClient getTransactionsApiClient() {
        var apiclient = new com.backbase.drivewealth.reactive.clients.transactions.ApiClient();
        apiclient.addDefaultHeader(DW_CLIENT_APP_KEY, driveWealthConfigurationProperties.getDwClientAppKey());
        apiclient.setOffsetDateTimeFormatter(DateTimeFormatter.ofPattern(TRANSACTION_DATE_FORMAT));
        apiclient.setBasePath(driveWealthConfigurationProperties.getBaseUrl());
        return apiclient;
    }

    @Bean
    public com.backbase.drivewealth.reactive.clients.instrument.ApiClient getInstrumentApiClient() {
        var apiclient = new com.backbase.drivewealth.reactive.clients.instrument.ApiClient();
        apiclient.addDefaultHeader(DW_CLIENT_APP_KEY, driveWealthConfigurationProperties.getDwClientAppKey());
        apiclient.setBasePath(driveWealthConfigurationProperties.getBaseUrl());
        return apiclient;
    }

    @Bean
    public com.backbase.drivewealth.reactive.clients.orders.ApiClient getOrdersApiClient() {
        var apiclient = new com.backbase.drivewealth.reactive.clients.orders.ApiClient();
        apiclient.setOffsetDateTimeFormatter(DateTimeFormatter.ofPattern(TRANSACTION_DATE_FORMAT));
        apiclient.addDefaultHeader(DW_CLIENT_APP_KEY, driveWealthConfigurationProperties.getDwClientAppKey());
        apiclient.setBasePath(driveWealthConfigurationProperties.getBaseUrl());
        return apiclient;
    }

    @Bean
    public com.backbase.drivewealth.reactive.clients.settlements.ApiClient getSettlementsApiClient() {
        var apiclient = new com.backbase.drivewealth.reactive.clients.settlements.ApiClient();
        apiclient.addDefaultHeader(DW_CLIENT_APP_KEY, driveWealthConfigurationProperties.getDwClientAppKey());
        apiclient.setBasePath(driveWealthConfigurationProperties.getBaseUrl());
        apiclient.setOffsetDateTimeFormatter(DateTimeFormatter.ofPattern(ISO_DATE_TIME_FORMAT));
        return apiclient;
    }

    @Bean
    public LoginApi loginApi(ApiClient apiClient) {
        return new LoginApi(apiClient);
    }

    @Bean
    public AccountsApi getAccountsApi(com.backbase.drivewealth.reactive.clients.accounts.ApiClient apiClient) {
        return new AccountsApi(apiClient);
    }

    @Bean
    public MarketDataApi getMarketDataApi(com.backbase.drivewealth.reactive.clients.marketdata.ApiClient apiClient) {
        return new MarketDataApi(apiClient);
    }

    @Bean
    public DepositsApi getDepositsApi(com.backbase.drivewealth.reactive.clients.money.ApiClient apiClient) {
        return new DepositsApi(apiClient);
    }


    @Bean
    public WithdrawalsApi getWithdrawalsApi(com.backbase.drivewealth.reactive.clients.money.ApiClient apiClient) {
        return new WithdrawalsApi(apiClient);
    }

    @Bean
    public TransactionsApi getTransactionsApi(com.backbase.drivewealth.reactive.clients.transactions.ApiClient apiClient) {
        return new TransactionsApi(apiClient);
    }

    @Bean
    public InstrumentApi getInstrumentApi(com.backbase.drivewealth.reactive.clients.instrument.ApiClient apiClient) {
        return new InstrumentApi(apiClient);
    }

    @Bean
    public OrdersApi getOrdersApi(com.backbase.drivewealth.reactive.clients.orders.ApiClient apiClient) {
        return new OrdersApi(apiClient);
    }

    @Bean
    public SettlementsApi getSettlementsApi(com.backbase.drivewealth.reactive.clients.settlements.ApiClient apiClient) {
        return new SettlementsApi(apiClient);
    }
}