package com.backbase.productled.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.admin.clients.ApiClient;
import com.backbase.admin.clients.api.AdminApi;
import com.backbase.admin.clients.model.AccessTokenResponse;
import com.backbase.admin.clients.model.RoleModel;
import com.backbase.admin.clients.model.RoleResponse;
import com.backbase.admin.clients.model.UserResponse;
import com.backbase.productled.config.properties.AdminConfigurationProperties;
import com.backbase.productled.model.RemoteConfigRoleEnum;
import com.backbase.productled.model.RemoteConfigUserGroup;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
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
import org.springframework.util.ResourceUtils;

@RunWith(MockitoJUnitRunner.class)
public class RemoteConfigUserTest {

    @Mock
    private AdminApi adminApi;

    private AdminService adminService;

    @Before
    public void setUp() throws IOException {
        var objectMapper = new ObjectMapper();
        var factoryBean = new YamlPropertiesFactoryBean();
        factoryBean.setResources(new ClassPathResource("application.yml"));

        var properties = factoryBean.getObject();

        var propertySource = new MapConfigurationPropertySource(properties);
        var binder = new Binder(propertySource);

        var configurationProperties = binder
            .bind("admin", AdminConfigurationProperties.class).get();
        configurationProperties.setBootstrapManualStepsEnabled(true);
        configurationProperties.setRemoteConfigUsersLocation("classpath:remote-config-users.json");

        var remoteConfigUsers = objectMapper.readValue(
            ResourceUtils.getFile(configurationProperties.getRemoteConfigUsersLocation()),
            new TypeReference<List<RemoteConfigUserGroup>>() {
            });

        adminService = new AdminService(adminApi, configurationProperties, remoteConfigUsers);

    }

    @Test
    public void testRemoteConfigUsers() {
        // Given
        when(adminApi.getToken(any(), any(), any(), any(),any()))
            .thenReturn(new AccessTokenResponse().accessToken("token"));
        when(adminApi.getAvailableUsers(any()))
            .thenReturn(List.of(
                new UserResponse().username("admin").id("id-1"),
                new UserResponse().username("daria").id("id-2"),
                new UserResponse().username("milad").id("id-3"),
                new UserResponse().username("george").id("id-4"),
                new UserResponse().username("tibor").id("id-5"),
                new UserResponse().username("eric").id("id-6"),
                new UserResponse().username("ankit").id("id-7")
            ));
        when(adminApi.getAvailableRoles(any(), any()))
            .thenReturn(List.of(
                    new RoleResponse()
                        .id("id1")
                        .name(RemoteConfigRoleEnum.ROLE_REMOTE_CONFIG_ADMIN.name())
                        .clientRole(false)
                        .composite(false)
                        .containerId("containerId"),
                    new RoleResponse()
                        .id("id2")
                        .name(RemoteConfigRoleEnum.ROLE_REMOTE_CONFIG_MANAGER.name())
                        .clientRole(false)
                        .composite(false)
                        .containerId("containerId"),
                    new RoleResponse()
                        .id("id3")
                        .name(RemoteConfigRoleEnum.ROLE_REMOTE_CONFIG_DEVELOPER.name())
                        .clientRole(false)
                        .composite(false)
                        .containerId("containerId")
                )
            );
        when(adminApi.getApiClient()).thenReturn(new ApiClient());
        doNothing().when(adminApi).addRole(any(), any(), any());
        doNothing().when(adminApi).verifyUserEmail(any(), any(), any());
        doNothing().when(adminApi).resetPassword(any(), any(), any());

        // When
        adminService.executeTask();

        // Then
        verify(adminApi, times(14)).verifyUserEmail(any(), any(), any());
        verify(adminApi, times(14)).resetPassword(any(), any(), any());
        // Check user/roles
        verify(adminApi).addRole(any(), eq("id-1"), eq(List.of(new RoleModel()
            .clientRole(false)
            .composite(false)
            .containerId("containerId")
            .id("id1")
            .name(RemoteConfigRoleEnum.ROLE_REMOTE_CONFIG_ADMIN.name()))));
        verify(adminApi).addRole(any(), eq("id-2"), eq(List.of(new RoleModel()
            .clientRole(false)
            .composite(false)
            .containerId("containerId")
            .id("id1")
            .name(RemoteConfigRoleEnum.ROLE_REMOTE_CONFIG_ADMIN.name()))));
        verify(adminApi).addRole(any(), eq("id-3"), eq(List.of(new RoleModel()
            .clientRole(false)
            .composite(false)
            .containerId("containerId")
            .id("id2")
            .name(RemoteConfigRoleEnum.ROLE_REMOTE_CONFIG_MANAGER.name()))));
        verify(adminApi).addRole(any(), eq("id-4"), eq(List.of(new RoleModel()
            .clientRole(false)
            .composite(false)
            .containerId("containerId")
            .id("id2")
            .name(RemoteConfigRoleEnum.ROLE_REMOTE_CONFIG_MANAGER.name()))));
        verify(adminApi).addRole(any(), eq("id-5"), eq(List.of(new RoleModel()
            .clientRole(false)
            .composite(false)
            .containerId("containerId")
            .id("id3")
            .name(RemoteConfigRoleEnum.ROLE_REMOTE_CONFIG_DEVELOPER.name()))));
        verify(adminApi).addRole(any(), eq("id-6"), eq(List.of(new RoleModel()
            .clientRole(false)
            .composite(false)
            .containerId("containerId")
            .id("id3")
            .name(RemoteConfigRoleEnum.ROLE_REMOTE_CONFIG_DEVELOPER.name()))));
        verify(adminApi).addRole(any(), eq("id-7"), eq(List.of(new RoleModel()
            .clientRole(false)
            .composite(false)
            .containerId("containerId")
            .id("id3")
            .name(RemoteConfigRoleEnum.ROLE_REMOTE_CONFIG_DEVELOPER.name()))));
    }

}
