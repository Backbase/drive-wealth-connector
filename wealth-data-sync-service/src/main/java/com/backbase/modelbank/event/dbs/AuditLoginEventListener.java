package com.backbase.modelbank.event.dbs;

import com.backbase.audit.persistence.event.spec.v1.AuditMessagesCreatedEvent;
import com.backbase.audit.rest.spec.v3.model.AuditMessage;
import com.backbase.audit.rest.spec.v3.model.AuditMessage.Status;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.handler.EventHandler;
import com.backbase.modelbank.service.WealthOrchestrationService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Log4j2
@Component
public class AuditLoginEventListener implements EventHandler<AuditMessagesCreatedEvent> {

    private final WealthOrchestrationService wealthOrchestrationService;

    /**
     * Listen to Audit event and call Orchestration service
     *
     * @param envelopedEvent Audit event containing user data
     */
    @Override
    public void handle(EnvelopedEvent<AuditMessagesCreatedEvent> envelopedEvent) {
        log.debug("auditMessagesEvent: {}", envelopedEvent.getEvent());

        AuditMessage auditMessage = envelopedEvent.getEvent().getAuditMessages().get(0);

        if (isLoginEvent(auditMessage) || isOrderCreateEvent(auditMessage)) {

            String username = auditMessage.getUsername();
            String internalId = auditMessage.getUserId();

            wealthOrchestrationService.triggerSynchronization(internalId, username,
                StringUtils.joinWith(StringUtils.SPACE, auditMessage.getEventAction(), auditMessage.getObjectType()));

            log.info("Processed login event for user: {}", username);
        }
    }

    private static boolean isOrderCreateEvent(AuditMessage auditMessage) {
        return auditMessage.getEventCategory().equalsIgnoreCase("Portfolios")
            && auditMessage.getObjectType().equalsIgnoreCase("Order")
            && auditMessage.getEventAction().equalsIgnoreCase("Create")
            && (auditMessage.getStatus() == Status.SUCCESSFUL ||
            auditMessage.getStatus() == Status.FAILED);
    }

    private static boolean isLoginEvent(AuditMessage auditMessage) {
        return auditMessage.getEventCategory().equalsIgnoreCase("Identity and Access")
            && auditMessage.getObjectType().equalsIgnoreCase("Authentication")
            && auditMessage.getEventAction().equalsIgnoreCase("Attempt Login")
            && auditMessage.getStatus() == Status.SUCCESSFUL;
    }
}
