package com.backbase.productled.service;

import com.backbase.dbs.limit.api.client.v2.LimitsApi;
import com.backbase.dbs.limit.api.client.v2.model.UpsertGlobalLimitsRequestBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LimitsService {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String USER_CONTEXT_COOKIE = "USER_CONTEXT";
    private final LimitsApi limitsApi;
    private final LoginService loginService;

    public LimitsService(
        @Qualifier("limitsClientApi") LimitsApi limitsApi, LoginService loginService) {
        this.limitsApi = limitsApi;
        this.loginService = loginService;
    }

    public void boostrapGlobalLimits(UpsertGlobalLimitsRequestBody limitsRequestBody) {
        log.info("Bootstrapping Global Limits");
        var httpHeaders =  loginService.loginAndSetContext();
        limitsApi.getApiClient().addDefaultHeader(AUTHORIZATION_HEADER, httpHeaders.get(AUTHORIZATION_HEADER));
        limitsApi.getApiClient().addDefaultCookie(USER_CONTEXT_COOKIE, httpHeaders.get(USER_CONTEXT_COOKIE));
        limitsApi.putGlobalLimits(limitsRequestBody);
        log.info("Successfully Bootstrapped Global Limits");
    }

}
