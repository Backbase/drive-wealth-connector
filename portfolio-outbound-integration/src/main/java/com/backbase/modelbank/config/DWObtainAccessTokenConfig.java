package com.backbase.modelbank.config;

import com.backbase.drivewealth.clients.login.api.LoginApi;
import com.backbase.drivewealth.clients.login.model.LoginRequest;
import com.backbase.modelbank.exceptions.DriveWealthObtainTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;


@RequiredArgsConstructor
@Component
@Log4j2
public class DWObtainAccessTokenConfig {

    private final LoginApi loginApi;
    private final com.backbase.drivewealth.clients.accounts.ApiClient accountApiClient;
    private final com.backbase.drivewealth.clients.marketdata.ApiClient marketDataApiCLient;
    private final com.backbase.drivewealth.clients.money.ApiClient moneyApiCLient;
    private final DriveWealthConfigurationProperties driveWealthConfigurationProperties;

    /**
     * Schedule run DW login
     */
    @Scheduled(initialDelayString = "${drive-wealth.auth.scheduler.initialDelay}", fixedRateString = "${drive-wealth.auth.scheduler.fixedRate}")
    @Retryable(value = Throwable.class,
            maxAttemptsExpression = "${drive-wealth.auth.retry.maxAttempts}",
            backoff = @Backoff(delayExpression = "${drive-wealth.auth.retry.maxDelay}"))
    public void dwApiLogin() {
        try {
            log.debug("Trying to perform login request to DW API");
            var loginResponse = loginApi.getAuthToken(getLoginRequest());

            if (loginResponse != null && StringUtils.hasText(loginResponse.getAccessToken())) {
                accountApiClient.setBearerToken(loginResponse.getAccessToken());
                marketDataApiCLient.setBearerToken(loginResponse.getAccessToken());
                moneyApiCLient.setBearerToken(loginResponse.getAccessToken());
                log.debug("Obtain DW accessToken Successfully");
            } else {
                log.error("Error while trying to obtain DW accessToken");
                throw new DriveWealthObtainTokenException("Failed to obtain dw accessToken");
            }
        } catch (RestClientException ex) {
            log.error("Error while trying to obtain DW accessToken", ex);
            throw new DriveWealthObtainTokenException("Failed to obtain dw accessToken");
        }
    }

    private LoginRequest getLoginRequest() {
        return new LoginRequest()
                .clientID(driveWealthConfigurationProperties.getClientID())
                .clientSecret(driveWealthConfigurationProperties.getClientSecret());
    }
}
