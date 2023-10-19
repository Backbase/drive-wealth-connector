package com.backbase.productled.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.backbase.dbs.message.rest.serviceapi.spec.v2.ServiceEmployeeApi;
import com.backbase.dbs.message.rest.serviceapi.spec.v2.model.TopicsPostRequestBody;
import com.backbase.dbs.user.manager.rest.serviceapi.spec.v2.IdentityManagementApi;
import com.backbase.dbs.user.manager.rest.serviceapi.spec.v2.model.IdentityListedItem;
import com.backbase.stream.worker.exception.StreamTaskException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final ServiceEmployeeApi serviceEmployeeApi;
    private final IdentityManagementApi identityManagementApi;

    public void ingestTopics(List<String> topics) {

        var identities = identityManagementApi.getIdentities(null, null, null, 1000)
            .filter(getIdentities -> !CollectionUtils.isEmpty(getIdentities.getIdentities()))
            .map(getIdentities -> getIdentities.getIdentities().stream().map(IdentityListedItem::getId)
                .collect(Collectors.toList()))
            .doOnError(ex -> log.error("Failed to retrieve Identities ", ex))
            .onErrorResume(e -> Mono.just(Collections.emptyList()))
            .block();

        if (identities == null) {
            log.warn("There are no identity users, So skipping creation of topics {}", topics);
            return;
        }

        Flux.fromIterable(topics)
            .filter(topicName -> {
                    if (serviceEmployeeApi.getTopics(null)
                        .filter(responseBody -> responseBody.getName().equals(topicName)).blockFirst() != null) {
                        log.warn(
                            "Topic `{}` already exist, So skipping creation. Update not possible due to unavailability of feature from product",
                            topicName);
                        return false;
                    }
                    return true;
                }
            )
            .flatMap(topicName -> {
                log.info("Creating message topic with name '{}' with {} subscribers", topicName, identities.size());
                createTopic(topicName, identities);
                return Mono.empty();
            })
            .doOnError(StreamTaskException.class, throwable -> {
                log.error("Failed to ingest message topic: ", throwable);
                throwable.getTask().logSummary();
            })
            .collectList()
            .block();
    }

    /**
     * Creating topics in blocking way, since DBS return when sending concurrent calls
     *
     * @param topicName  Name of topic
     * @param identities List of subscribers
     */
    private void createTopic(String topicName, List<String> identities) {

        serviceEmployeeApi.postTopics(new TopicsPostRequestBody().name(topicName).subscribers(identities))
            .doOnError(ex -> log.error("Failed to create topic with name {}", topicName, ex))
            .onErrorResume(e -> Mono.empty())
            .block();
    }

}
