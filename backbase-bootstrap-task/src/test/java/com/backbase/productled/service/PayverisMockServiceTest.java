package com.backbase.productled.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.payveris.mock.clients.api.MockApi;
import com.backbase.productled.config.properties.PayverisMockConfigurationProperties;
import com.backbase.stream.LegalEntityTask;
import com.backbase.stream.legalentity.model.LegalEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.io.ClassPathResource;
import reactor.core.publisher.Mono;

@RunWith(MockitoJUnitRunner.class)
public class PayverisMockServiceTest {

    @Mock
    private MockApi mockApi;

    private LegalEntityTask legalEntityTask;

    private PayverisMockService payverisMockService;

    @Before
    public void setUp() throws IOException {
        var objectMapper = new ObjectMapper();
        var factoryBean = new YamlPropertiesFactoryBean();
        factoryBean.setResources(new ClassPathResource("application.yml"));

        var properties = factoryBean.getObject();

        var propertySource = new MapConfigurationPropertySource(properties);
        var binder = new Binder(propertySource);

        var configurationProperties = binder
            .bind("payveris", PayverisMockConfigurationProperties.class).get();
        configurationProperties.setBootstrapPayverisMockEnabled(true);

        payverisMockService = new PayverisMockService(mockApi, configurationProperties);

        legalEntityTask = new LegalEntityTask(
            objectMapper.readValue(new File("src/main/resources/le-hierarchy/le-base-structure.json"),
                LegalEntity.class));
    }

    @Test
    public void testExecuteTaskShouldCreateUsers() {
        // Given
        var monoVoid = Mono.just("Success").then();
        when(mockApi.addProfile(any(), any())).thenReturn(monoVoid);
        when(mockApi.resetData()).thenReturn(monoVoid);

        // When
        payverisMockService.executeTask(legalEntityTask).block();

        // Then
        verify(mockApi, times(1)).resetData();
        verify(mockApi, times(7)).addProfile(any(), any());
    }
}
