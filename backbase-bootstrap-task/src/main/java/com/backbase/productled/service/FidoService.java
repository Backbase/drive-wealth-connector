package com.backbase.productled.service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.backbase.dbs.identity.fido.service.model.v2.CreateApplicationRequestBody;
import com.backbase.dbs.identity.fido.service.model.v2.UpdateApplicationRequestBody;
import com.backbase.identity.fido.service.api.v2.ApplicationControllerApi;
import com.backbase.productled.model.FidoApplication;
import com.backbase.stream.worker.exception.StreamTaskException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class FidoService {

    private final ApplicationControllerApi applicationControllerApi;

    public FidoService(@Qualifier("applicationControllerApi") ApplicationControllerApi applicationControllerApi) {
        this.applicationControllerApi = applicationControllerApi;
    }

    public void bootstrapFidoFacetIds(List<FidoApplication> applications) {

        applications = applications.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(applications)) {
            return;
        }

        var fidoApplications = applicationControllerApi.getApplications()
            .doOnError(ex -> log.error("Failed to bootstrapping Fido Facet Ids", ex))
            .onErrorResume(e -> Mono.empty())
            .block();
        log.debug("Existing Fido Application on env {}", fidoApplications);

        log.debug("Going to Ingest Fido Facets {}", applications);
        Flux.fromIterable(applications)
            .flatMap(fidoApplication -> {
                Optional.ofNullable(fidoApplications != null ? fidoApplications.getApplications() : null)
                    .ifPresentOrElse(applicationDtos -> {
                        var matchedApp = applicationDtos.stream()
                            .filter(applicationDto -> Objects.equals(applicationDto.getAppKey(), fidoApplication.getAppKey()))
                            .findFirst();
                        if (matchedApp.isPresent()) {
                            updateFacetId(matchedApp.get().getId(), fidoApplication);
                        } else {
                            createApplication(fidoApplication).run();
                        }
                    }, createApplication(fidoApplication));
                return Mono.empty();
            })
            .doOnError(StreamTaskException.class, throwable -> {
                log.error("Failed to ingest Facet Ids: ", throwable);
                throwable.getTask().logSummary();
            })
            .collectList()
            .block();
    }

    private Runnable createApplication(FidoApplication fidoApplication) {
        return () -> {
            var createApplicationRequestBody = new CreateApplicationRequestBody();
            createApplicationRequestBody.setAppId(fidoApplication.getAppId());
            createApplicationRequestBody.setAppKey(fidoApplication.getAppKey());
            createApplicationRequestBody.setTrustedFacetIds(fidoApplication.getTrustedFacetIds());
            applicationControllerApi.createApplication(createApplicationRequestBody)
                .doOnError(ex -> log.error("Failed to create Fido application with appKey {}",
                    createApplicationRequestBody.getAppKey(), ex))
                .onErrorResume(e -> Mono.empty())
                .block();
            log.info("Created Fido application with appKey {}", createApplicationRequestBody.getAppKey());
        };
    }

    private void updateFacetId(Integer id, FidoApplication fidoApplication) {
        var updateApplicationRequestBody = new UpdateApplicationRequestBody();
        updateApplicationRequestBody.appId(fidoApplication.getAppId()).appKey(fidoApplication.getAppKey())
            .trustedFacetIds(fidoApplication.getTrustedFacetIds());
        applicationControllerApi.updateApplication(id, updateApplicationRequestBody)
            .doOnError(ex -> log.error("Failed to update Fido application with Id {} and appKey {}", id,
                updateApplicationRequestBody.getAppKey(), ex))
            .onErrorResume(e -> Mono.empty())
            .block();
        log.info("Updated Fido application with Id {} and appKey {}", id, updateApplicationRequestBody.getAppKey());
    }

}
