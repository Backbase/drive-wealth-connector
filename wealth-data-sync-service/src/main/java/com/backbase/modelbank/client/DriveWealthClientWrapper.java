package com.backbase.modelbank.client;

import static java.time.OffsetDateTime.now;

import com.backbase.drivewealth.reactive.clients.accounts.api.AccountsApi;
import com.backbase.drivewealth.reactive.clients.accounts.model.Account;
import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountResponse;
import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountSummaryResponse;
import com.backbase.drivewealth.reactive.clients.instrument.api.InstrumentApi;
import com.backbase.drivewealth.reactive.clients.instrument.model.InstrumentDetail;
import com.backbase.drivewealth.reactive.clients.money.api.DepositsApi;
import com.backbase.drivewealth.reactive.clients.money.api.WithdrawalsApi;
import com.backbase.drivewealth.reactive.clients.orders.api.OrdersApi;
import com.backbase.drivewealth.reactive.clients.orders.model.Order;
import com.backbase.drivewealth.reactive.clients.orders.model.OrdersHistoryOrdersInner;
import com.backbase.drivewealth.reactive.clients.transactions.api.TransactionsApi;
import com.backbase.drivewealth.reactive.clients.transactions.model.Transaction;
import com.backbase.modelbank.config.DriveWealthConfigurationProperties;
import com.backbase.modelbank.config.DriveWealthConfigurationProperties.Event;
import com.backbase.modelbank.mapper.DriveWealthPortfolioMapper;
import com.backbase.modelbank.model.PerformanceSummaryRecord;
import com.backbase.modelbank.utils.PerformanceCalculationUtil;
import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * This class is a Drive-Wealth Client API wrapper
 */
@Log4j2

@RequiredArgsConstructor
@Service
public class DriveWealthClientWrapper {

    private final AccountsApi accountsApi;
    private final DepositsApi depositsApi;
    private final WithdrawalsApi withdrawalsApi;
    private final TransactionsApi transactionsApi;
    private final InstrumentApi instrumentApi;
    private final OrdersApi ordersApi;
    private final DriveWealthConfigurationProperties properties;
    private final DriveWealthPortfolioMapper driveWealthPortfolioMapper;

    /**
     * Get user accounts by drive
     *
     * @param userId Drive-wealth userId
     * @return {@link Flux } of {@link Account}
     */
    public Mono<List<Account>> getAccountsByUserId(String userId) {
        return accountsApi.getAccountByUserId(userId)
            .filter(Objects::nonNull)
            .collectList()
            .filter(list -> !CollectionUtils.isEmpty(list));
    }

    /**
     * Get account performance
     *
     * @param accountId Drive-wealth accountId
     * @return {@link Mono} of {@link PerformanceSummaryRecord}
     */
    public Mono<PerformanceSummaryRecord> getPerformanceSummary(String accountId) {

        return Mono.zip(accountsApi.getAccountSummaryById(accountId),
                        accountsApi.getAccountPerformanceById(accountId, null, PerformanceCalculationUtil.get1stDayOfYear(), now()),
                        depositsApi.listAccountDeposits(accountId).collectList(),
                        withdrawalsApi.listAccountWithdrawals(accountId).collectList())
                .map(driveWealthPortfolioMapper::mapPerformanceSummary);
    }

    /**
     * Get instrument details
     *
     * @param instrumentId drive-wealth instrumentId
     * @return {@link Mono} of {@link InstrumentDetail}
     */
    public Mono<InstrumentDetail> getInstruments(Optional<String> instrumentId) {
        return instrumentId.isPresent() ? instrumentApi.getInstrumentById(instrumentId.get()) : Mono.empty();
    }

    /**
     * Get transaction
     *
     * @param accountId drive-wealth accountId
     * @param eventName DBS Audit Event Name
     * @return {@link Flux} of {@link Transaction}
     */
    public Flux<Transaction> getTransactionsByAccount(String accountId, String eventName) {
        return transactionsApi.getTransactionsByAccount(accountId, now().minusDays(getTransactionRefreshPeriod(eventName)),
            now().plusDays(1), null, null, null);
    }

    /**
     * Get account summary
     *
     * @param accountId drive-wealth accountId
     * @return {@link Mono} of {@link GetAccountSummaryResponse}
     */
    public Mono<GetAccountSummaryResponse> getAccountSummary(String accountId) {
        return accountsApi.getAccountSummaryById(accountId);
    }

    /**
     * Get user accounts by drive
     *
     * @param accountIds Drive-wealth account ids
     * @return {@link Flux } of {@link Account}
     */
    public Mono<List<Account>> getAccountByIds(List<String> accountIds) {
        return Flux.fromIterable(accountIds)
            .flatMap(accountsApi::getAccount)
            .mapNotNull(GetAccountResponse::getAccount)
            .collectList()
            .filter(list -> !CollectionUtils.isEmpty(list));
    }

    /**
     * @param accountId DW Account Id
     * @param eventName DBS audit event name
     * @return @return {@link Flux } of {@link Order}
     */
    public Flux<Order> getOrdersByAccountId(String accountId, String eventName) {

        return Flux.fromIterable(IntStream.range(0, getOrderRefreshWindow(eventName))
                .boxed()
                .map(i -> Pair.of(OffsetDateTime.now().minusMonths(i).with(TemporalAdjusters.firstDayOfMonth()),
                    OffsetDateTime.now().minusMonths(i).with(TemporalAdjusters.lastDayOfMonth())))
                .toList())
            .flatMap(pair -> ordersApi.getOrderHistory(accountId, pair.getFirst(), pair.getSecond(), null,
                null,
                null))
            .flatMapIterable(ordersHistory -> {
                if (ordersHistory != null && CollectionUtils.isNotEmpty(ordersHistory.getOrders())) {
                    return ordersHistory.getOrders();
                }
                return Collections.emptyList();
            })
            .mapNotNull(OrdersHistoryOrdersInner::getOrderID)
            .flatMap(ordersApi::getOrderById);
    }

    public Mono<InstrumentDetail> getInstrument(String symbol) {
        return instrumentApi.getInstrumentBySymbol(symbol);
    }

    private int getTransactionRefreshPeriod(String eventName) {
        return properties.getTransactions().getDaysRefreshWindow().stream()
            .filter(event -> eventName.equals(event.eventName())).findFirst().orElseGet(() -> new Event("default", 1))
            .period();
    }

    private int getOrderRefreshWindow(String eventName) {
        return properties.getOrders().getMonthsRefreshWindow().stream()
            .filter(event -> eventName.equals(event.eventName())).findFirst().orElseGet(() -> new Event("default", 1))
            .period();
    }
}
