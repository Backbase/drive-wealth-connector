package com.backbase.productled.service;

import com.backbase.stream.LegalEntityTask;
import com.backbase.stream.legalentity.model.LegalEntity;
import com.backbase.stream.worker.StreamTaskExecutor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Service
public class LegalEntityFilterService implements StreamTaskExecutor<LegalEntityTask> {

    public static final String SKIP_LEGAL_ENTITY_SAGA = "SKIP_LEGAL_ENTITY_SAGA";
    public static final String TRUE = "true";

    @NewSpan
    @Override
    public Mono<LegalEntityTask> executeTask(LegalEntityTask legalEntityTask) {

        LegalEntity legalEntity = legalEntityTask.getData();
        processSubsidiaries(legalEntity.getExternalId(), legalEntity.getSubsidiaries());
        return Mono.just(legalEntityTask);
    }

    private void processSubsidiaries(String legalEntityExternalId, List<LegalEntity> subsidiaries) {
        if (Optional.ofNullable(subsidiaries).isPresent()) {
            subsidiaries.removeIf(x -> x.getAdditions() != null
                && TRUE.equalsIgnoreCase(x.getAdditions().get(SKIP_LEGAL_ENTITY_SAGA)));
            subsidiaries.stream()
                .filter(Objects::nonNull)
                .forEach(legalEntity -> {
                    processSubsidiaries(legalEntity.getExternalId(), legalEntity.getSubsidiaries());
                });
            log.debug("Subsidiaries for Legal Entity with External ID {} were processed", legalEntityExternalId);
        } else {
            log.debug("No subsidiaries for Legal Entity with External ID {} were found", legalEntityExternalId);
        }

    }

    @Override
    public Mono<LegalEntityTask> rollBack(LegalEntityTask legalEntityTask) {
        return null;
    }
}