package com.backbase.modelbank.service;

import com.backbase.drivewealth.reactive.clients.accounts.model.Account;
import com.backbase.drivewealth.reactive.clients.instrument.model.InstrumentDetail;
import com.backbase.modelbank.client.DriveWealthClientWrapper;
import com.backbase.modelbank.client.PortfolioIntegrationClientWrapper;
import com.backbase.modelbank.mapper.PositionBundleMapper;
import com.backbase.modelbank.model.PortfolioSyncTask;
import com.backbase.stream.portfolio.model.PositionTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;


/**
 * This service intedned to orchestrate the synchronization process to sync all below feature
 * <pre> {@code
 * - Portfolio Details
 * - Portfolio performance
 * - Portfolio allocation
 * - Portfolio hierarchy
 * - Portfolio Positions
 * - Portfolio Transactions
 * - Portfolio Orders
 * }</pre>
 */
@Log4j2
@RequiredArgsConstructor
@Service
public class WealthOrchestrationService {

    private static final String ACCOUNT_STATUS_OPEN = "OPEN";

    private final DriveWealthClientWrapper driveWealthClientWrapper;
    private final PortfolioIntegrationClientWrapper portfolioIntegrationClientWrapper;

    private final PositionBundleMapper positionBundleMapper;

    /**
     * Start synchronization process
     *
     * @param internalId dbs userInternalId
     * @param eventName DBS Audit Event Name
     */
    public void triggerSynchronization(String internalId, String username, String eventName) {
        log.info("Portfolios synchronization started");

        portfolioIntegrationClientWrapper.getUserIdFromIdentity(internalId)
            .flatMap(driveWealthClientWrapper::getAccountsByUserId)
            .switchIfEmpty(Mono.defer(() -> portfolioIntegrationClientWrapper.getAccountsByArrangement(internalId, username)
                .flatMap(driveWealthClientWrapper::getAccountByIds)
            ))
            .map(response -> response.stream()
                // Filter Open status account only
                .filter(account -> ACCOUNT_STATUS_OPEN.equalsIgnoreCase(
                    Objects.requireNonNull(account.getStatus()).getName()))
                .toList()
            )
            .filter(list -> !CollectionUtils.isEmpty(list))
            .map(accounts -> new PortfolioSyncTask(accounts, accounts.stream().findFirst().orElseThrow().getUserID(), eventName))
            .flatMap(this::triggerPortfolioDetailsSync)
            .flatMap(this::triggerPortfolioOrdersSync)
            .flatMap(this::triggerPortfolioTradingAccountSync)
            .flatMap(this::triggerPortfolioPositionBundleSync)
            .flatMap(this::triggerPortfolioInstrumentHoldingsSync)
            .onErrorContinue((ex, object) -> log.error("Error on triggering sync process, [{}] ", object, ex))
            .doOnSuccess(task -> {
                    if (Objects.nonNull(task) && !CollectionUtils.isEmpty(task.accounts())) {
                        log.info("Portfolios [{}] synchronization Finished Successfully",
                            task.accounts().stream().map(Account::getAccountNo).toList());
                    } else {
                        log.info("No Portfolio found for User {}", username);
                    }
                }
            )
            .subscribe();
    }

    private Mono<PortfolioSyncTask> triggerPortfolioDetailsSync(PortfolioSyncTask task) {
        log.debug("Trying to sync portfolios detail");
        return Flux.fromIterable(task.accounts())
            .flatMap(account -> Mono.zip(
                driveWealthClientWrapper.getPerformanceSummary(account.getId()),
                portfolioIntegrationClientWrapper.getArrangementByCode(account.getAccountNo())))
            .flatMap(
                tuple2 -> portfolioIntegrationClientWrapper.updatePortfolio(tuple2.getT1(), tuple2.getT2()))
            .then(Mono.just(task));
    }

    private Mono<PortfolioSyncTask> triggerPortfolioTradingAccountSync(PortfolioSyncTask task) {
        log.debug("Trying to sync portfolio trading account");
        return Flux.fromIterable(task.accounts())
            .mapNotNull(Account::getAccountNo)
            .flatMap(portfolioIntegrationClientWrapper::updateTradingAccount)
            .then(Mono.just(task));

    }

    private Mono<PortfolioSyncTask> triggerPortfolioPositionBundleSync(PortfolioSyncTask task) {
        log.info("Trying to sync Portfolio Positions");
        return Flux.fromIterable(task.accounts())
            .mapNotNull(Account::getId)
            .flatMap(accountId -> Mono.zip(
                driveWealthClientWrapper.getAccountSummary(accountId),
                driveWealthClientWrapper.getTransactionsByAccount(accountId, task.eventName()).collectList(),
                driveWealthClientWrapper.getOrdersByAccountId(accountId, task.eventName()).collectList())
            )
            .map(positionBundleMapper::mapPositionBundle)
            .flatMap(Flux::fromIterable)
            .flatMap(positionBundle -> Mono.zip(
                Mono.just(positionBundle),
                driveWealthClientWrapper.getInstruments(positionBundle.getTransactions().stream()
                    .map(PositionTransaction::getOfficialCode)
                    .findFirst())))
            .map(positionBundleMapper::updateInstrumentDetailsInPositionBundle)
            .flatMap(portfolioIntegrationClientWrapper::upsertPositionBundle)
            .then(Mono.just(task));

    }

    private Mono<PortfolioSyncTask> triggerPortfolioInstrumentHoldingsSync(PortfolioSyncTask task) {
        log.debug("Trying to sync portfolios instrument holdings");
        return Flux.fromIterable(task.accounts())
            .flatMap(account -> driveWealthClientWrapper.getAccountSummary(account.getId()))
            .collectList()
            .flatMapMany(
                accountSummary -> portfolioIntegrationClientWrapper.updatePortfolioInstrumentHoldings(accountSummary,
                    task.dwUserId()))
            .then(Mono.just(task));
    }

    private Mono<PortfolioSyncTask> triggerPortfolioOrdersSync(PortfolioSyncTask task) {

        log.info("Trying to sync Portfolio Orders");

        return Flux.fromIterable(task.accounts())
            .mapNotNull(Account::getId)
            .flatMap(accountId -> driveWealthClientWrapper.getOrdersByAccountId(accountId, task.eventName()))
            .flatMap(order -> Mono.zip(
                    Mono.just(order),
                    driveWealthClientWrapper.getInstrument(order.getSymbol()).mapNotNull(InstrumentDetail::getId)))
            .flatMap(tuple -> portfolioIntegrationClientWrapper.insertOrder(tuple.getT1(), tuple.getT2()))
            .then(Mono.just(task));
    }

}
