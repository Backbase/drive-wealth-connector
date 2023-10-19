package com.backbase.productled.service;


import static java.util.Optional.ofNullable;

import com.backbase.productled.config.properties.EnvironmentConfigurationProperties;
import com.backbase.stream.legalentity.model.JobProfileUser;
import com.backbase.stream.legalentity.model.LegalEntity;
import com.backbase.stream.legalentity.model.LegalEntityReference;
import com.backbase.stream.legalentity.model.ProductGroup;
import com.backbase.stream.legalentity.model.ServiceAgreement;
import com.backbase.stream.legalentity.model.User;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * Enrich the details about installation and runtime to user's externalId in LegalEntity data
 */
@Slf4j
@AllArgsConstructor
@Service
public class EnvironmentDetailsEnricher {

    private static final String HYPHEN = "-";
    private final EnvironmentConfigurationProperties properties;

    @Value("${enableExperimentalUniversalSupport}")
    private final boolean enableExperimentalUniversalSupport;

    public void enrich(LegalEntity legalEntity) {
        Optional.ofNullable(legalEntity).ifPresent(entity -> {
            processAdministrators(entity.getAdministrators());
            updateLegalEntityExternalId(entity);
            processUsers(entity.getUsers());
            processProductGroups(entity.getProductGroups());
            processServiceAgreement(entity.getCustomServiceAgreement());
            processServiceAgreement(entity.getMasterServiceAgreement());
            processSubsidiaries(entity.getSubsidiaries());
        });
    }

    private void processServiceAgreement(ServiceAgreement serviceAgreement) {
        ofNullable(serviceAgreement).ifPresent(sa -> {

            ofNullable(sa.getCreatorLegalEntity())
                .ifPresent(le -> sa.setCreatorLegalEntity(appendEnvDetails(le)));
            ofNullable(sa.getParticipants())
                .ifPresent(participants -> participants.forEach(participant -> {
                    participant.setExternalId(appendEnvDetails(participant.getExternalId()));
                    if (participant.getUsers() != null) {
                        participant.getUsers().replaceAll(this::appendEnvDetails);
                    }
                    if (participant.getAdmins() != null) {
                        participant.getAdmins().replaceAll(this::appendEnvDetails);
                    }
                }));
        });
    }

    private void processProductGroups(List<ProductGroup> productGroups) {
        ofNullable(productGroups).ifPresent(pgs -> pgs.forEach(productGroup -> {
            processUsers(productGroup.getUsers());
            ofNullable(productGroup.getCurrentAccounts()).ifPresent(cas -> cas.forEach(
                currentAccount -> {
                    currentAccount.setExternalId(appendEnvDetails(currentAccount.getExternalId()));
                    if(!enableExperimentalUniversalSupport && currentAccount.getBBAN() != null) {
                        currentAccount.setBBAN(appendEnvDetails(currentAccount.getBBAN()));
                    }
                    processLegalEntities(currentAccount.getLegalEntities());

                }));
            ofNullable(productGroup.getSavingAccounts()).ifPresent(sas -> sas.forEach(
                savingsAccount -> {
                    savingsAccount.setExternalId(appendEnvDetails(savingsAccount.getExternalId()));
                    if(!enableExperimentalUniversalSupport && savingsAccount.getBBAN() != null) {
                        savingsAccount.setBBAN(appendEnvDetails(savingsAccount.getBBAN()));
                    }
                }));
            ofNullable(productGroup.getLoans()).ifPresent(sas -> sas.forEach(
                loanAccount -> {
                    loanAccount.setExternalId(appendEnvDetails(loanAccount.getExternalId()));
                    if(!enableExperimentalUniversalSupport && loanAccount.getBBAN() != null) {
                        loanAccount.setBBAN(appendEnvDetails(loanAccount.getBBAN()));
                    }
                    loanAccount.setInterestSettlementAccount(
                        appendEnvDetails(loanAccount.getInterestSettlementAccount()));
                }));
        }));
    }

    private void processLegalEntities(List<LegalEntityReference> legalEntities) {
        ofNullable(legalEntities).ifPresent(les -> les.forEach(legalEntityReference ->
            legalEntityReference.setExternalId(appendEnvDetails(legalEntityReference.getExternalId()))));
    }

    private void processSubsidiaries(List<LegalEntity> subsidiaries) {
        ofNullable(subsidiaries)
            .orElseGet(Collections::emptyList)
            .stream()
            .filter(Objects::nonNull)
            .forEach(legalEntity -> {
                updateLegalEntityExternalId(legalEntity);
                processUsers(legalEntity.getUsers());
                processAdministrators(legalEntity.getAdministrators());
                processProductGroups(legalEntity.getProductGroups());
                processServiceAgreement(legalEntity.getCustomServiceAgreement());
                processServiceAgreement(legalEntity.getMasterServiceAgreement());
                processSubsidiaries(legalEntity.getSubsidiaries());
            });
    }

    private void processUsers(List<JobProfileUser> users) {
        ofNullable(users).ifPresent(usersOpt ->
            usersOpt.forEach(user -> user.getUser().setExternalId(appendEnvDetails(user.getUser().getExternalId()))));
    }

    private void processAdministrators(List<User> users) {
        ofNullable(users)
            .ifPresent(userOptional -> userOptional
                .forEach(user -> user.setExternalId(appendEnvDetails(user.getExternalId()))));
    }

    private void updateLegalEntityExternalId(LegalEntity legalEntity) {
        ofNullable(legalEntity)
            .map(data -> {
                data.setExternalId(appendEnvDetails(data.getExternalId()));
                ofNullable(data.getParentExternalId())
                    .ifPresent(parentExternalId -> data.setParentExternalId(appendEnvDetails(parentExternalId)));
                return data;
            });
    }

    private String appendEnvDetails(String externalId) {
        Objects.requireNonNull(externalId, "ExternalId must not be null");

        if (!CollectionUtils.isEmpty(properties.getEnrichment().getExcludeList()) && excludePrefixMatches(externalId)) {
            return externalId;
        }

        return Stream.of(properties.getInstallation(), properties.getRuntime(), externalId)
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
