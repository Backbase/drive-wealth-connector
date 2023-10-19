package com.backbase.modelbank.event.drivewealth.handler;

import static com.backbase.modelbank.util.TestFactory.getOrdersUpdatedEvent;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.drivewealth.sqs.event.spec.v1.OrdersUpdatedEvent;
import com.backbase.modelbank.client.PortfolioIntegrationClientWrapper;
import com.github.dockerjava.api.exception.BadRequestException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

class OrderUpdatedEventHandlerTest {

    @Mock
    private PortfolioIntegrationClientWrapper portfolioIntegrationClientWrapper;

    @InjectMocks
    private OrderUpdatedEventHandler orderUpdatedEventHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handle_shouldUpdateOrderStatus() throws IOException {
        // Given
        var event = getOrdersUpdatedEvent();
        var envelopedEvent = new EnvelopedEvent<OrdersUpdatedEvent>();
        envelopedEvent.setEvent(event);

        Mockito.when(portfolioIntegrationClientWrapper.updateInProgressOrder(any())).thenReturn(Mono.empty());

        // When
        orderUpdatedEventHandler.handle(envelopedEvent);

        // Then
        verify(portfolioIntegrationClientWrapper).updateInProgressOrder(event.getPayload());
    }

    @Test
    void handle_shouldUpdateOrderStatusWhenError() throws IOException {
        // Given
        var event = getOrdersUpdatedEvent();
        var envelopedEvent = new EnvelopedEvent<OrdersUpdatedEvent>();
        envelopedEvent.setEvent(event);

        Mockito.when(portfolioIntegrationClientWrapper.updateInProgressOrder(any()))
            .thenReturn(Mono.error(new BadRequestException("bad request")));

        // When
        orderUpdatedEventHandler.handle(envelopedEvent);

        // Then
        verify(portfolioIntegrationClientWrapper).updateInProgressOrder(event.getPayload());
    }
}