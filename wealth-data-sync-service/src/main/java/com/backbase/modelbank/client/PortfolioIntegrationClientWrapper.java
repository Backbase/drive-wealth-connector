package com.backbase.modelbank.client;

import static java.util.Objects.requireNonNull;

import com.backbase.dbs.accesscontrol.api.service.v3.ServiceAgreementsApi;
import com.backbase.dbs.accesscontrol.api.service.v3.UsersApi;
import com.backbase.dbs.accesscontrol.api.service.v3.model.ArrangementPrivilegesGetResponseBody;
import com.backbase.dbs.accesscontrol.api.service.v3.model.ServiceAgreementItem;
import com.backbase.dbs.arrangement.api.service.v2.ArrangementsApi;
import com.backbase.dbs.arrangement.api.service.v2.model.AccountArrangementItem;
import com.backbase.dbs.arrangement.api.service.v2.model.AccountArrangementItems;
import com.backbase.dbs.user.api.service.v2.IdentityManagementApi;
import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountSummaryResponse;
import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountSummaryResponseAccountSummaryEquityEquityPositionsInner;
import com.backbase.drivewealth.reactive.clients.orders.model.Order;
import com.backbase.drivewealth.sqs.event.spec.v1.Payload______;
import com.backbase.drivewealth.sqs.event.spec.v1.Payload________;
import com.backbase.modelbank.config.DriveWealthConfigurationProperties;
import com.backbase.modelbank.mapper.DriveWealthPortfolioMapper;
import com.backbase.modelbank.mapper.InstrumentHoldingsMapper;
import com.backbase.modelbank.mapper.PortfolioOrderMapper;
import com.backbase.modelbank.mapper.PositionsHierarchyMapper;
import com.backbase.modelbank.model.PerformanceSummaryRecord;
import com.backbase.portfolio.api.service.integration.v1.model.InstrumentHoldingsGetResponse;
import com.backbase.portfolio.api.service.integration.v1.model.InstrumentHoldingsPostRequest;
import com.backbase.portfolio.api.service.integration.v1.model.InstrumentHoldingsPutRequest;
import com.backbase.portfolio.api.service.integration.v1.model.PortfolioGetResponse;
import com.backbase.portfolio.api.service.integration.v1.model.PortfolioPositionsHierarchyPutRequest;
import com.backbase.portfolio.integration.api.service.v1.InstrumentHoldingsManagementApi;
import com.backbase.portfolio.integration.api.service.v1.PortfolioPositionsHierarchyManagementApi;
import com.backbase.portfolio.integration.api.service.v1.PortfolioTradingAccountsManagementApi;
import com.backbase.portfolio.trading.integration.api.service.v1.ExternalTradeOrderApi;
import com.backbase.stream.portfolio.mapper.PortfolioMapper;
import com.backbase.stream.portfolio.model.PortfolioPositionsHierarchy;
import com.backbase.stream.portfolio.model.PositionBundle;
import com.backbase.stream.portfolio.service.PortfolioIntegrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * This class is a DBS Portfolio Integration Client API wrapper
 */
@Log4j2
@RequiredArgsConstructor
@Service
public class PortfolioIntegrationClientWrapper {


    private static final String DRIVE_WEALTH_USER_ID_ATTRIBUTE_NAME = "dwUserId";

    private final PortfolioMapper portfolioMapper = Mappers.getMapper(PortfolioMapper.class);

    private final IdentityManagementApi identityManagementApi;
    private final UsersApi accessControlUsersApi;
    private final ServiceAgreementsApi serviceAgreementApi;
    private final ArrangementsApi arrangementsApi;
    private final PortfolioPositionsHierarchyManagementApi portfolioPositionsHierarchyManagementApi;
    private final DriveWealthPortfolioMapper driveWealthPortfolioMapper;
    private final PortfolioIntegrationService portfolioIntegrationService;
    private final ExternalTradeOrderApi externalTradeOrderApi;
    private final PositionsHierarchyMapper positionsHierarchyMapper;
    private final InstrumentHoldingsManagementApi instrumentHoldingsManagementApi;
    private final PortfolioTradingAccountsManagementApi portfolioTradingAccountsManagementApi;
    private final InstrumentHoldingsMapper instrumentHoldingsMapper;
    private final DriveWealthConfigurationProperties configurationProperties;
    private final PortfolioOrderMapper portfolioOrderMapper;
    private final ObjectMapper objectMapper;

