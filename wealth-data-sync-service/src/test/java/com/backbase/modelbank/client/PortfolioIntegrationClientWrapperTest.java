package com.backbase.modelbank.client;

import static com.backbase.modelbank.util.TestFactory.getAccountArrangementItems;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.dbs.arrangement.api.service.v2.ArrangementsApi;
import com.backbase.drivewealth.reactive.clients.accounts.model.*;
import com.backbase.drivewealth.reactive.clients.money.model.GetAccountDepositsResponseInner;
import com.backbase.drivewealth.reactive.clients.money.model.GetAccountWithdrawalsResponseInner;
import com.backbase.modelbank.mapper.DriveWealthPortfolioMapper;
import com.backbase.modelbank.mapper.InstrumentHoldingsMapper;
import com.backbase.modelbank.mapper.PositionsHierarchyMapper;
import com.backbase.modelbank.model.PerformanceSummaryRecord;
import com.backbase.portfolio.api.service.integration.v1.model.AggregatePortfoliosManagerItem;
import com.backbase.portfolio.api.service.integration.v1.model.InstrumentHoldingsGetResponse;
import com.backbase.portfolio.api.service.integration.v1.model.InstrumentHoldingsItem;
import com.backbase.portfolio.api.service.integration.v1.model.InstrumentHoldingsPostRequest;
import com.backbase.portfolio.api.service.integration.v1.model.PortfolioGetResponse;
import com.backbase.portfolio.integration.api.service.v1.InstrumentHoldingsManagementApi;
import com.backbase.portfolio.integration.api.service.v1.PortfolioPositionsHierarchyManagementApi;
import com.backbase.stream.portfolio.model.PortfolioBundle;
import com.backbase.stream.portfolio.model.PortfolioPositionsHierarchy;
import com.backbase.stream.portfolio.model.Position;
import com.backbase.stream.portfolio.model.PositionBundle;
import com.backbase.stream.portfolio.service.PortfolioIntegrationService;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class PortfolioIntegrationClientWrapperTest {

    private static final String ACCOUNT_ID = "accountId";
    private static final String ACCOUNT_NUMBER = "accountNumber";
    private static final String DW_USER_ID = "b7926e27-73e3-4f92-9a33-e0762861b430.1675848110284";
    public static final String INSTRUMENT_ID = "06926627-e950-48f3-9c53-b679f61120ec";
    @Mock
    ArrangementsApi arrangementsApi;
    @Mock
    DriveWealthPortfolioMapper driveWealthPortfolioMapper;
    @Mock
    PortfolioIntegrationService portfolioIntegrationService;
    @Mock
    PositionsHierarchyMapper positionsHierarchyMapper;
    @Mock
    InstrumentHoldingsMapper instrumentHoldingsMapper;
    @Mock
    InstrumentHoldingsManagementApi instrumentHoldingsManagementApi;
    @Mock
    PortfolioPositionsHierarchyManagementApi portfolioPositionsHierarchyManagementApi;
    @InjectMocks
    PortfolioIntegrationClientWrapper portfolioIntegrationClientWrapper;

    @Test
    void testUpdatePortfolioPerformance() throws IOException {
        // Given
        when(positionsHierarchyMapper.map(any())).thenReturn(new PortfolioPositionsHierarchy());
        when(driveWealthPortfolioMapper.mapPortfolioBundle(any(), any(), any(), any()))
            .thenReturn(new PortfolioBundle());

        //When
        Mono<Void> result = portfolioIntegrationClientWrapper.updatePortfolio(getPortfolioSummaryRecord(),
            getAccountArrangementItems());

        // Then
        Assertions.assertNotNull(result);
    }

    @Test
    void testGetArrangementByCode() {
        // Given
        when(arrangementsApi.getArrangements(null, null, List.of(ACCOUNT_NUMBER)))
                .thenReturn(Mono.empty());

        // When
        var result = portfolioIntegrationClientWrapper.getArrangementByCode(ACCOUNT_NUMBER);

        // Then
        Assertions.assertNotNull(result);
        verify(arrangementsApi, times(1)).getArrangements(null, null, List.of(ACCOUNT_NUMBER));
    }

    @Test
    void testUpsertPositionBundle() {
        // Given
        var positionBundle = new PositionBundle().portfolioId("portfolioId")
                .position(new Position().externalId("externalId"));
        when(portfolioIntegrationService.upsertPosition(positionBundle))
                .thenReturn(Mono.just(positionBundle));

        // When
        var result = portfolioIntegrationClientWrapper.upsertPositionBundle(positionBundle).block();

        // Then
        Assertions.assertNotNull(result);
        verify(portfolioIntegrationService, times(1)).upsertPosition(positionBundle);
    }

    @Test
    void testUpsertHierarchy() {
        // Given
        var accountSummary = new GetAccountSummaryResponse()
                .accountSummary(new GetAccountSummaryResponseAccountSummary().accountNo("accountNo"));
        PortfolioPositionsHierarchy hierarchy = new PortfolioPositionsHierarchy();
        when(positionsHierarchyMapper.map(any(GetAccountSummaryResponse.class))).thenReturn(hierarchy);
        when(portfolioPositionsHierarchyManagementApi.putPortfolioPositionsHierarchy(any(), any()))
                .thenReturn(Mono.empty());

        // When
        portfolioIntegrationClientWrapper.upsertHierarchy(accountSummary).block();

        // Then
        verify(portfolioPositionsHierarchyManagementApi, times(1))
                .putPortfolioPositionsHierarchy(any(), any());
    }

    @Test
    void testUpdateInstrumentHoldings() {
        // Given
        var accountSummary = new GetAccountSummaryResponse()
                .accountSummary(new GetAccountSummaryResponseAccountSummary().accountNo("accountNo")
                        .cash(new GetAccountSummaryResponseAccountSummaryCash()
                                .cashBalance(BigDecimal.valueOf(1000.0d)))
                        .equity(new GetAccountSummaryResponseAccountSummaryEquity()
                                .equityValue(BigDecimal.valueOf(1000.0d))
                                .equityPositions(List.of(
                                        new GetAccountSummaryResponseAccountSummaryEquityEquityPositionsInner()
                                                .instrumentID(INSTRUMENT_ID)
                                ))));

        when(instrumentHoldingsMapper.mapInstrumentHoldings(anyString(), any(), any(), any(), any(), eq(InstrumentHoldingsPostRequest.class)))
                .thenReturn(new InstrumentHoldingsPostRequest());

        when(instrumentHoldingsManagementApi.postInstrumentHoldings(any()))
                .thenReturn(Mono.empty());

        when(instrumentHoldingsManagementApi.getInstrumentHoldings())
                .thenReturn(Mono.just(
                        new InstrumentHoldingsGetResponse()
                                .holdings(List.of(
                                        (InstrumentHoldingsItem) new InstrumentHoldingsItem()
                                                .instrumentId(INSTRUMENT_ID)
                                                .portfolioIds(List.of("code"))
                                ))
                ));

        // When
        portfolioIntegrationClientWrapper.updatePortfolioInstrumentHoldings(List.of(accountSummary), DW_USER_ID)
                .blockLast();

        // Then
        verify(instrumentHoldingsManagementApi, times(1))
                .getInstrumentHoldings();
        verify(instrumentHoldingsManagementApi, times(1))
                .postInstrumentHoldings(any());
    }

    private PerformanceSummaryRecord getPortfolioSummaryRecord() {
        return new PerformanceSummaryRecord(ACCOUNT_NUMBER, ACCOUNT_ID,
                new GetAccountSummaryResponse(),
                new GetAccountPerformanceResponse(),
                List.of(new GetAccountDepositsResponseInner()),
                List.of(new GetAccountWithdrawalsResponseInner()) );
    }

    private PortfolioGetResponse getPortfolioGetResponse() {
        return new PortfolioGetResponse()
                .addManagersItem(new AggregatePortfoliosManagerItem()
                        .name("test"));
    }
}