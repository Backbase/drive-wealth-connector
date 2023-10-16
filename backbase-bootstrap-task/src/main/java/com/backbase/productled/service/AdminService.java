package com.backbase.productled.service;

import com.backbase.admin.clients.api.AdminApi;
import com.backbase.admin.clients.model.PasswordModel;
import com.backbase.admin.clients.model.RoleModel;
import com.backbase.admin.clients.model.RoleResponse;
import com.backbase.admin.clients.model.UserModel;
import com.backbase.admin.clients.model.UserResponse;
import com.backbase.productled.config.properties.AdminConfigurationProperties;
import com.backbase.productled.model.RemoteConfigUserGroup;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.stereotype.Service;

/**
 * Automates the below listed operations.
 * 1. Verifies email for retail and employee realms.
 * 2. Sets default passwords for all users.
 * 3. Add admin roles for users in employee realm.
 */
@Slf4j
@AllArgsConstructor
@Service
public class AdminService {

    private static final String GRANT_TYPE = "password";
    private static final String CLIENT_ID = "admin-cli";

    private static final String ADMIN_USERNAME = "admin";
    private static final String EMPLOYEE_REALM = "employee";
    private static final String CUSTOMER_REALM = "customer";
    private static final String MASTER_REALM = "master";
    private static final Set<String> ADMIN_ROLES = Set.of("role_admin", "role_group_admin(admin)");
    private static final List<String> REALMS = List.of(EMPLOYEE_REALM, CUSTOMER_REALM);

    private final AdminApi adminApi;
    private final AdminConfigurationProperties adminConfig;
    private final List<RemoteConfigUserGroup> remoteConfigUsers;

    private void initAccessToken() {
        var accessToken = adminApi
            .getToken(MASTER_REALM, GRANT_TYPE, CLIENT_ID, adminConfig.getUsername(), adminConfig.getPassword())
            .getAccessToken();
        adminApi.getApiClient().setApiKeyPrefix("Bearer");
        adminApi.getApiClient().setApiKey(accessToken);
    }

    @NewSpan
    public void executeTask() {
        if (Boolean.TRUE.equals(adminConfig.getBootstrapManualStepsEnabled())) {
            initAccessToken();
            processManualSteps();
        } else {
            log.warn("Flag 'admin.bootstrapManualStepsEnabled' is set to false. Skipping manual steps in Admin console");
        }
    }

    private void processManualSteps() {
        final BiConsumer<String, UserResponse> verifyUserEmailStep = this::verifyUserEmail;
        final BiConsumer<String, UserResponse> resetUserPasswordStep = this::resetUserPassword;
        final BiConsumer<String, UserResponse> addAdminRolesForUserStep = this::addAdminRolesForUserInEmployeeRealm;

        var manualSteps = Set.of(
            verifyUserEmailStep,
            resetUserPasswordStep,
            addAdminRolesForUserStep
        );

        var mapOfUsers = REALMS.stream()
            .collect(Collectors.toMap(realmName -> realmName, adminApi::getAvailableUsers));

        mapOfUsers.forEach((realmName, users) ->
            users.forEach(user -> manualSteps.forEach(func -> func.accept(realmName, user))));
    }

    private void addAdminRolesForUserInEmployeeRealm(String realmName, UserResponse user) {
        if (EMPLOYEE_REALM.equals(realmName)) {
            var roles = getAvailableRolesForUser(realmName, user.getId());
            addAdminRolesForUser(realmName, user.getId(), roles,
                Stream.concat(remoteConfigUsers.stream().filter(
                        group -> group.getUsers() != null && group.getRole() != null && group.getUsers()
                            .contains(user.getUsername()))
                    .map(g -> g.getRole().name().toLowerCase()), ADMIN_ROLES.stream()).collect(Collectors.toSet()));
            log.info("Admin role added for user {} in realm {}", user.getUsername(), realmName);
        }
    }

    private void addAdminRolesForUser(String realmName, String userId, List<RoleResponse> roleResponseList, Set<String> rolesToAdd) {
        var roles = roleResponseList.stream()
            .filter(roleResponse -> rolesToAdd.contains(roleResponse.getName().toLowerCase()))
            .map(roleResponse -> new RoleModel()
                .clientRole(roleResponse.getClientRole())
                .composite(roleResponse.getComposite())
                .containerId(roleResponse.getContainerId())
                .id(roleResponse.getId())
                .name(roleResponse.getName()))
            .toList();

        adminApi.addRole(realmName, userId, roles);
    }

    private List<RoleResponse> getAvailableRolesForUser(String realmName, String userId) {
        return adminApi.getAvailableRoles(realmName, userId);
    }

    private void verifyUserEmail(String realmName, UserResponse user) {
        var userModel = new UserModel()
            .emailVerified(true);

        adminApi.verifyUserEmail(realmName, user.getId(), userModel);
        log.info("Email verification completed for user {} in realm {}", user.getUsername(), realmName);
    }

    private void resetUserPassword(String realmName, UserResponse user) {
        var passwordModel = Optional.ofNullable(user.getUsername())
            .filter(u -> u.contains(ADMIN_USERNAME))
            .map(u-> new PasswordModel()
                .type(GRANT_TYPE)
                .temporary(false)
                .value(adminConfig.getPassword()))
            .orElseGet(() -> new PasswordModel()
                .temporary(false)
                .type(GRANT_TYPE)
                .value(adminConfig.getDefaultUserPassword()));

        adminApi.resetPassword(realmName, user.getId(), passwordModel);
        log.info("Password reset operation completed for user {} in realm {}", user.getUsername(), realmName);
    }

}
