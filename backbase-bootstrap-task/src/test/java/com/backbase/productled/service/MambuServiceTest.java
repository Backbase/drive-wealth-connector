package com.backbase.productled.service;

import com.backbase.mambu.clients.api.CentresConfigurationApi;
import com.backbase.mambu.clients.api.ClientsApi;
import com.backbase.mambu.clients.api.DepositAccountsApi;
import com.backbase.mambu.clients.api.DepositTransactionsApi;
import com.backbase.mambu.clients.api.DocumentsApi;
import com.backbase.mambu.clients.api.LoanAccountsApi;
import com.backbase.mambu.clients.api.LoanTransactionsApi;
import com.backbase.mambu.clients.model.Client;
import com.backbase.mambu.clients.model.DepositAccount;
import com.backbase.productled.config.properties.EnvironmentConfigurationProperties;
import com.backbase.productled.config.properties.MambuConfigurationProperties;
import com.backbase.stream.LegalEntityTask;
import com.backbase.stream.legalentity.model.LegalEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MambuServiceTest {

    @Mock
    private ClientsApi clientsApi;
    @Mock
    private DepositAccountsApi depositAccountsApi;
    @Mock
    private DepositTransactionsApi depositTransactionsApi;
    @Mock
    private DocumentsApi documentsApi;
    @Mock
    private LoanAccountsApi loanAccountsApi;
    @Mock
    private LoanTransactionsApi loanTransactionsApi;
    @Mock
    private CentresConfigurationApi centresConfigurationApi;
    private MambuService mambuService;
    private LegalEntityTask legalEntityTask;

    @Before
    public void setUp() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();
        factoryBean.setResources(new ClassPathResource("application.yml"));

        Properties properties = factoryBean.getObject();

        ConfigurationPropertySource propertySource = new MapConfigurationPropertySource(properties);
        Binder binder = new Binder(propertySource);
        MambuConfigurationProperties configurationProperties =
            binder.bind("mambu", MambuConfigurationProperties.class).get();

        mambuService = new MambuService(clientsApi, depositAccountsApi, depositTransactionsApi, documentsApi,
            loanAccountsApi, loanTransactionsApi, centresConfigurationApi, configurationProperties, false);
        configurationProperties.setBootstrapResourcesEnabled(true);

        legalEntityTask = new LegalEntityTask(
            objectMapper.readValue(new File("src/test/resources/legal-entity-hierarchy.json"),
                LegalEntity.class));

    }

    @Test
    public void testExecuteTaskWhenDataExists() {
        // GIVEN
        when(clientsApi.getById(any(), any())).thenAnswer(i -> new Client().id(i.getArgument(0)));
        when(depositAccountsApi.getById(any(), any())).thenReturn(new DepositAccount().accountHolderKey("acctHolder"));

        // WHEN
        mambuService.executeTask(legalEntityTask);

        // THEN
        verify(clientsApi, times(4)).getById(any(), any());
        verify(clientsApi, never()).create(any(), any());
        verify(clientsApi, times(2)).update(any(), any());
        verify(depositAccountsApi, times(3)).getById(any(), any());
        verify(depositAccountsApi, never()).create(any(), any());
    }

    @Test
    public void testExecuteTaskWhenDataDoesntExist() {
        // GIVEN
        when(clientsApi.getById(any(), any()))
            .thenThrow(HttpClientErrorException.create("INVALID_CLIENT_ID", HttpStatus.NOT_FOUND, "not_found",
                new HttpHeaders(), new byte[]{}, StandardCharsets.UTF_8))
            .thenReturn(new Client().encodedKey("key"));
        when(clientsApi.create(any(), any())).thenAnswer(i -> i.getArgument(0));
        when(depositAccountsApi.getById(any(), any()))
            .thenThrow(HttpClientErrorException.create("INVALID_DEPOSIT_ACCOUNT_ID", HttpStatus.NOT_FOUND, "not_found",
                new HttpHeaders(), new byte[]{}, StandardCharsets.UTF_8));
        when(depositAccountsApi.create(any(), any())).thenAnswer(i -> i.getArgument(0));

        // WHEN
        mambuService.executeTask(legalEntityTask);

        // THEN
        verify(clientsApi, times(4)).getById(any(), any());
        verify(clientsApi, times(1)).create(any(), any());
        verify(depositAccountsApi, times(3)).getById(any(), any());
        verify(depositAccountsApi, times(3)).create(any(), any());
    }

    @Test
    public void testCentresConfigurationUpdate() {
        // GIVEN
        when(clientsApi.getById(any(), any())).thenAnswer(i -> new Client().id(i.getArgument(0)));
        when(depositAccountsApi.getById(any(), any())).thenReturn(new DepositAccount().accountHolderKey("acctHolder"));
        doNothing().when(centresConfigurationApi).update(any());

        // WHEN
        mambuService.executeTask(legalEntityTask);

        // THEN
        verify(centresConfigurationApi, times(1)).update(any());
    }

    @Test(expected = HttpClientErrorException.class)
    public void testCentresConfigurationUpdateInvalidYamlSyntax() {
        String badRequestResponseBody = "{\"errors\":[{\"errorCode\":10000,\"errorSource\":\"centres.null: while parsing a block mapping\",\"errorReason\":\"INVALID_YAML_SYNTAX\"}]}";

        // GIVEN
        when(clientsApi.getById(any(), any())).thenAnswer(i -> new Client().id(i.getArgument(0)));
        when(depositAccountsApi.getById(any(), any())).thenReturn(new DepositAccount().accountHolderKey("acctHolder"));
        doThrow(HttpClientErrorException.create(badRequestResponseBody, HttpStatus.BAD_REQUEST, "bad_request",
                new HttpHeaders(), new byte[]{}, StandardCharsets.UTF_8)).when(centresConfigurationApi).update(any());

        // WHEN
        mambuService.executeTask(legalEntityTask);
    }
}