    /**
     * Update dbs portfolio details Performance and Allocation
     *
     * @param performanceSummary drive-wealth performance summary record
     * @param arrangementItems   dbs arrangement
     * @return {@link Mono}
     */
    public Mono<Void> updatePortfolio(PerformanceSummaryRecord performanceSummary,
        AccountArrangementItems arrangementItems) {

        var hierarchy = positionsHierarchyMapper.map(performanceSummary.todaySummaryList());
        var portfolioBundle = driveWealthPortfolioMapper.mapPortfolioBundle(performanceSummary, arrangementItems,
            hierarchy, configurationProperties);

        return Mono.justOrEmpty(portfolioBundle)
            .flatMapMany(portfolioIntegrationService::upsertPortfolio)
            .then(Mono.empty());
    }

    /**
     * Get dbs arrangement by portfolio code
     *
     * @param code portfolio code
     * @return {@link Mono} of {@link Mono<PortfolioGetResponse> }
     */
    public Mono<AccountArrangementItems> getArrangementByCode(String code) {
        return arrangementsApi.getArrangements(null, null, List.of(code));
    }

    /**
     * Upsert dbs position bundle
     *
     * @param positionBundle position bundle
     * @return {@link Mono} of {@link PositionBundle}
     */
    public Mono<PositionBundle> upsertPositionBundle(PositionBundle positionBundle) {
        return portfolioIntegrationService.upsertPosition(positionBundle)
                .doOnSuccess(
                        i -> log.info("Successfully synchronize Positions {} for Portfolio {}", i.getPosition().getExternalId(),
                                i.getPortfolioId()))
                .onErrorContinue((error, obj) -> log.warn("Failed to update position Bundle {}", obj, error));
    }

    /**
     * Upsert dbs position hierarchy
     *
     * @param accountSummaryResponse drive-wealth account summary
     * @return {@link Mono}
     */
    public Mono<Void> upsertHierarchy(GetAccountSummaryResponse accountSummaryResponse) {

        PortfolioPositionsHierarchy positionsHierarchy = positionsHierarchyMapper.map(accountSummaryResponse);
        String portfolioCode = requireNonNull(accountSummaryResponse.getAccountSummary()).getAccountNo();
        return Mono.justOrEmpty(positionsHierarchy)
            .flatMap(
                hierarchy -> portfolioPositionsHierarchyManagementApi
                    .putPortfolioPositionsHierarchy(portfolioCode,
                        new PortfolioPositionsHierarchyPutRequest()
                            .items(portfolioMapper.mapHierarchies(Collections.singletonList(hierarchy))))
                    .doOnSuccess(
                        i -> log.info("Successfully synchronize Portfolio Positions Hierarchy for {}", portfolioCode))
                    .onErrorContinue(
                        (error, obj) -> log.warn("Failed to update Portfolio Positions Hierarchy {}", obj, error)));
    }

    public Mono<Void> insertOrder(Order order, String instrumentId) {

        return Mono.justOrEmpty(order)
            .flatMap(
                dwOrder -> {
                    var changeOrderRequest = portfolioOrderMapper.toChangeOrderStatusRequest(dwOrder);
                    return externalTradeOrderApi.changeExternalOrderStatus(dwOrder.getId(), changeOrderRequest)
                        .onErrorResume(throwable -> {
                            if (throwable.getMessage().contains("404")) {
                                log.warn("Order {} doesn't exist for portfolio {}, creating new order", dwOrder.getId(),
                                    dwOrder.getAccountNo());
                                return externalTradeOrderApi
                                    .postExternalOrder(
                                        portfolioOrderMapper.toExternalOrderPostRequests(order, instrumentId))
                                    .doOnSuccess(
                                        i -> log.info("Successfully ingested Portfolio order {} for account {}",
                                            dwOrder.getId(), dwOrder.getAccountNo()))
                                    .onErrorResume(
                                        throwable1 ->  {
                                            log.warn("Failed to ingest Portfolio order {}", dwOrder.getId(), throwable1);
                                            return Mono.empty();
                                        });
                            }
                            return Mono.empty();
                        })
                        .then(Mono.empty());
                });
    }


