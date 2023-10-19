package com.backbase.modelbank.config;

import com.backbase.drivewealth.clients.accounts.ApiClient;
import com.backbase.drivewealth.clients.login.api.LoginApi;
import com.backbase.drivewealth.clients.login.model.LoginRequest;
import com.backbase.drivewealth.clients.login.model.LoginResponse;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@ActiveProfiles("it")
@DirtiesContext
@EnableScheduling
@SpringBootTest(
        classes = {DWObtainAccessTokenConfig.class},
        properties = {"drive-wealth.auth.scheduler.fixedRate=1800", "drive-wealth.auth.scheduler.initialDelay=100"}
)
class DWObtainAccessTokenConfigTest {
    private static final String ACCESS_TOKEN = "access_token";
    private final static String ACCESS_TOKEN_EXPIRATION = "1000";
    @MockBean
    LoginApi loginApi;
    @MockBean
    ApiClient accountApiClient;
    @MockBean
    com.backbase.drivewealth.clients.marketdata.ApiClient marketDataApiCLient;
    @MockBean
    com.backbase.drivewealth.clients.money.ApiClient moneyApiCLient;
    @MockBean
    DriveWealthConfigurationProperties driveWealthConfigurationProperties;
    @MockBean
    Logger log;
    @SpyBean
    DWObtainAccessTokenConfig dWObtainAccessTokenConfig;

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
                .thenReturn(new LoginResponse()
                        .accessToken(ACCESS_TOKEN)
                        .expiresIn(ACCESS_TOKEN_EXPIRATION)
                );

        // WHEN THEN
        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            verify(loginApi, atLeastOnce()).getAuthToken(any());
            verify(accountApiClient, atMostOnce()).setBearerToken(ACCESS_TOKEN);
            verify(marketDataApiCLient, atMostOnce()).setBearerToken(ACCESS_TOKEN);
            verify(moneyApiCLient, atMostOnce()).setBearerToken(ACCESS_TOKEN);
        });

        verify(dWObtainAccessTokenConfig, atMostOnce()).dwApiLogin();
    }


    @Test
    void testDwApiLogin_Error() {
        // Given
        when(loginApi.getAuthToken(any())).thenThrow(RestClientException.class);

        // WHEN THEN
        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            verify(loginApi, atLeastOnce()).getAuthToken(any());
        });

        verify(dWObtainAccessTokenConfig, atMostOnce()).dwApiLogin();
    }

    @Test
    void testDwApiLogin_Error_NoAccessToken() {
        // Given
        when(loginApi.getAuthToken(any())).thenReturn(new LoginResponse());

        // WHEN THEN
        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            verify(loginApi, atLeastOnce()).getAuthToken(any());
        });

        verify(dWObtainAccessTokenConfig, atMostOnce()).dwApiLogin();
    }
}