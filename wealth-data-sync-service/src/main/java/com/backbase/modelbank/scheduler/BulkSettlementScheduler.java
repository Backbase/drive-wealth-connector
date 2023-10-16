package com.backbase.modelbank.scheduler;

import com.backbase.drivewealth.reactive.clients.settlements.api.SettlementsApi;
import com.backbase.drivewealth.reactive.clients.settlements.model.Settlement;
import com.backbase.drivewealth.reactive.clients.settlements.model.SettlementRequest;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Log4j2
public class BulkSettlementScheduler {

    private static final String DEPOSIT_CATEGORY = "DEPOSIT";
    private static final String WITHDRAWAL_CATEGORY = "WITHDRAWAL";
    private final SettlementsApi settlementsApi;

    @Scheduled(cron = "${drive-wealth.bulk-settlement.interval-in-cron:0 * 8 * * *}")
    public void handleSettlements() {

        log.info("Starting DEPOSIT Bulk Payment Settlement");

        settlementsApi.listAllBulkSettlements(DEPOSIT_CATEGORY, OffsetDateTime.now().minusYears(1),
                OffsetDateTime.now())
            .mapNotNull(Settlement::getId)
            .doOnNext(settlementId -> log.info("Received DEPOSIT settlement id {}", settlementId))
            .flatMap(settlementId -> settlementsApi.doSettlement(settlementId, new SettlementRequest()))
            .collectList()
            .doOnSuccess(settlement -> log.info("Successfully completed DEPOSIT Bulk Payment Settlement"))
            .block();

        log.info("Starting WITHDRAWAL Bulk Payment Settlement");

        settlementsApi.listAllBulkSettlements(WITHDRAWAL_CATEGORY, OffsetDateTime.now().minusYears(1),
                OffsetDateTime.now())
            .mapNotNull(Settlement::getId)
            .doOnNext(settlementId -> log.info("Received WITHDRAWAL settlement id {}", settlementId))
            .flatMap(settlementId -> settlementsApi.doSettlement(settlementId, new SettlementRequest()))
            .collectList()
            .doOnSuccess(settlement -> log.info("Successfully completed WITHDRAWAL Bulk Payment Settlement"))
            .block();
    }
}
