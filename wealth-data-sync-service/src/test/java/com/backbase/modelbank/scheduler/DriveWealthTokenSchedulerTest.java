package com.backbase.modelbank.scheduler;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import com.backbase.drivewealth.reactive.clients.login.api.LoginApi;
import com.backbase.drivewealth.reactive.clients.login.model.LoginRequest;
import com.backbase.drivewealth.reactive.clients.login.model.LoginResponse;
import com.backbase.modelbank.config.DriveWealthConfigurationProperties;
import com.backbase.modelbank.scheduler.DriveWealthTokenScheduler;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientException;
import reactor.core.publisher.Mono;

@ActiveProfiles("it")
@DirtiesContext
@EnableScheduling
@SpringBootTest(
        classes = {DriveWealthTokenScheduler.class},
        properties = {"drive-wealth.auth.scheduler.fixedRate=1800", "drive-wealth.auth.scheduler.initialDelay=100"}
)
class DriveWealthTokenSchedulerTest {
    private static final String ACCESS_TOKEN = "access_token";
    private final static String ACCESS_TOKEN_EXPIRATION = "1000";
    @MockBean
    LoginApi loginApi;
    @MockBean
    com.backbase.drivewealth.reactive.clients.accounts.ApiClient accountApiClient;
    @MockBean
    com.backbase.drivewealth.reactive.clients.marketdata.ApiClient marketDataApiClient;
    @MockBean
    com.backbase.drivewealth.reactive.clients.money.ApiClient moneyApiClient;
    @MockBean
    com.backbase.drivewealth.reactive.clients.transactions.ApiClient transactionApiClient;
    @MockBean
    com.backbase.drivewealth.reactive.clients.instrument.ApiClient instrumentApiClient;
    @MockBean
    com.backbase.drivewealth.reactive.clients.orders.ApiClient orderApiClient;
    @MockBean
    com.backbase.drivewealth.reactive.clients.settlements.ApiClient settlementApiClient;

    @MockBean
    DriveWealthConfigurationProperties driveWealthConfigurationProperties;
    @SpyBean
    DriveWealthTokenScheduler driveWealthTokenScheduler;

    @BeforeEach
    public void setup() {
        when(driveWealthConfigurationProperties.getClientID())
                .thenReturn("getClientIDResponse");
        when(driveWealthConfigurationProperties.getClientSecret())
                .thenReturn("getClientSecretResponse");
    }

    @Test
    void testDwApiLogin() {

        // Given
        when(loginApi.getAuthToken(any(LoginRequest.class)))
                .thenReturn(Mono.just(
                        new LoginResponse()
                                .accessToken(ACCESS_TOKEN)
                                .expiresIn(ACCESS_TOKEN_EXPIRATION)
                ));

        // WHEN THEN
        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            verify(loginApi, atLeastOnce()).getAuthToken(any());
            verify(accountApiClient, atMostOnce()).setBearerToken(ACCESS_TOKEN);
            verify(marketDataApiClient, atMostOnce()).setBearerToken(ACCESS_TOKEN);
            verify(moneyApiClient, atMostOnce()).setBearerToken(ACCESS_TOKEN);
            verify(instrumentApiClient, atMostOnce()).setBearerToken(ACCESS_TOKEN);
            verify(transactionApiClient, atMostOnce()).setBearerToken(ACCESS_TOKEN);
        });

        verify(driveWealthTokenScheduler, atMostOnce()).dwApiLogin();
    }


    @Test
    void testDwApiLogin_Error() {
        // Given
        when(loginApi.getAuthToken(any())).thenThrow(RestClientException.class);

        // WHEN THEN
        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> verify(loginApi, atLeastOnce()).getAuthToken(any()));

        verify(driveWealthTokenScheduler, atMostOnce()).dwApiLogin();
    }

    @Test
    void testDwApiLogin_NoToken() {
        // Given
        when(loginApi.getAuthToken(any(LoginRequest.class)))
            .thenReturn(Mono.just(
                new LoginResponse()
            ));

        // WHEN THEN
        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> verify(loginApi, atLeastOnce()).getAuthToken(any()));

        verify(driveWealthTokenScheduler, atMostOnce()).dwApiLogin();
    }
}