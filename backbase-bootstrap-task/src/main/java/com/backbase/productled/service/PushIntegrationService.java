package com.backbase.productled.service;

import com.backbase.admin.clients.api.AdminApi;
import com.backbase.admin.clients.model.AccessTokenResponse;
import com.backbase.productled.config.properties.AdminConfigurationProperties;
import com.backbase.productled.config.properties.BootstrapDataConfigurationProperties;
import com.backbase.services.mobile.push.rest.clientapi.spec.v2.ClientPushApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;

import static com.backbase.productled.service.LoginService.*;

/**
 * @copyright (C) 2022, Backbase
 * @Version 1.0
 * @Since 03. Nov 2022 3:47 pm
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class PushIntegrationService {

    private final ClientPushApi clientPushApi;
    private final AdminApi adminApi;
    private final AdminConfigurationProperties adminConfig;


    public Mono<Void> bootstrapPushIntegrationCertificate(BootstrapDataConfigurationProperties.PushIntegration.Certificate certificate) {

        AccessTokenResponse loginResponse = adminApi
                .getToken(EMPLOYEE_REALM, GRANT_TYPE, CLIENT_ID, adminConfig.getUsername(), adminConfig.getPassword());

        clientPushApi.getApiClient().addDefaultHeader(AUTHORIZATION_HEADER, "Bearer " + loginResponse.getAccessToken());
        clientPushApi.getApiClient().addDefaultHeader(X_XSRF_TOKEN_HEADER, loginResponse.getSessionState());

        try {
            log.debug("Trying to post push credential, {}", certificate);
            return clientPushApi.postCredentials(certificate.getAppName(),
                    certificate.getPlatform().name(),
                    certificate.getIsProduction().toString(),
                    certificate.getPassword(),
                    ResourceUtils.getFile(certificate.getLocation()),
                    certificate.getApnsTopic()
            ).onErrorResume(WebClientResponseException.class, (throwable) -> Mono.error(new RuntimeException("Failed to post credentials: " + throwable.getResponseBodyAsString(), throwable)));
        } catch (FileNotFoundException ex) {
            log.error("Error reading certificate file", ex);
            return Mono.error(new RuntimeException(ex.getMessage()));
        }
    }
}
