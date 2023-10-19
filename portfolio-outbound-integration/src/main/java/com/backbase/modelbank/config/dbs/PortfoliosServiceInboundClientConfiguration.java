package com.backbase.modelbank.config.dbs;


import com.backbase.buildingblocks.communication.client.ApiClientConfig;
import com.backbase.buildingblocks.communication.http.HttpCommunicationConfiguration;
import com.backbase.portfolio.api.integration.ApiClient;
import com.backbase.portfolio.api.integration.v2.PortfolioManagementApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("backbase.communication.services.portfolio")
public class PortfoliosServiceInboundClientConfiguration extends ApiClientConfig {

    public static final String PORTFOLIO_SERVICE_ID = "portfolio";

    public PortfoliosServiceInboundClientConfiguration() {
        super(PORTFOLIO_SERVICE_ID);
    }

    /**
     * Creates a REST client.
     *
     * @return the client.
     */
    @Bean
    public PortfolioManagementApi createPortfolioManagementApiClient() {
        return new PortfolioManagementApi(createApiClient());
    }

    private ApiClient createApiClient() {
        return new ApiClient(getRestTemplate())
                .setBasePath(createBasePath())
                .addDefaultHeader(HttpCommunicationConfiguration.INTERCEPTORS_ENABLED_HEADER, Boolean.TRUE.toString());
    }
}
