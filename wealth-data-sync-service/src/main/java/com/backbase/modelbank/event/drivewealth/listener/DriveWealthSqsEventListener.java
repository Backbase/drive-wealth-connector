package com.backbase.modelbank.event.drivewealth.listener;

import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.modelbank.model.EventTypeRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;


@ConditionalOnProperty(
    name = "cloud.aws.sqs.listener.auto-startup", havingValue = "true"
)
@Component
@Slf4j
@RequiredArgsConstructor
public class DriveWealthSqsEventListener {

    private final EventBus eventBus;
    private final ObjectMapper objectMapper;
    private final Map<String, Class<? extends Event>> eventTypes;

    @PostConstruct
    public void onStartUp() {
        log.info("DriveWealthSqsEventListener is enabled and started successfully");
    }

    @SqsListener(value = "${cloud.aws.end-point.uri}", deletionPolicy = SqsMessageDeletionPolicy.ALWAYS)
    public void receiveMessage(String eventJson) throws JsonProcessingException {
        log.info("Message Received from DriveWealth SQS " + eventJson);

        var eventTypeName = objectMapper.readValue(eventJson, EventTypeRecord.class);
        var eventTypeClass = Optional.ofNullable(eventTypes.get(eventTypeName.type()));
        eventTypeClass.ifPresentOrElse(aClass -> notifyEventHandler(eventJson, aClass),
            () -> log.warn("There is no Handler of Event type {}", eventTypeName.type()));
    }

    @SneakyThrows
    private <T> void notifyEventHandler(String eventJson, Class<T> type) {
        EnvelopedEvent<T> eventWrapper = new EnvelopedEvent<>();
        eventWrapper.setEvent(objectMapper.readValue(eventJson, type));
        eventBus.emitEvent(eventWrapper);
    }
}
