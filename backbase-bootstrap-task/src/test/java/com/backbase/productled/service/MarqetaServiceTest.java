package com.backbase.productled.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.backbase.marqeta.clients.api.CardProductsApi;
import com.backbase.marqeta.clients.api.CardsApi;
import com.backbase.marqeta.clients.api.UsersApi;
import com.backbase.marqeta.clients.api.VelocityControlsApi;
import com.backbase.marqeta.clients.model.CardListResponse;
import com.backbase.marqeta.clients.model.CardProductResponse;
import com.backbase.marqeta.clients.model.CardResponse;
import com.backbase.marqeta.clients.model.CardResponse.StateEnum;
import com.backbase.marqeta.clients.model.UserCardHolderResponse;
import com.backbase.productled.config.properties.EnvironmentConfigurationProperties;
import com.backbase.productled.config.properties.MarqetaConfigurationProperties;
import com.backbase.stream.LegalEntityTask;
import com.backbase.stream.legalentity.model.LegalEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@RunWith(MockitoJUnitRunner.class)
public class MarqetaServiceTest {

    @Mock
    private UsersApi usersApi;

    @Mock
    private CardsApi cardsApi;

    @Mock
    private VelocityControlsApi velocityControlsApi;

    @Mock
    private CardProductsApi cardProductsApi;

    private LegalEntityTask legalEntityTask;

    private MarqetaService marqetaService;

    @Before
    public void setUp() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();
        factoryBean.setResources(new ClassPathResource("application.yml"));

        Properties properties = factoryBean.getObject();

        ConfigurationPropertySource propertySource = new MapConfigurationPropertySource(properties);
        Binder binder = new Binder(propertySource);

        MarqetaConfigurationProperties configurationProperties = binder
            .bind("marqeta", MarqetaConfigurationProperties.class).get();

        EnvironmentConfigurationProperties environmentConfigurationProperties = new EnvironmentConfigurationProperties();
        environmentConfigurationProperties.setInstallation("ins");
        environmentConfigurationProperties.setRuntime("rnt");

        marqetaService = new MarqetaService(usersApi, cardsApi, velocityControlsApi, cardProductsApi,
            configurationProperties, environmentConfigurationProperties);
        configurationProperties.setBootstrapResourcesEnabled(true);

        legalEntityTask = new LegalEntityTask(
            objectMapper.readValue(new File("src/main/resources/le-hierarchy/le-base-structure.json"),
                LegalEntity.class));
    }

    @Test
    public void testExecuteTaskWhenDataDoesntExist() {

        // Given
        when(usersApi.getUsersToken(any(), any())).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        when(cardsApi.getCardsUserToken(any(), any(), any(), any(), any())).thenReturn(new CardListResponse().data(
            Collections.singletonList(new CardResponse())));
        when(cardProductsApi.postCardproducts(any())).thenReturn(new CardProductResponse().token("token"));

        // When
        marqetaService.executeTask(legalEntityTask);

        // Then
        Mockito.verify(cardProductsApi, times(5)).postCardproducts(any());
        Mockito.verify(velocityControlsApi, times(10)).postVelocitycontrols(any());
        Mockito.verify(cardsApi, times(5)).postCards(any(), any(), any());
    }

    @Test
    public void testExecuteTaskWhenDataAlreadyExist() {

        // Given
        when(usersApi.getUsersToken("matthew", null)).thenReturn(new UserCardHolderResponse().token("matthew"));
        when(usersApi.getUsersToken("paolo", null)).thenReturn(new UserCardHolderResponse().token("paolo"));
        when(usersApi.getUsersToken("will", null)).thenReturn(new UserCardHolderResponse().token("paolo"));
        when(usersApi.getUsersToken("sara", null)).thenReturn(new UserCardHolderResponse().token("sara"));
        when(usersApi.getUsersToken("john", null)).thenReturn(new UserCardHolderResponse().token("john"));

        when(cardsApi.getCardsUserToken("matthew", 100, 0, "token,state", null))
            .thenReturn(new CardListResponse().addDataItem(new CardResponse().userToken("matthew").token("card-1").state(
                StateEnum.UNACTIVATED)));
        when(cardsApi.getCardsUserToken("will", 100, 0, "token,state", null))
            .thenReturn(new CardListResponse().addDataItem(new CardResponse().userToken("paolo").token("card-2").state(
                StateEnum.UNACTIVATED)));
        when(cardsApi.getCardsUserToken("paolo", 100, 0, "token,state", null))
            .thenReturn(new CardListResponse().addDataItem(new CardResponse().userToken("paolo").token("card-2").state(
                StateEnum.UNACTIVATED)));
        when(cardsApi.getCardsUserToken("sara", 100, 0, "token,state", null))
            .thenReturn(new CardListResponse().addDataItem(new CardResponse().userToken("paolo").token("card-3").state(
                StateEnum.UNACTIVATED)));
        when(cardsApi.getCardsUserToken("john", 100, 0, "token,state", null))
            .thenReturn(new CardListResponse().addDataItem(new CardResponse().userToken("paolo").token("card-4").state(
                StateEnum.UNACTIVATED)));

        // When
        marqetaService.executeTask(legalEntityTask);

        // Then
        Mockito.verify(cardProductsApi, never()).postCardproducts(any());
        Mockito.verify(velocityControlsApi, never()).postVelocitycontrols(any());
        Mockito.verify(cardsApi, never()).postCards(any(), any(), any());
    }
}
