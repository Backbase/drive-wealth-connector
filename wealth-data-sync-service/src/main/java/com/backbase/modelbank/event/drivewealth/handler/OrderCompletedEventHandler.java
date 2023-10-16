package com.backbase.modelbank.event.drivewealth.handler;

import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.handler.EventHandler;
import com.backbase.drivewealth.sqs.event.spec.v1.OrdersCompletedEvent;
import com.backbase.modelbank.client.PortfolioIntegrationClientWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderCompletedEventHandler implements EventHandler<OrdersCompletedEvent> {

    private final PortfolioIntegrationClientWrapper portfolioIntegrationClientWrapper;


    @Override
    public void handle(EnvelopedEvent<OrdersCompletedEvent> envelopedEvent) {
        var event = envelopedEvent.getEvent();
        log.info("Received Order Event of Type {}", event.getType());

        portfolioIntegrationClientWrapper.updateCompletedOrder(event.getPayload())
            .doOnSuccess(
                response -> log.info("Successfully Updated Completed order {} with Status {}", event.getPayload().getId(),
                    event.getPayload().getStatus()))
            .doOnError(
                e -> log.error("Failed tp Update Completed order {} with Status {} due to error ", event.getPayload().getId(),
                    event.getPayload().getStatus(), e))
            .subscribe();
    }

}