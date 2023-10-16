package com.backbase.modelbank.event.dbs;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.backbase.audit.persistence.event.spec.v1.AuditMessagesCreatedEvent;
import com.backbase.audit.rest.spec.v3.model.AuditMessage;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.modelbank.service.WealthOrchestrationService;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditLoginEventListenerTest {

    public static final String USER_ID = "userId";
    private static final String USERNAME = "username";
    @Mock
    private WealthOrchestrationService orchestrationService;

    @InjectMocks
    private AuditLoginEventListener auditLoginEventListener;

    @Test
    void testAuditLoginEvent() {
        AuditMessage auditMessage = buildAuditMessage()
                .withEventMetaData(Map.of("Realm", "retail"));
        verifyLoginEventCallsOrchestrationService(auditMessage, 1);

    }

    @Test
    void testAuditLoginEventWhenEventIsNotIdentity() {
        AuditMessage auditMessage = buildAuditMessage()
                .withEventCategory("Access");
        verifyLoginEventCallsOrchestrationService(auditMessage, 0);

    }

    @Test
    void testAuditLoginEventWhenObjectTypeIsNotAuthentication() {
        AuditMessage auditMessage = buildAuditMessage()
                .withObjectType("Authentication1");
        verifyLoginEventCallsOrchestrationService(auditMessage, 0);

    }

    @Test
    void testAuditLoginEventWhenActionIsNotAttemptLogin() {
        AuditMessage auditMessage = buildAuditMessage()
                .withEventAction("Attempt Logout");
        verifyLoginEventCallsOrchestrationService(auditMessage, 0);

    }

    @Test
    void testAuditLoginEventWhenStatusIsNotSuccessful() {
        AuditMessage auditMessage = buildAuditMessage()
                .withStatus(AuditMessage.Status.FAILED);
        verifyLoginEventCallsOrchestrationService(auditMessage, 0);

    }


    private static AuditMessage buildAuditMessage() {
        return new AuditMessage()
                .withEventCategory("Identity and Access")
                .withObjectType("Authentication")
                .withEventAction("Attempt Login")
                .withUsername(USERNAME)
                .withUserId(USER_ID)
                .withStatus(AuditMessage.Status.SUCCESSFUL);
    }

    private void verifyLoginEventCallsOrchestrationService(AuditMessage auditMessage, int expectedNumberOfInvocations) {
        // Given
        AuditMessagesCreatedEvent auditMessagesCreatedEvent = new AuditMessagesCreatedEvent()
                .withAuditMessages(List.of(auditMessage));

        EnvelopedEvent<AuditMessagesCreatedEvent> event = new EnvelopedEvent<>();
        event.setEvent(auditMessagesCreatedEvent);

        // When
        auditLoginEventListener.handle(event);

        // Then
        verify(orchestrationService, times(expectedNumberOfInvocations))
                .triggerSynchronization(USER_ID, USERNAME, StringUtils.joinWith(StringUtils.SPACE, auditMessage.getEventAction(),
                    auditMessage.getObjectType()));
    }
}