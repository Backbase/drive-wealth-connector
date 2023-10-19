package com.backbase.modelbank.config;

import com.backbase.portfolio.integration.api.service.ApiClient;
import com.backbase.portfolio.integration.api.service.v1.InstrumentHoldingsManagementApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DbsClientApiConfig {

    @Bean
    public InstrumentHoldingsManagementApi getInstrumentHoldingsManagementApi(ApiClient apiClient) {
        return new InstrumentHoldingsManagementApi(apiClient);
    }
}
