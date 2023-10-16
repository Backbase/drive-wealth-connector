package com.backbase.productled.config.restclient;

import com.backbase.dbs.accesscontrol.api.service.ApiClient;
import com.backbase.dbs.accesscontrol.api.service.v3.PermissionSetApi;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @copyright (C) 2022, Backbase
 * @Version 1.0
 * @Since 19. Oct 2022 12:05 pm
 */
@RequiredArgsConstructor
@Configuration
public class PermissionSetApiClientConfiguration {

    @Bean
    public PermissionSetApi getPermissionSetApi (ApiClient apiClient) {
        return new PermissionSetApi(apiClient);
    }
}
