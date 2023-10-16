package com.backbase.productled.config.restclient;

import com.backbase.buildingblocks.webclient.WebClientConstants;
import com.backbase.dbs.user.manager.rest.serviceapi.spec.ApiClient;
import com.backbase.dbs.user.manager.rest.serviceapi.spec.v2.IdentityManagementApi;
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
public class UserManagerRestClientConfiguration {

    @Value("${backbase.stream.dbs.user-manager-base-url}")
    private final String userManagerBaseUrl;

    @Bean
    public IdentityManagementApi idManagementApi(ApiClient userManagerApiClient) {
        return new IdentityManagementApi(userManagerApiClient);
    }

    @Bean
    protected ApiClient userManagerApiClient(
        @Qualifier(WebClientConstants.INTER_SERVICE_WEB_CLIENT_NAME) WebClient dbsWebClient,
        ObjectMapper objectMapper,
        DateFormat dateFormat) {

        ApiClient apiClient = createApiClient(dbsWebClient, objectMapper, dateFormat);
        apiClient.setBasePath(userManagerBaseUrl);
        return apiClient;
    }

    ApiClient createApiClient(WebClient dbsWebClient, ObjectMapper objectMapper, DateFormat dateFormat) {
        return new ApiClient(dbsWebClient, objectMapper, dateFormat);
    }

}
