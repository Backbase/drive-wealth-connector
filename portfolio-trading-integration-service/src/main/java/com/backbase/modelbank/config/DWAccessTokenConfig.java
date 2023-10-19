package com.backbase.modelbank.config;

import com.backbase.buildingblocks.presentation.errors.UnauthorizedException;
import com.backbase.drivewealth.clients.login.api.LoginApi;
import com.backbase.drivewealth.clients.login.model.LoginRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class DWAccessTokenConfig {

    private final LoginApi loginApi;
    private final com.backbase.drivewealth.clients.orders.ApiClient ordersApiClient;
    private final com.backbase.drivewealth.clients.instrument.ApiClient instrumentApiClient;
    private final DriveWealthConfigurationProperties driveWealthConfigurationProperties;


    /**
     * Schedule run DW login every 30 minute (1800000 millisecond)
     */
    @Scheduled(initialDelayString = "${drive-wealth.token.initial.delay:1000}", fixedRateString = "${drive-wealth.token.fixed.delay:1800000}")
    @Retryable(value = Throwable.class)
    public void dwApiLogin() {
        try {
            log.debug("Trying to perform login request to DW API");
            var loginResponse = loginApi.getAuthToken(getLoginRequest());

            if (loginResponse != null && StringUtils.hasText(loginResponse.getAccessToken())) {
                ordersApiClient.setBearerToken(loginResponse.getAccessToken());
                instrumentApiClient.setBearerToken(loginResponse.getAccessToken());
                log.debug("Obtain DW accessToken Successfully");
            } else {
                log.error("Error while trying to obtain DW accessToken");
                throw new UnauthorizedException("Failed to obtain dw accessToken");
            }
        } catch (RestClientException ex) {
            log.error("Error while trying to obtain DW accessToken", ex);
            throw new UnauthorizedException("Failed to obtain dw accessToken");
        }
    }

    private LoginRequest getLoginRequest() {
        return new LoginRequest()
            .clientID(driveWealthConfigurationProperties.getClientID())
            .clientSecret(driveWealthConfigurationProperties.getClientSecret());
    }

}
