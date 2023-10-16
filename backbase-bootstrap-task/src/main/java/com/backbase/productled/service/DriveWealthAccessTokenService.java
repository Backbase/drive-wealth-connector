package com.backbase.productled.service;

import com.backbase.buildingblocks.presentation.errors.UnauthorizedException;
import com.backbase.drivewealth.clients.login.api.LoginApi;
import com.backbase.drivewealth.clients.login.model.LoginRequest;
import com.backbase.productled.config.properties.DriveWealthConfigurationProperties;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;

@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnProperty(prefix = "drive-wealth", name="bootstrapResourcesEnabled", havingValue="true", matchIfMissing = true)
public class DriveWealthAccessTokenService {

    private final LoginApi loginApi;
    private final com.backbase.drivewealth.clients.orders.ApiClient ordersApiClient;
    private final com.backbase.drivewealth.clients.instrument.ApiClient instrumentApiClient;
    private final com.backbase.drivewealth.clients.marketdata.ApiClient marketDataApiClient;
    private final com.backbase.drivewealth.clients.accounts.ApiClient accountsApiClient;
    private final com.backbase.drivewealth.clients.deposits.ApiClient depositsApiClient;
    private final com.backbase.drivewealth.clients.users.ApiClient usersApiClient;
    private final DriveWealthConfigurationProperties dwConfig;

    @PostConstruct
    public void onStartup() {
        dwApiLogin();
    }

    /**
     * Schedule run DW login every 30 minute (1800000 millisecond)
     */
    @Scheduled(fixedRateString = "${drive-wealth.token.fixed.delay:1800000}")
    @Retryable(value = Throwable.class)
    public void dwApiLogin() {
        try {
            log.debug("Trying to perform login request to DW API");
            var loginResponse = loginApi.getAuthToken(getLoginRequest());

            if (loginResponse != null && StringUtils.hasText(loginResponse.getAccessToken())) {
                ordersApiClient.setBearerToken(loginResponse.getAccessToken());
                instrumentApiClient.setBearerToken(loginResponse.getAccessToken());
                marketDataApiClient.setBearerToken(loginResponse.getAccessToken());
                accountsApiClient.setBearerToken(loginResponse.getAccessToken());
                depositsApiClient.setBearerToken(loginResponse.getAccessToken());
                usersApiClient.setBearerToken(loginResponse.getAccessToken());
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
            .clientID(dwConfig.clientID())
            .clientSecret(dwConfig.clientSecret());
    }

}
