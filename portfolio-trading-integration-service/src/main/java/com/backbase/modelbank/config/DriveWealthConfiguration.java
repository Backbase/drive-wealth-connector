package com.backbase.modelbank.config;

import com.backbase.drivewealth.clients.instrument.api.InstrumentApi;
import com.backbase.drivewealth.clients.login.ApiClient;
import com.backbase.drivewealth.clients.login.api.LoginApi;
import com.backbase.drivewealth.clients.orders.api.OrdersApi;
import java.time.format.DateTimeFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class DriveWealthConfiguration {

    public static final String DW_CLIENT_KEY = "dw-client-app-key";

    @Bean
    public LoginApi loginApi(ApiClient loginApiClient) {
        return new LoginApi(loginApiClient);
    }

    @Bean
    public OrdersApi ordersApi(com.backbase.drivewealth.clients.orders.ApiClient ordersApiClient) {
        return new OrdersApi(ordersApiClient);
    }

    @Bean
    public InstrumentApi instrumentApi(com.backbase.drivewealth.clients.instrument.ApiClient instrumentApiClient) {
        return new InstrumentApi(instrumentApiClient);
    }


    @Bean
    public ApiClient loginApiClient(DriveWealthConfigurationProperties config) {
        var apiClient = new ApiClient();
        apiClient.setBasePath(config.getBaseUrl());
        apiClient.addDefaultHeader(DW_CLIENT_KEY, config.getDwClientAppKey());
        return apiClient;
    }

    @Bean
    public com.backbase.drivewealth.clients.orders.ApiClient ordersApiClient(
        DriveWealthConfigurationProperties config) {
        var apiClient = new com.backbase.drivewealth.clients.orders.ApiClient(restTemplate());
        apiClient.setBasePath(config.getBaseUrl());
        apiClient.setOffsetDateTimeFormatter(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
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
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        return new RestTemplate(requestFactory);
    }
}
