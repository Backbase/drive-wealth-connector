package com.backbase.productled.config.restclient;

import com.backbase.dbs.accesscontrol.client.api.ApiClient;
import com.backbase.dbs.accesscontrol.client.api.v2.UserContextApi;
import com.backbase.productled.config.properties.GlobalLimitsConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccessControlRestClientConfiguration {

    @Bean("userContextApiV2")
    public UserContextApi userContextApi(@Qualifier("accessControlClient") ApiClient apiClient) {
        return new UserContextApi(apiClient);
    }

    @Bean
    @Qualifier("accessControlClient")
    public ApiClient accessControlClient(GlobalLimitsConfiguration properties) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(properties.getAccessControlUrl());
        return apiClient;
    }
}
