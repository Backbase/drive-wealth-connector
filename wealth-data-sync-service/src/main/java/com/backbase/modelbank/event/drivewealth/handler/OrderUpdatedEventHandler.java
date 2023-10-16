package com.backbase.modelbank.event.drivewealth.handler;

import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.handler.EventHandler;
import com.backbase.drivewealth.sqs.event.spec.v1.OrdersUpdatedEvent;
import com.backbase.modelbank.client.PortfolioIntegrationClientWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderUpdatedEventHandler implements EventHandler<OrdersUpdatedEvent> {

    private final PortfolioIntegrationClientWrapper portfolioIntegrationClientWrapper;

    @Override
    public void handle(EnvelopedEvent<OrdersUpdatedEvent> envelopedEvent) {
        var event = envelopedEvent.getEvent();
        log.info("Received Order Event of Type {}", event.getType());

        portfolioIntegrationClientWrapper.updateInProgressOrder(event.getPayload())
            .doOnSuccess(
                response -> log.info("Successfully Updated InProgress order {} with Status {} and Quantity {}",
                    event.getPayload().getId(),
                    event.getPayload().getStatus(), event.getPayload().getCumulativeQuantity()))
            .doOnError(
                e -> log.error("Failed tp Update InProgress order {} with Status {} due to error ",
                    event.getPayload().getId(),
                    event.getPayload().getStatus(), e))
            .subscribe();
    }
}
