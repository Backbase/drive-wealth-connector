package com.backbase.productled.service;

import static java.util.Optional.ofNullable;

import com.backbase.productled.config.properties.EnvironmentConfigurationProperties;
import com.backbase.productled.config.properties.EnvironmentConfigurationProperties.Enrichment;
import com.backbase.stream.legalentity.model.JobProfileUser;
import com.backbase.stream.legalentity.model.LegalEntity;
import com.backbase.stream.legalentity.model.ProductGroup;
import com.backbase.stream.legalentity.model.ServiceAgreement;
import com.backbase.stream.legalentity.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnvironmentDetailsEnricherTest {

    private LegalEntity legalEntity;

    private EnvironmentConfigurationProperties configurationProperties;

    private EnvironmentDetailsEnricher environmentDetailsEnricher;

    @Before
    public void setUp() throws IOException {

        var enrichment = new Enrichment();
        enrichment.setExcludeList(List.of("admin"));

        configurationProperties = new EnvironmentConfigurationProperties();
        configurationProperties.setEnrichment(enrichment);
        configurationProperties.setInstallation("ref");
        configurationProperties.setRuntime("bus-dev");

        environmentDetailsEnricher = new EnvironmentDetailsEnricher(configurationProperties,false);

        legalEntity = new ObjectMapper().readValue(new File("src/main/resources/le-hierarchy/le-base-structure.json"),
            LegalEntity.class);
    }

    @Test
    public void testExecutionTask() {

        environmentDetailsEnricher.enrich(legalEntity);

        String envPrefix = Stream.of(configurationProperties.getInstallation(), configurationProperties.getRuntime())
            .filter(s -> s != null && !s.isEmpty())
            .collect(Collectors.joining("-"));

        Optional.ofNullable(legalEntity).ifPresent(entity -> {
            validateAdministrators(envPrefix, entity.getAdministrators());
            validateLegalEntityExternalId(envPrefix, entity);
            validateUsers(envPrefix, entity.getUsers());
            validateProductGroups(envPrefix, entity.getProductGroups());
            validateServiceAgreement(envPrefix, entity.getCustomServiceAgreement());
            validateServiceAgreement(envPrefix, entity.getMasterServiceAgreement());
            validateSubsidiaries(envPrefix, entity.getSubsidiaries());
        });

    }

    private void validateSubsidiaries(String envPrefix, List<LegalEntity> subsidiaries) {
        ofNullable(subsidiaries)
            .orElseGet(Collections::emptyList)
            .stream()
            .filter(Objects::nonNull)
            .forEach(legalEntity -> {
                validateLegalEntityExternalId(envPrefix, legalEntity);
                validateUsers(envPrefix, legalEntity.getUsers());
                validateAdministrators(envPrefix, legalEntity.getAdministrators());
                validateProductGroups(envPrefix, legalEntity.getProductGroups());
                validateServiceAgreement(envPrefix, legalEntity.getCustomServiceAgreement());
                validateServiceAgreement(envPrefix, legalEntity.getMasterServiceAgreement());
                validateSubsidiaries(envPrefix, legalEntity.getSubsidiaries());
            });
    }

    private void validateServiceAgreement(String envPrefix, ServiceAgreement customServiceAgreement) {
        if (customServiceAgreement != null && customServiceAgreement.getExternalId() != null) {
            Assert.assertFalse(customServiceAgreement.getExternalId().contains(envPrefix));
        }
    }

    private void validateProductGroups(String envPrefix, List<ProductGroup> productGroups) {

        Optional.ofNullable(productGroups).ifPresent(pgs -> {
            pgs
                .forEach(productGroup -> Assert.assertFalse(productGroup.getName().contains(envPrefix)));
            pgs.stream().filter(Objects::nonNull)
                .filter(productGroup -> Objects.nonNull(productGroup.getSavingAccounts()))
                .forEach(productGroup -> productGroup.getSavingAccounts().forEach(
                    savingsAccount -> Assert.assertTrue(savingsAccount.getExternalId().contains(envPrefix))));
            pgs.stream().filter(Objects::nonNull)
                .filter(productGroup -> Objects.nonNull(productGroup.getCurrentAccounts()))
                .forEach(productGroup -> productGroup.getCurrentAccounts().forEach(
                    currentAccount -> Assert.assertTrue(currentAccount.getExternalId().contains(envPrefix))));
        });
    }

    private void validateUsers(String envPrefix, List<JobProfileUser> users) {

        Optional.ofNullable(users).ifPresent(jobProfileUsers -> jobProfileUsers.forEach(
            jobProfileUser -> {
                if (excludePrefixMatches(jobProfileUser.getUser().getExternalId())) {
                    Assert.assertFalse(jobProfileUser.getUser().getExternalId().contains(envPrefix));
                } else {
                    Assert.assertTrue(jobProfileUser.getUser().getExternalId().contains(envPrefix));
                }
            }));
    }

    private void validateLegalEntityExternalId(String envPrefix, LegalEntity entity) {

        if (excludePrefixMatches(entity.getExternalId())) {
            Assert.assertFalse(entity.getExternalId().contains(envPrefix));
        } else {
            Assert.assertTrue(entity.getExternalId().contains(envPrefix));
        }
    }

    private void validateAdministrators(String envPrefix, List<User> administrators) {

        Optional.ofNullable(administrators).ifPresent(admins -> admins.forEach(
            admin -> {
                if (excludePrefixMatches(admin.getExternalId())) {
                    Assert.assertFalse(admin.getExternalId().contains(envPrefix));
                } else {
                    Assert.assertTrue(admin.getExternalId().contains(envPrefix));
                }
            }));
    }

    private boolean excludePrefixMatches(String externalId) {
        for (String prefix : configurationProperties.getEnrichment().getExcludeList()) {
            if (externalId.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

}