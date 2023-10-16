package com.backbase.productled.config.restclient;

import com.backbase.mambu.clients.ApiClient;
import com.backbase.mambu.clients.api.CentresConfigurationApi;
import com.backbase.mambu.clients.api.ClientsApi;
import com.backbase.mambu.clients.api.DepositAccountsApi;
import com.backbase.mambu.clients.api.DepositTransactionsApi;
import com.backbase.mambu.clients.api.DocumentsApi;
import com.backbase.mambu.clients.api.LoanAccountsApi;
import com.backbase.mambu.clients.api.LoanTransactionsApi;
import com.backbase.productled.config.properties.MambuConfigurationProperties;
import com.backbase.productled.converter.YamlJackson2HttpMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Initializes and injects beans for 'DepositTransactionsApi' and 'DepositAccountsApi' (in Mambu) using configuration
 * properties
 */
@Configuration
public class MambuRestClientConfiguration {

    @Bean
    @Primary
    public ClientsApi clientsApi(ApiClient apiClient) {
        return new ClientsApi(apiClient);
    }

    @Bean
    @Primary
    public DepositAccountsApi getDepositAccountsApi(ApiClient apiClient) {
        return new DepositAccountsApi(apiClient);
    }

    @Bean
    @Primary
    public LoanAccountsApi getLoanAccountsApi(ApiClient apiClient) {
        return new LoanAccountsApi(apiClient);
    }

    @Bean
    @Primary
    public LoanTransactionsApi getLoanTransactionsApi(ApiClient apiClient) {
        return new LoanTransactionsApi(apiClient);
    }

    @Bean
    @Primary
    public DepositTransactionsApi getDepositTransactionsApi(ApiClient apiClient) {
        return new DepositTransactionsApi(apiClient);
    }

    @Bean
    public DocumentsApi documentsApi(ApiClient apiClient) {
        return new DocumentsApi(apiClient);
    }

    @Bean
    @Primary
    public CentresConfigurationApi getCentresConfigurationApi(ApiClient apiClient) {
        return new CentresConfigurationApi(apiClient);
    }

    @Bean
    @Primary
    public ApiClient apiClient(MambuConfigurationProperties mambuConfigurationProperties, RestTemplate restTemplate) {
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setUsername(mambuConfigurationProperties.getUsername());
        apiClient.setPassword(mambuConfigurationProperties.getPassword());
        apiClient.setBasePath(mambuConfigurationProperties.getBaseUrl());
        return apiClient;
    }

    @Bean
    @Primary
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
        restTemplate.getMessageConverters().add(new YamlJackson2HttpMessageConverter());
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        return restTemplate;
    }

}
