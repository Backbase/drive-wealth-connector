package com.backbase.productled.config.properties;

import com.backbase.productled.model.RemoteConfigUserGroup;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@Data
@AllArgsConstructor
@Slf4j
public class RemoteConfigDataConfiguration {

    private static final String HYPHEN = "-";
    private final EnvironmentConfigurationProperties properties;
    @Bean
    public List<RemoteConfigUserGroup> remoteConfigUsers(ObjectMapper mapper, AdminConfigurationProperties adminConfigurationProperties) {
        try {
            List<RemoteConfigUserGroup> remoteConfigUsers = mapper.readValue(ResourceUtils.getFile(adminConfigurationProperties.getRemoteConfigUsersLocation()), new TypeReference<>() {});
            remoteConfigUsers.forEach(rcusergroup -> rcusergroup.setUsers(
                    (rcusergroup.getUsers().stream().map(
                            this::appendEnvDetails
                    ).collect(Collectors.toSet())
                    ))
            );
            return remoteConfigUsers;
        } catch (IOException e) {
            log.error("Error loading Remote Config users JSON: File not found in classpath");
            return Collections.emptyList();
        }
    }
    private String appendEnvDetails(String user) {
        Objects.requireNonNull(user, "ExternalId must not be null");

        if (!CollectionUtils.isEmpty(properties.getEnrichment().getExcludeList()) && excludePrefixMatches(user)) {
            return user;
        }

        return Stream.of(properties.getInstallation(), properties.getRuntime(), user)
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining(HYPHEN));

    }

    private boolean excludePrefixMatches(String externalId) {
        for (String prefix : properties.getEnrichment().getExcludeList()) {
            if (externalId.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

}