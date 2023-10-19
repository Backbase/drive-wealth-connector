package com.backbase.modelbank.scheduler;

import com.backbase.drivewealth.reactive.clients.login.api.LoginApi;
import com.backbase.drivewealth.reactive.clients.login.model.LoginRequest;
import com.backbase.modelbank.config.DriveWealthConfigurationProperties;
import com.backbase.modelbank.exceptions.DriveWealthObtainTokenException;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;

/**
 * Configuration to obtain drive-wealth access token in a regular basis, base on configuration
 * <pre>
 *       drive-wealth.auth.scheduler.fixedRate  // run on a fixed rate in ms
 *       drive-wealth.auth.retry.maxAttempt     // max retry attempts
 *       drive-wealth.auth.retry.maxDelay       // max retry delay
 * </pre>
 */
@RequiredArgsConstructor
@Component
@Log4j2
public class DriveWealthTokenScheduler {

    private final LoginApi loginApi;
    private final com.backbase.drivewealth.reactive.clients.accounts.ApiClient accountApiClient;
    private final com.backbase.drivewealth.reactive.clients.marketdata.ApiClient marketDataApiClient;
    private final com.backbase.drivewealth.reactive.clients.money.ApiClient moneyApiClient;
    private final com.backbase.drivewealth.reactive.clients.transactions.ApiClient transactionsApiClient;
    private final com.backbase.drivewealth.reactive.clients.instrument.ApiClient instrumentsApiClient;
    private final com.backbase.drivewealth.reactive.clients.orders.ApiClient ordersApiClient;
    private final com.backbase.drivewealth.reactive.clients.settlements.ApiClient settlementsApiClient;
    private final DriveWealthConfigurationProperties driveWealthConfigurationProperties;

    /**
     * Schedule run DW login
     */
    @Scheduled(initialDelayString = "${drive-wealth.auth.scheduler.initialDelay}", fixedRateString = "${drive-wealth.auth.scheduler.fixedRate}")
    @Retryable(value = Throwable.class, maxAttemptsExpression = "${drive-wealth.auth.retry.maxAttempts}",
        backoff = @Backoff(delayExpression = "${drive-wealth.auth.retry.maxDelay}"))
    public void dwApiLogin() {
        try {
            log.debug("Trying to perform login request to DW API");
            var loginResponse = loginApi.getAuthToken(getLoginRequest()).block();

            if (loginResponse != null && StringUtils.hasText(loginResponse.getAccessToken())) {
                accountApiClient.setBearerToken(loginResponse.getAccessToken());
                marketDataApiClient.setBearerToken(loginResponse.getAccessToken());
                moneyApiClient.setBearerToken(loginResponse.getAccessToken());
                transactionsApiClient.setBearerToken(loginResponse.getAccessToken());
                instrumentsApiClient.setBearerToken(loginResponse.getAccessToken());
                ordersApiClient.setBearerToken(loginResponse.getAccessToken());
                settlementsApiClient.setBearerToken(loginResponse.getAccessToken());
                log.info("Obtain DW accessToken Successfully");
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
        return new LoginRequest().clientID(driveWealthConfigurationProperties.getClientID())
            .clientSecret(driveWealthConfigurationProperties.getClientSecret());
    }
}
