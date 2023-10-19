package com.backbase.productled.config.restclient;

import com.backbase.dbs.limit.api.client.ApiClient;
import com.backbase.dbs.limit.api.client.v2.LimitsApi;
import com.backbase.productled.config.properties.GlobalLimitsConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LimitsRestClientConfiguration {

    @Bean
    @Qualifier("limitsClientApi")
    public LimitsApi limitsClientApi(@Qualifier("limitsClient") ApiClient apiClient) {
        return new LimitsApi(apiClient);
    }

    @Bean
    @Qualifier("limitsClient")
    public ApiClient limitsClient(GlobalLimitsConfiguration properties) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(properties.getLimitsUrl());
        return apiClient;
    }

}
