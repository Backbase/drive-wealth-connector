package com.backbase.productled.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.backbase.cxp.contentservice.api.v2.ContentManagementServiceToServiceApi;
import com.backbase.cxp.contentservice.api.v2.ContentUploadServiceToServiceApi;
import com.backbase.cxp.contentservice.api.v2.RepositoryManagementServiceToServiceApi;
import com.backbase.cxp.contentservice.model.v2.AntivirusScanTrigger;
import com.backbase.cxp.contentservice.model.v2.Document;
import com.backbase.cxp.contentservice.model.v2.DocumentToSave;
import com.backbase.cxp.contentservice.model.v2.QueryRequest;
import com.backbase.cxp.contentservice.model.v2.Repository;
import com.backbase.productled.model.ContentRepositoryItem;
import com.backbase.productled.model.ContentTemplate;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * @copyright (C) 2022, Backbase
 * @Version 1.0
 * @Since 01. Nov 2022 4:46 pm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentRepositoryService {

    private final ContentUploadServiceToServiceApi contentUploadApi;
    private final RepositoryManagementServiceToServiceApi repositoryManagementApi;
    private final ContentManagementServiceToServiceApi contentManagementApi;

    public Mono<String> bootstrapContentRepository(ContentRepositoryItem contentRepositoryItem) {
        Mono<String> existingRepository =
                repositoryManagementApi.getRepositoryServiceToService(contentRepositoryItem.getRepositoryId())
                        .thenMany(contentManagementApi.queryContentServiceToService(
                                new QueryRequest()
                                        .repositories(Collections.singletonList(contentRepositoryItem.getRepositoryId()))
                                        .paths(Collections.singletonList("/"))
                        ))
                        .filter(repo -> Objects.equals(repo.getRepositoryId(), contentRepositoryItem.getRepositoryId()))
                        .doOnNext(repo -> log.debug("Repository already exist [{}], Repository: [{}]", contentRepositoryItem.getRepositoryId(), repo))
                        .mapNotNull(Document::getId)
                        .flatMap(repoInternalId -> this.createFolders(repoInternalId, contentRepositoryItem))
                        .thenMany(this.createTemplates(contentRepositoryItem))
                        .then(Mono.just(contentRepositoryItem.getRepositoryId()))
                        .onErrorResume(WebClientResponseException.class, (throwable) -> {
                            if (throwable.getStatusCode() == HttpStatus.NOT_FOUND) {
                                log.debug("Repository doesn't exist RepositoryId: {}", contentRepositoryItem.getRepositoryId());
                                return Mono.empty();
                            }
                            log.error("Error retrieving repository: {}", throwable.getResponseBodyAsString());
                            return Mono.error(new RuntimeException("Error retrieving RepositoryId: " + contentRepositoryItem.getRepositoryId()));
                        });

        Mono<String> newRepository = repositoryManagementApi.createRepositoriesServiceToService(
                        Collections.singletonList(new Repository()
                                .repositoryId(contentRepositoryItem.getRepositoryId())
                                .name(contentRepositoryItem.getRepositoryId())
                                .description("Repository for " + contentRepositoryItem.getRepositoryId())
                                .implementation("DB")
                                .versioningEnabled(Boolean.TRUE)
                                .isPrivate(Boolean.FALSE)
                                .antivirusScanTrigger(AntivirusScanTrigger.NONE))
                )
                .thenMany(contentManagementApi.queryContentServiceToService(
                                new QueryRequest()
                                        .repositories(Collections.singletonList(contentRepositoryItem.getRepositoryId()))
                                        .paths(Collections.singletonList("/")))
                )
                .filter(repo -> Objects.equals(repo.getRepositoryId(), contentRepositoryItem.getRepositoryId()))
                .doOnNext(repo -> log.debug("Repository created [{}], Repository: [{}]", contentRepositoryItem.getRepositoryId(), repo))
                .mapNotNull(Document::getId)
                .flatMap(repoInternalId -> this.createFolders(repoInternalId, contentRepositoryItem))
                .thenMany(this.createTemplates(contentRepositoryItem))
                .then(Mono.just(contentRepositoryItem.getRepositoryId()))
                .onErrorResume(WebClientResponseException.class, (throwable) -> {
                    log.error("Error creating new repository: {}", throwable.getResponseBodyAsString());
                    return Mono.error(new RuntimeException("Error Creating new RepositoryId: " + contentRepositoryItem.getRepositoryId()));
                });

        return existingRepository.switchIfEmpty(newRepository);
    }

    private Flux<Document> createFolders(String repositoryInternalId, ContentRepositoryItem contentRepositoryItem) {

        Set<String> folderSet = contentRepositoryItem.getTemplates().stream()
                .map(ContentTemplate::getPathInRepository)
                .collect(Collectors.toSet());

        return Flux.fromIterable(folderSet)
            .flatMap(path -> {
                    log.debug("Trying to create folder: {}", path);
                    return contentManagementApi.saveContentServiceToService(Boolean.TRUE, Collections.singletonList(
                            new DocumentToSave()
                                    .id(repositoryInternalId)
                                    .repositoryId(contentRepositoryItem.getRepositoryId())
                                    .type("cmis:folder")
                                    .path(path)
                    ));
                })
                .onErrorResume(WebClientResponseException.class, (throwable) -> {
                    log.error("Error create folder: {}", throwable.getResponseBodyAsString());
                    return Mono.error(new RuntimeException("Error create folder in RepositoryId: " + contentRepositoryItem.getRepositoryId()));
                });
    }

    private Flux<String> createTemplates(ContentRepositoryItem contentRepositoryItem) {
        return Flux.fromIterable(contentRepositoryItem.getTemplates())
            .flatMap(template -> {
                    log.debug("Trying to upload template : {}", template.getFile().getName());
                    return contentUploadApi.handleContentUploadOnService2ServiceCall(
                            contentRepositoryItem.getRepositoryId(),
                            template.getPathInRepository(),
                            template.getFile(),
                            template.getFile().getName(),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                    );
                })
                .mapNotNull(Document::getRepositoryId)
                .onErrorResume(WebClientResponseException.class, (throwable) -> {
                    log.error("Error while uploading template: {}", throwable.getResponseBodyAsString());
                    return Mono.error(new RuntimeException("Error upload template in RepositoryId: " + contentRepositoryItem.getRepositoryId()));
                });
    }

}