    public Flux<Void> updatePortfolioInstrumentHoldings(List<GetAccountSummaryResponse> accountSummaryResponses, String dwUserId) {
        return Flux.fromIterable(accountSummaryResponses)
                .filter(summary -> Objects.nonNull(summary.getAccountSummary())
                        && Objects.nonNull(summary.getAccountSummary().getEquity())
                        && Objects.nonNull(summary.getAccountSummary().getEquity().getEquityPositions()))
                .flatMapIterable(summary -> summary.getAccountSummary().getEquity().getEquityPositions())
                .groupBy(GetAccountSummaryResponseAccountSummaryEquityEquityPositionsInner::getInstrumentID)
                .flatMap(Flux::collectList)
                .flatMap(positionList -> {
                    String instrumentSymbol = positionList.stream().findFirst().orElseThrow().getSymbol();
                    String instrumentHoldingExternalId = instrumentSymbol + "_" + dwUserId.substring(0, 30);

                    List<String> portfoliosCodes = accountSummaryResponses.stream()
                            .map(summary -> summary.getAccountSummary().getAccountNo())
                            .toList();
                    double totalEquity = accountSummaryResponses.stream()
                            .mapToDouble(summary -> summary.getAccountSummary().getEquity().getEquityValue().doubleValue())
                            .sum();

                    double totalCash = accountSummaryResponses.stream()
                            .mapToDouble(summary -> summary.getAccountSummary().getCash().getCashBalance().doubleValue())
                            .sum();

                    return instrumentHoldingsManagementApi.getInstrumentHoldings()
                            .flatMapIterable(InstrumentHoldingsGetResponse::getHoldings)
                            .filter(instrumentHoldingsItem -> Objects.equals(instrumentHoldingsItem.getExternalId(), instrumentHoldingExternalId))
                            .flatMap(instrumentHoldingsItem -> instrumentHoldingsManagementApi
                                    .putInstrumentHoldings(instrumentHoldingExternalId,
                                            instrumentHoldingsMapper.mapInstrumentHoldings(
                                                    instrumentHoldingExternalId,
                                                    portfoliosCodes,
                                                    totalEquity,
                                                    totalCash,
                                                    positionList,
                                                    InstrumentHoldingsPutRequest.class)

                                    )
                            )
                            .switchIfEmpty(instrumentHoldingsManagementApi
                                    .postInstrumentHoldings(instrumentHoldingsMapper.mapInstrumentHoldings(
                                            instrumentHoldingExternalId,
                                            portfoliosCodes,
                                            totalEquity,
                                            totalCash,
                                            positionList,
                                            InstrumentHoldingsPostRequest.class)
                                    )
                            );
                });
    }

    public Mono<Void> updateTradingAccount(String portfolioCode) {
        var tradingAccountsPutRequest = driveWealthPortfolioMapper.mapTradingAccountsPutRequest(portfolioCode);
        return Mono.justOrEmpty(portfolioCode)
            .flatMap(
                orderPostRequest -> portfolioTradingAccountsManagementApi.putTradingAccounts(portfolioCode,
                        tradingAccountsPutRequest)
                    .doOnSuccess(
                        i -> log.info("Successfully updated Portfolio Trading Account order for portfolio {}",
                            portfolioCode))
                    .onErrorContinue(
                        (error, obj) -> log.warn("Failed to update Portfolio Trading Account order for portfolio {}",
                            obj, error)))
            .then(Mono.empty());
    }

    /**
     * Retrieves the list of Account Id for given LegalEntity Id
     *
     * @param username External Legal entity Id/ username
     * @return {@link Mono} of {@link List} of {@link String} List of DW Account Ids
     */
    public Mono<List<String>> getAccountsByArrangement(String internalId, String username) {
        log.info("Retrieving Arrangements for User {}", username);
        return serviceAgreementApi.getServiceAgreementExternalId("sa_" + username)
            .map(ServiceAgreementItem::getId)
            .flatMapMany(saId -> accessControlUsersApi.getArrangementPrivileges(internalId, saId, "Manage Portfolio",
                "Portfolio", "view"))
            .map(ArrangementPrivilegesGetResponseBody::getArrangementId)
            .flatMap(arrangementId -> arrangementsApi.getArrangementById(arrangementId, false))
            .mapNotNull(AccountArrangementItem::getBBAN)
            .collectList();

    }

    /**
     * Retrieves DW User Id from DBS internal User Id
     *
     * @param internalId Internal DBS User Id
     * @return {@link Mono} of DW user Ids
     */
    public Mono<String> getUserIdFromIdentity(String internalId) {
        log.info("Retrieving DW user id for User {}", internalId);
        return identityManagementApi.getIdentity(internalId)
            .map(getIdentity -> Objects.requireNonNull(getIdentity.getAttributes())
                .getOrDefault(DRIVE_WEALTH_USER_ID_ATTRIBUTE_NAME, ""))
            .filter(Objects::nonNull);
    }

    public Mono<Void> updateCompletedOrder(Payload______ payload) {
        var changeOrderRequest = portfolioOrderMapper.toChangeOrderStatusRequest(payload);
        return externalTradeOrderApi.changeExternalOrderStatus(payload.getId(), changeOrderRequest);
    }

    public Mono<Void> updateInProgressOrder(Payload________ payload) {
        var changeOrderRequest = portfolioOrderMapper.toChangeOrderStatusRequest(portfolioOrderMapper.map(payload));
        return externalTradeOrderApi.changeExternalOrderStatus(payload.getId(), changeOrderRequest);
    }
}
