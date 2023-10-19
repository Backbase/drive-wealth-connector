package com.backbase.modelbank.event.drivewealth.handler;

import static com.backbase.modelbank.util.TestFactory.getOrdersCompletedEvent;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.drivewealth.sqs.event.spec.v1.OrdersCompletedEvent;
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

class OrderCompletedEventHandlerTest {

    @Mock
    private PortfolioIntegrationClientWrapper portfolioIntegrationClientWrapper;

    @InjectMocks
    private OrderCompletedEventHandler orderCompletedEventHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handle_shouldUpdateOrderStatus() throws IOException {
        // Given
        var event = getOrdersCompletedEvent();
        var envelopedEvent = new EnvelopedEvent<OrdersCompletedEvent>();
        envelopedEvent.setEvent(event);

        Mockito.when(portfolioIntegrationClientWrapper.updateCompletedOrder(any())).thenReturn(Mono.empty());

        // When
        orderCompletedEventHandler.handle(envelopedEvent);

        // Then
        verify(portfolioIntegrationClientWrapper).updateCompletedOrder(event.getPayload());
    }

    @Test
    void handle_shouldUpdateOrderStatusWhenError() throws IOException {
        // Given
        var event = getOrdersCompletedEvent();
        var envelopedEvent = new EnvelopedEvent<OrdersCompletedEvent>();
        envelopedEvent.setEvent(event);

        Mockito.when(portfolioIntegrationClientWrapper.updateCompletedOrder(any()))
            .thenReturn(Mono.error(new BadRequestException("bad request")));

        // When
        orderCompletedEventHandler.handle(envelopedEvent);

        // Then
        verify(portfolioIntegrationClientWrapper).updateCompletedOrder(event.getPayload());
    }
}
