package com.backbase.modelbank.config;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.buildingblocks.presentation.errors.UnauthorizedException;
import com.backbase.drivewealth.clients.login.api.LoginApi;
import com.backbase.drivewealth.clients.login.model.LoginResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class DWAccessTokenConfigTest {

    @Mock
    private LoginApi loginApi;
    @Mock
    private com.backbase.drivewealth.clients.instrument.ApiClient instrumentApiClient;
    @Mock
    private com.backbase.drivewealth.clients.marketdata.ApiClient marketDataApiCLient;
    @Mock
    private DriveWealthConfigurationProperties driveWealthConfigurationProperties;
    @InjectMocks
    private DWAccessTokenConfig instrumentMapper;

    @Test
    void testDwApiLogin() {
        // given
        when(loginApi.getAuthToken(any())).thenReturn(new LoginResponse().accessToken("access_token"));

        // when
        instrumentMapper.dwApiLogin();

        // then
        verify(instrumentApiClient).setBearerToken(any());
        verify(marketDataApiCLient).setBearerToken(any());
    }

    @Test
    void testDwApiLoginWithNullToken() {
        // given
        when(loginApi.getAuthToken(any())).thenReturn(new LoginResponse().accessToken(null));

        // when
        UnauthorizedException thrown = assertThrows(
            UnauthorizedException.class,
            () ->  instrumentMapper.dwApiLogin(),
            "Expected dwApiLogin() to throw Unauthorized, but it didn't"
        );

        assertTrue(thrown.getMessage().contentEquals("Failed to obtain dw accessToken"));
    }

    @Test
    void testDwApiLoginWithDwReturnException() {
        // given
        when(loginApi.getAuthToken(any())).thenThrow(new RestClientException("Something went wrong"));

        // when
        UnauthorizedException thrown = assertThrows(
            UnauthorizedException.class,
            () ->  instrumentMapper.dwApiLogin(),
            "Expected dwApiLogin() to throw Unauthorized, but it didn't"
        );

        assertTrue(thrown.getMessage().contentEquals("Failed to obtain dw accessToken"));
    }
}