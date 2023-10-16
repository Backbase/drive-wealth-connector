package com.backbase.modelbank.event.drivewealth.handler;

import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.handler.EventHandler;
import com.backbase.drivewealth.sqs.event.spec.v1.OrdersCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderCreatedEventHandler implements EventHandler<OrdersCreatedEvent> {

    @Override
    public void handle(EnvelopedEvent<OrdersCreatedEvent> envelopedEvent) {
        var event = envelopedEvent.getEvent();
        log.info("Received Order Event of Type {}", event.getType());
    }
}
