package com.backbase.productled.service;

import static org.apache.logging.log4j.util.Strings.EMPTY;

import com.backbase.admin.clients.api.AdminApi;
import com.backbase.dbs.accesscontrol.client.api.v2.UserContextApi;
import com.backbase.dbs.accesscontrol.client.model.v2.Serviceagreementpartialitem;
import com.backbase.dbs.accesscontrol.client.model.v2.UserContextPOST;
import com.backbase.productled.config.properties.AdminConfigurationProperties;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LoginService {

    static final String GRANT_TYPE = "password";
    static final String CLIENT_ID = "bb-tooling-client";
    static final String EMPLOYEE_REALM = "employee";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String X_XSRF_TOKEN_HEADER = "X-XSRF-TOKEN";

    public static final String SET_COOKIE_HEADER = "set-cookie";
    public static final String USER_CONTEXT_COOKIE = "USER_CONTEXT";
    public static final String EQUALS = "=";

    private final AdminApi adminApi;
    @Qualifier("userContextApiV2")
    private final UserContextApi userContextApi;
    private final AdminConfigurationProperties adminConfig;


    public Map<String, String> loginAndSetContext() {
        HashMap<String, String> headers = new HashMap<>();
        var token = doLogin();
        userContextApi.getApiClient().addDefaultHeader(AUTHORIZATION_HEADER, "Bearer " + token);
        var saId = getServiceAgreementId();
        var userContextCookie = postUserContext(saId);
        headers.put(AUTHORIZATION_HEADER, "Bearer " + token);
        headers.put(USER_CONTEXT_COOKIE, userContextCookie);
        return headers;
    }

    public String doLogin() {
        return adminApi
            .getToken(EMPLOYEE_REALM, GRANT_TYPE, CLIENT_ID, adminConfig.getUsername(), adminConfig.getPassword())
            .getAccessToken();
    }

    @NotNull
    private String postUserContext(String saId) {
        return userContextApi.postUserContextWithHttpInfo(
                new UserContextPOST().serviceAgreementId(saId))
            .getHeaders()
                .getOrEmpty(SET_COOKIE_HEADER)
                .stream().filter(cookie -> cookie.contains(USER_CONTEXT_COOKIE))
            .map(context -> context.split(EQUALS)[1])
            .findFirst().orElse(EMPTY);
    }

    @NotNull
    private String getServiceAgreementId() {
        return userContextApi.getUserContextServiceAgreements(null, 0, null, 7).stream().findFirst()
            .map(Serviceagreementpartialitem::getId)
                .orElse("");
    }

}
