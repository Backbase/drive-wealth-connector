package com.backbase.productled.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.admin.clients.ApiClient;
import com.backbase.admin.clients.api.AdminApi;
import com.backbase.admin.clients.model.AccessTokenResponse;
import com.backbase.admin.clients.model.RoleResponse;
import com.backbase.admin.clients.model.UserResponse;
import com.backbase.productled.config.properties.AdminConfigurationProperties;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.io.ClassPathResource;

@RunWith(MockitoJUnitRunner.class)
public class AdminServiceTest {

    @Mock
    private AdminApi adminApi;

    private AdminService adminService;

    @Before
    public void setUp() {
        var factoryBean = new YamlPropertiesFactoryBean();
        factoryBean.setResources(new ClassPathResource("application.yml"));

        var properties = factoryBean.getObject();

        var propertySource = new MapConfigurationPropertySource(properties);
        var binder = new Binder(propertySource);

        var configurationProperties = binder
            .bind("admin", AdminConfigurationProperties.class).get();
        configurationProperties.setBootstrapManualStepsEnabled(true);

        adminService = new AdminService(adminApi, configurationProperties, Collections.emptyList());

    }

    @Test
    public void testExecuteTaskShouldCompleteManualSteps() {
        // Given
        when(adminApi.getToken(any(), any(), any(), any(), any()))
            .thenReturn(new AccessTokenResponse().accessToken("token"));
        when(adminApi.getAvailableUsers(any()))
            .thenReturn(List.of(
                new UserResponse().username("admin").id("id-1"),
                new UserResponse().username("test").id("id-2")
            ));
        when(adminApi.getAvailableRoles(any(), any()))
            .thenReturn(List.of(
                new RoleResponse()
                    .id("id")
                    .name("ROLE_ADMIN")
                    .clientRole(false)
                    .composite(false)
                    .containerId("containerId")
            ));
        when(adminApi.getApiClient()).thenReturn(new ApiClient());
        doNothing().when(adminApi).addRole(any(), any(), any());
        doNothing().when(adminApi).verifyUserEmail(any(), any(), any());
        doNothing().when(adminApi).resetPassword(any(), any(), any());

        // When
        adminService.executeTask();

        // Then
        verify(adminApi, times(4)).verifyUserEmail(any(), any(), any());
        verify(adminApi, times(4)).resetPassword(any(), any(), any());
        verify(adminApi, times(2)).addRole(any(), any(), any());
    }
}
