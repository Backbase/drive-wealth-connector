package com.backbase.productled.config.restclient;

import com.backbase.buildingblocks.webclient.WebClientConstants;
import com.backbase.identity.fido.service.api.ApiClient;
import com.backbase.identity.fido.service.api.v2.ApplicationControllerApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.DateFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class FidoRestClientConfiguration {

    @Value("${backbase.stream.identity.fido-service-url}")
    private final String fidoServiceUrl;

    @Bean
    @Qualifier("applicationControllerApi")
    public ApplicationControllerApi applicationControllerApi(ApiClient fidoServiceApiClient) {
        return new ApplicationControllerApi(fidoServiceApiClient);
    }

    @Bean
    protected com.backbase.identity.fido.service.api.ApiClient fidoServiceApiClient(
        @Qualifier(WebClientConstants.INTER_SERVICE_WEB_CLIENT_NAME) WebClient dbsWebClient,
        ObjectMapper objectMapper,
        DateFormat dateFormat) {

        com.backbase.identity.fido.service.api.ApiClient apiClient = createApiClient(dbsWebClient, objectMapper, dateFormat);
        apiClient.setBasePath(fidoServiceUrl);
        return apiClient;
    }

    com.backbase.identity.fido.service.api.ApiClient createApiClient(WebClient dbsWebClient, ObjectMapper objectMapper, DateFormat dateFormat) {
        return new com.backbase.identity.fido.service.api.ApiClient(dbsWebClient, objectMapper, dateFormat);
    }

}
