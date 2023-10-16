package com.backbase.modelbank.config.dbs;


import com.backbase.arrangments.api.integration.ApiClient;
import com.backbase.arrangments.api.integration.v2.ArrangementsApi;
import com.backbase.buildingblocks.communication.client.ApiClientConfig;
import com.backbase.buildingblocks.communication.http.HttpCommunicationConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
@ConfigurationProperties("backbase.communication.services.arrangement-manager")
public class ArrangementsServiceIntegrationClientConfiguration extends ApiClientConfig {

    public static final String ARRANGEMENTS_SERVICE_ID = "arrangement-manager";

    public ArrangementsServiceIntegrationClientConfiguration() {
        super(ARRANGEMENTS_SERVICE_ID);
    }

    /**
     * Creates a REST client.
     *
     * @return the client.
     */
    @Bean
    public ArrangementsApi createArrangementApiClient() {
        return new ArrangementsApi(createApiClient());
    }

    private ApiClient createApiClient() {
        return new ApiClient(getRestTemplate())
                .setBasePath(createBasePath())
                .addDefaultHeader(HttpCommunicationConfiguration.INTERCEPTORS_ENABLED_HEADER, Boolean.TRUE.toString());
    }
}
