package com.backbase.modelbank.controller;

import static com.backbase.modelbank.util.TestFactory.CREATE_ORDER;
import static org.mockito.Mockito.verify;

import com.backbase.modelbank.model.DataSyncRequest;
import com.backbase.modelbank.service.WealthOrchestrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataSyncControllerTest {

    @Mock
    private WealthOrchestrationService wealthOrchestrationService;
    @InjectMocks
    private DataSyncController dataSyncController;


    @Test
    void triggerDataSync_givenInternalId_callsWealthOrchestrationServiceWithInternalId() {

        DataSyncRequest request = new DataSyncRequest("124", "james", CREATE_ORDER);
        dataSyncController.triggerDataSync(request);
        verify(wealthOrchestrationService).triggerSynchronization(request.internalUserId(), request.username(),
            request.eventName());
    }
}
