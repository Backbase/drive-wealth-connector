package com.backbase.modelbank.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.drivewealth.reactive.clients.settlements.api.SettlementsApi;
import com.backbase.drivewealth.reactive.clients.settlements.model.Settlement;
import com.backbase.drivewealth.reactive.clients.settlements.model.SettlementRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class BulkSettlementSchedulerTest {

    @Mock
    private SettlementsApi settlementsApi;

    private BulkSettlementScheduler bulkSettlementScheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bulkSettlementScheduler = new BulkSettlementScheduler(settlementsApi);
    }

    @Test
    void testHandleSettlements() {

        // Given
        String depositCategoryId = "DEPOSIT";
        String withdrawalCategoryId = "WITHDRAWAL";
        SettlementRequest settlementRequest = new SettlementRequest();

        when(settlementsApi.listAllBulkSettlements(eq(depositCategoryId), any(), any()))
            .thenReturn(Flux.just(new Settlement().id("1"), new Settlement().id("2")));

        when(settlementsApi.listAllBulkSettlements(eq(withdrawalCategoryId), any(), any()))
            .thenReturn(Flux.just(new Settlement().id("3"), new Settlement().id("4")));

        when(settlementsApi.doSettlement("1", settlementRequest))
            .thenReturn(Mono.empty());

        when(settlementsApi.doSettlement("2", settlementRequest))
            .thenReturn(Mono.empty());

        when(settlementsApi.doSettlement("3", settlementRequest))
            .thenReturn(Mono.empty());

        when(settlementsApi.doSettlement("4", settlementRequest))
            .thenReturn(Mono.empty());

        // When
        bulkSettlementScheduler.handleSettlements();

        // Then
        verify(settlementsApi, times(2)).listAllBulkSettlements(any(), any(), any());
        verify(settlementsApi, times(4)).doSettlement(any(), any());

    }
}
