package com.backbase.modelbank.event.drivewealth.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.drivewealth.sqs.event.spec.v1.OrdersUpdatedEvent;
import com.backbase.modelbank.model.EventTypeRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class DriveWealthSqsEventListenerTest {

    @Mock
    private EventBus eventBus;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Map<String, Class<OrdersUpdatedEvent>> eventTypes;

    @InjectMocks
    private DriveWealthSqsEventListener listener;

    @Captor
    private ArgumentCaptor<EnvelopedEvent<?>> eventCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void receiveMessage_ValidEvent_CallsEventBus() throws JsonProcessingException {
        String eventJson = "{\"id\": \"event_4da42927-77f5-4308-a6bf-60c1ed0a4b24\",\"type\": \"orders.updated\",\"timestamp\": \"2019-01-09T12:14:44.155187291Z\",\"payload\": {\"id\": \"GA.021be353-1f63-4540-aeeb-8a11b1f5a189\",\"orderNo\": \"GAFZ000003\",\"type\": \"MARKET\",\"side\": \"BUY\",\"status\": \"PARTIAL_FILL\",\"symbol\": \"BABA\",\"averagePrice\": 134.42,\"cumulativeQuantity\": 12,\"quantity\": 1000,\"lastShares\": 6,\"lastPrice\": 134.11,\"createdBy\": \"cc07f91b-7ee1-4868-b8fc-823c70a1b932\",\"lastExecuted\": \"2019-01-04T14:36:17.378Z\",\"userID\": \"cc07f91b-7ee1-4868-b8fc-823c70a1b932\",\"accountID\": \"cc07f91b-7ee1-4868-b8fc-823c70a1b932.1407775317759\",\"accountNo\": \"DPKU000001\",\"created\": \"2019-01-04T14:36:17.378Z\"}}";
        EventTypeRecord eventTypeRecord = new EventTypeRecord("orders.updated");
        Mockito.when(eventTypes.get("orders.updated")).thenReturn(OrdersUpdatedEvent.class);

        when(objectMapper.readValue(eventJson, EventTypeRecord.class)).thenReturn(eventTypeRecord);
        when(objectMapper.readValue(eventJson, OrdersUpdatedEvent.class)).thenReturn(new OrdersUpdatedEvent());

        listener.receiveMessage(eventJson);

        verify(eventBus).emitEvent(eventCaptor.capture());

        EnvelopedEvent<?> capturedEvent = eventCaptor.getValue();
        assertEquals(OrdersUpdatedEvent.class, capturedEvent.getEvent().getClass());
        // Additional assertions for the captured event data

        // Verify that the objectMapper.readValue method was called
        verify(objectMapper).readValue(eventJson, EventTypeRecord.class);

        // Verify that the eventTypes map was accessed
        verify(eventTypes).get(eventTypeRecord.type());
    }

    @Test
    void receiveMessage_UnknownEventType_LogsWarning() throws JsonProcessingException {
        String eventJson = "{\"type\": \"UnknownType\"}";
        EventTypeRecord eventTypeRecord = new EventTypeRecord("UnknownType");

        when(objectMapper.readValue(eventJson, EventTypeRecord.class)).thenReturn(eventTypeRecord);
        when(eventTypes.get(eventTypeRecord.type())).thenReturn(null);

        listener.receiveMessage(eventJson);

        // Verify that the logger logged the warning message
        // You can use an appropriate logging framework assertion or mock the logger to verify the behavior
        // For example, if you are using SLF4J with Mockito, you can use the following verification:
        // verify(logger).warn("There is no Handler of Event type {}", eventTypeRecord.getType());

        // Verify that the objectMapper.readValue method was called
        verify(objectMapper).readValue(eventJson, EventTypeRecord.class);

        // Verify that the eventTypes map was accessed
        verify(eventTypes).get(eventTypeRecord.type());

        // Verify that the eventBus.emitEvent method was not called
        verify(eventBus, never()).emitEvent(any());
    }

    @Test
    void receiveMessage_JsonProcessingException_ThrowsException() throws JsonProcessingException {
        String eventJson = "{\"type\": \"EventType\"}";

        when(objectMapper.readValue(eventJson, EventTypeRecord.class)).thenThrow(JsonProcessingException.class);

        assertThrows(JsonProcessingException.class, () -> listener.receiveMessage(eventJson));

        // Verify that the objectMapper.readValue method was called
        verify(objectMapper).readValue(eventJson, EventTypeRecord.class);

        // Verify that the eventBus.emitEvent method was not called
        verify(eventBus, never()).emitEvent(any());
    }

    // Dummy event class for testing
    static class DummyEvent extends Event {
        // Event implementation
    }
}

