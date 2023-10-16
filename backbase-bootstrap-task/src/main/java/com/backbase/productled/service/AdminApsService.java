package com.backbase.productled.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.backbase.dbs.accesscontrol.api.service.v3.PermissionSetApi;
import com.backbase.dbs.accesscontrol.api.service.v3.model.PresentationPermissionSetPut;
import com.backbase.dbs.accesscontrol.api.service.v3.model.PresentationPermissionSetResponseItem;
import com.backbase.dbs.accesscontrol.api.service.v3.model.PresentationUserApsIdentifiers;
import com.backbase.productled.config.properties.BootstrapDataConfigurationProperties;
import com.backbase.productled.mapper.PermissionSetMapper;
import com.backbase.productled.model.AdminApsModel;
import com.backbase.stream.LegalEntityTask;
import com.backbase.stream.legalentity.model.LegalEntity;
import com.backbase.stream.legalentity.model.ServiceAgreement;
import com.backbase.stream.worker.StreamTaskExecutor;
import com.backbase.stream.worker.exception.StreamTaskException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * @copyright (C) 2022, Backbase
 * @Version 1.0
 * @Since 19. Oct 2022 10:26 am
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminApsService implements StreamTaskExecutor<LegalEntityTask> {

    private final BootstrapDataConfigurationProperties configurationProperties;
    private final List<AdminApsModel> adminApsModelList;
    private final PermissionSetApi permissionSetApi;
    private final PermissionSetMapper permissionSetMapper;

    @Override
    public Mono<LegalEntityTask> executeTask(LegalEntityTask task) {

        if (adminApsModelList.isEmpty()) {
            log.warn("Admin APS not found in bootstrap config. Skipping creation");
            return Mono.just(task);
        }

        if (Boolean.TRUE.equals(configurationProperties.getAdminAps().isBootstrapResourcesEnabled())) {
            return bootstrapAdminAps(task);
        } else {
            log.warn("Flag 'adminAps.bootstrapManualStepsEnabled' is set to false. Skipping manual steps in Admin console");
        }

        return Mono.just(task);
    }

    private Mono<LegalEntityTask> bootstrapAdminAps(LegalEntityTask task) {
        task.info("ADMIN_APS", "assign-admin-aps", "", task.getData().toString(), (String) null, "Assign Admin APS", task.getData());

        Flux<List<Long>> existingAdminAps = Flux.fromIterable(adminApsModelList)
            .flatMap(adminApsModel -> permissionSetApi.getPermissionSet(adminApsModel.getName())
                        .filter(permissions -> Objects.equals(adminApsModel.getName(), permissions.getName()))
                        .collectList())
                .filter(Predicate.not(List::isEmpty))
            .flatMap(actual -> {
                    log.info("Admin APS Already Exist permissions: {}", actual);
                    return assignAdminPermissionsSet(task, actual.stream().map(PresentationPermissionSetResponseItem::getId).collect(Collectors.toList()));
                })
                .onErrorResume(WebClientResponseException.class, (throwable) -> {
                    task.error("ADMIN_APS", "assign-admin-aps", "failed", "adminAps.getName()", (String) null, throwable, throwable.getResponseBodyAsString(), "Unexpected Web Client Exception", new Object[0]);
                    return Mono.error(new StreamTaskException(task, throwable, "Failed to get Admin APS: " + throwable.getResponseBodyAsString()));
                });

        Flux<List<Long>> createNewAdminAps = Flux.fromIterable(adminApsModelList)
            .flatMap(
                adminApsModel -> permissionSetApi.postPermissionSet(permissionSetMapper.map(adminApsModel)))
            .flatMap(actual -> {
                    log.info("Admin APS Already has been created trying to assign to Service Agreements: {}", actual.getId());
                    return assignAdminPermissionsSet(task, Collections.singletonList(actual.getId()));
                })
                .onErrorResume(WebClientResponseException.class, (throwable) -> {
                    task.error("ADMIN_APS", "assign-admin-aps", "failed", "adminAps.getName()", (String) null, throwable, throwable.getResponseBodyAsString(), "Unexpected Web Client Exception", new Object[0]);
                    return Mono.error(new StreamTaskException(task, throwable, "Failed to create Admin APS: " + throwable.getResponseBodyAsString()));
                });

        return existingAdminAps.switchIfEmpty(createNewAdminAps)
                .then(Mono.just(task));
    }

    private Mono<List<Long>> assignAdminPermissionsSet(LegalEntityTask task, List<Long> permissionsSetIds) {

        List<String> serviceAgreementsIds = getServiceAgreementsIds(task.getLegalEntity().getSubsidiaries()).stream()
                .map(ServiceAgreement::getExternalId)
                .collect(Collectors.toList());

        return Flux.fromIterable(serviceAgreementsIds)
            .flatMap(saId -> {
                    task.info("ADMIN_APS", "assign-admin-aps", "assign", saId, saId, "Admin APS: %s assigned", saId);
                    log.info("Assigning admin APS for SA: {} permissionSetIds: {}", saId, permissionsSetIds);
                    return permissionSetApi.putPermissionSet(new PresentationPermissionSetPut()
                            .externalServiceAgreementId(saId)
                            .adminUserAps(new PresentationUserApsIdentifiers()
                                    .idIdentifiers(permissionsSetIds)));
                })
                .onErrorResume(WebClientResponseException.class, (throwable) -> {
                    task.error("ADMIN_APS", "assign-admin-aps", "failed", "adminAps.getName()", (String) null, throwable, throwable.getResponseBodyAsString(), "Unexpected Web Client Exception", new Object[0]);
                    return Mono.error(new StreamTaskException(task, throwable, "Failed to assign Admin APS: " + throwable.getResponseBodyAsString()));
                })
                .then(Mono.just(permissionsSetIds));
    }

    private List<ServiceAgreement> getServiceAgreementsIds(List<LegalEntity> legalEntities) {
        if (legalEntities == null) {
            return Collections.emptyList();
        }

        return legalEntities.stream()
                .filter(Objects::nonNull)
                .flatMap(le -> Stream.concat(getSAFromLegalEntity(le), getServiceAgreementsIds(le.getSubsidiaries()).stream()))
                .collect(Collectors.toList());
    }

    @Override
    public Mono<LegalEntityTask> rollBack(LegalEntityTask legalEntityTask) {
        return Mono.empty();
    }


    private Stream<ServiceAgreement> getSAFromLegalEntity(LegalEntity le) {
        return Stream.concat(Stream.ofNullable(le.getMasterServiceAgreement()), Stream.ofNullable(le.getCustomServiceAgreement()));
    }


}
