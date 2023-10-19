package com.backbase.modelbank.controller;

import com.backbase.modelbank.model.DataSyncRequest;
import com.backbase.modelbank.service.WealthOrchestrationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to manually trigger data-synchronization requests However, on env entry point will be via
 * AuditLoginEventListener
 */
@RestController
@Slf4j
@AllArgsConstructor
public class DataSyncController {

    private final WealthOrchestrationService wealthOrchestrationService;

    @PostMapping("/trigger/data-sync")
    public void triggerDataSync(@RequestBody DataSyncRequest request) {
        wealthOrchestrationService.triggerSynchronization(request.internalUserId(), request.username(),
            request.eventName());
    }

}

