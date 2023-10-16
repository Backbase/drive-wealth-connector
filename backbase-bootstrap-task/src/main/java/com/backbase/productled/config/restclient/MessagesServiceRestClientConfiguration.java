package com.backbase.productled.config.restclient;

import com.backbase.buildingblocks.webclient.WebClientConstants;
import com.backbase.dbs.message.rest.serviceapi.spec.ApiClient;
import com.backbase.dbs.message.rest.serviceapi.spec.v2.ServiceEmployeeApi;
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
public class MessagesServiceRestClientConfiguration {

    @Value("${backbase.stream.dbs.messages-service-base-url}")
    private final String messagesServiceBaseUrl;

    @Bean
    public ServiceEmployeeApi serviceEmployeeApi(ApiClient messageApiClient) {
        return new ServiceEmployeeApi(messageApiClient);
    }

    @Bean
    protected ApiClient messageApiClient(
        @Qualifier(WebClientConstants.INTER_SERVICE_WEB_CLIENT_NAME) WebClient dbsWebClient,
        ObjectMapper objectMapper,
        DateFormat dateFormat) {

        ApiClient apiClient = createApiClient(dbsWebClient, objectMapper, dateFormat);
        apiClient.setBasePath(messagesServiceBaseUrl);
        return apiClient;
    }

    ApiClient createApiClient(WebClient dbsWebClient, ObjectMapper objectMapper, DateFormat dateFormat) {
        return new ApiClient(dbsWebClient, objectMapper, dateFormat);
    }

}
