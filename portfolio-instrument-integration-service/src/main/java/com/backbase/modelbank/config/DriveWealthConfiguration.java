package com.backbase.modelbank.config;

import static com.backbase.modelbank.util.InstrumentConstant.DW_CLIENT_KEY;
import static com.backbase.modelbank.util.InstrumentConstant.DW_USER_KEY;

import com.backbase.drivewealth.clients.instrument.api.InstrumentApi;
import com.backbase.drivewealth.clients.login.ApiClient;
import com.backbase.drivewealth.clients.login.api.LoginApi;
import com.backbase.drivewealth.clients.marketdata.api.MarketDataApi;
import java.time.format.DateTimeFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DriveWealthConfiguration {

    @Bean
    public LoginApi loginApi(ApiClient loginApiClient) {
        return new LoginApi(loginApiClient);
    }

    @Bean
    public InstrumentApi instrumentApi(com.backbase.drivewealth.clients.instrument.ApiClient instrumentApiClient) {
        return new InstrumentApi(instrumentApiClient);
    }

    @Bean
    public MarketDataApi marketDataApi(com.backbase.drivewealth.clients.marketdata.ApiClient marketDataApiClient) {
        return new MarketDataApi(marketDataApiClient);
    }

    @Bean
    public ApiClient loginApiClient(DriveWealthConfigurationProperties config) {
        var apiClient = new ApiClient();
        apiClient.setBasePath(config.getBaseUrl());
        apiClient.addDefaultHeader(DW_CLIENT_KEY, config.getDwClientAppKey());
        return apiClient;
    }

    @Bean
    public com.backbase.drivewealth.clients.instrument.ApiClient instrumentApiClient(
        DriveWealthConfigurationProperties config) {
        var apiClient = new com.backbase.drivewealth.clients.instrument.ApiClient();
        apiClient.setBasePath(config.getBaseUrl());
        apiClient.setOffsetDateTimeFormatter(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        apiClient.addDefaultHeader(DW_CLIENT_KEY, config.getDwClientAppKey());
        return apiClient;
    }

    @Bean
    public com.backbase.drivewealth.clients.marketdata.ApiClient marketDataApiClient(
        DriveWealthConfigurationProperties config) {
        var apiClient = new com.backbase.drivewealth.clients.marketdata.ApiClient();
        apiClient.setBasePath(config.getBaseUrl());
        apiClient.setOffsetDateTimeFormatter(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        apiClient.addDefaultHeader(DW_CLIENT_KEY, config.getDwClientAppKey());
        apiClient.addDefaultHeader(DW_USER_KEY, config.getUserId());
        return apiClient;
    }
}
