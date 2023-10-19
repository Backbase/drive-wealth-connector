package com.backbase.modelbank.mapper;

import com.backbase.dbs.portfolio.integration.portfolio.rest.spec.model.CumulativePerformanceChartItem;
import com.backbase.dbs.portfolio.integration.portfolio.rest.spec.model.PortfolioSummaryAggregationGet;
import com.backbase.dbs.portfolio.integration.portfolio.rest.spec.model.PortfoliosAssetClass;
import com.backbase.drivewealth.clients.accounts.model.*;
import com.backbase.drivewealth.clients.money.model.GetAccountDepositsResponseInner;
import com.backbase.drivewealth.clients.money.model.GetAccountWithdrawalsResponseInner;
import com.backbase.modelbank.util.TestFactory;
import com.backbase.portfolio.api.integration.v2.model.PortfolioGetResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountPerformanceMapperTest {
    AccountPerformanceMapper accountPerformanceMapper = new AccountPerformanceMapper();
    LocalDate dateTime = LocalDate.of(2020, 1, 1);
    LocalDate datetimeOneDay = dateTime.plusDays(1);

    @Test
    void testMapCumulativePerformanceChartData() {
        List<CumulativePerformanceChartItem> result = accountPerformanceMapper.mapCumulativePerformanceChartData(
                List.of(
                        getPerformance(dateTime, new BigDecimal(1000), new BigDecimal(0)),
                        getPerformance(datetimeOneDay, new BigDecimal(1100), new BigDecimal(0)),
                        getPerformance(datetimeOneDay, new BigDecimal(0), new BigDecimal(0))
                ));
        Assertions.assertEquals(List.of(
                new CumulativePerformanceChartItem()
                        .dateFrom(OffsetDateTime.of(dateTime, LocalTime.MIN, ZoneOffset.UTC))
                        .dateTo(OffsetDateTime.of(dateTime, LocalTime.MIN, ZoneOffset.UTC))
                        .value(BigDecimal.valueOf(0.9d)),
                new CumulativePerformanceChartItem()
                        .dateFrom(OffsetDateTime.of(datetimeOneDay, LocalTime.MIN, ZoneOffset.UTC))
                        .dateTo(OffsetDateTime.of(datetimeOneDay, LocalTime.MIN, ZoneOffset.UTC))
                        .value(BigDecimal.valueOf(0.9090909090909091d)),
                new CumulativePerformanceChartItem()
                        .dateFrom(OffsetDateTime.of(datetimeOneDay, LocalTime.MIN, ZoneOffset.UTC))
                        .dateTo(OffsetDateTime.of(datetimeOneDay, LocalTime.MIN, ZoneOffset.UTC))
                        .value(BigDecimal.valueOf(0.0d))
        ), result);
    }

    @Test
    void testMapCumulativePerformanceChartData_withNullList() {
        List<CumulativePerformanceChartItem> result = accountPerformanceMapper.mapCumulativePerformanceChartData(null);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testMapAggregationPerformance() {
        PortfolioSummaryAggregationGet result = accountPerformanceMapper.mapAggregationPerformance(1,
                List.of(new GetAccountSummaryResponse()
                        .accountSummary(
                                new GetAccountSummaryResponseAccountSummary()
                                        .cash(new GetAccountSummaryResponseAccountSummaryCash()
                                                .cashAvailableForTrade(BigDecimal.valueOf(100))
                                                .cashBalance(BigDecimal.valueOf(100)))
                                        .equity(new GetAccountSummaryResponseAccountSummaryEquity()
                                                .equityValue(BigDecimal.valueOf(400)))
                        )),
                List.of(new GetAccountPerformanceResponse()
                        .performance(List.of(
                                        new GetAccountPerformanceResponsePerformanceInner()
                                                .date(LocalDate.now())
                                                .realizedDayPL(BigDecimal.valueOf(100f))
                                                .unrealizedDayPL(BigDecimal.valueOf(100f))
                                                .cash(BigDecimal.valueOf(100f))
                                                .equity(BigDecimal.valueOf(300f))
                                )
                        )
                ),
                List.of(new PortfolioGetResponse().name("Name")),
                List.of(new GetAccountDepositsResponseInner().timestamp(OffsetDateTime.now()).amount(BigDecimal.valueOf(100f))),
                List.of(new GetAccountWithdrawalsResponseInner().timestamp(OffsetDateTime.now()).amount(BigDecimal.valueOf(0f))), null);
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(100d), result.getNetCashTotal().getAmount());
        assertEquals(BigDecimal.valueOf(500d), result.getValuation().getAmount());

        assertEquals(BigDecimal.valueOf(200d), result.getPerformanceMTD().getAmount());
        assertEquals(BigDecimal.valueOf(0.0d), result.getPerformanceMTDpct());
        assertEquals(BigDecimal.valueOf(200d), result.getPerformanceYTD().getAmount());
        assertEquals(BigDecimal.valueOf(0.0d), result.getPerformanceYTDpct());
        assertEquals(BigDecimal.valueOf(500d), result.getValuation().getAmount());


        assertTrue(List.of(new PortfoliosAssetClass()
                        .name("Equity")
                        .valuePct(BigDecimal.valueOf(80d)),
                new PortfoliosAssetClass()
                        .name("Cash")
                        .valuePct(BigDecimal.valueOf(20d))
        ).containsAll(result.getAssetClasses()));


    }

    @Test
    void testMapAggregationPerformanceToEur() {
        PortfolioSummaryAggregationGet result = accountPerformanceMapper.mapAggregationPerformance(1,
                List.of(new GetAccountSummaryResponse()
                        .accountSummary(
                                new GetAccountSummaryResponseAccountSummary()
                                        .cash(new GetAccountSummaryResponseAccountSummaryCash()
                                                .cashAvailableForTrade(BigDecimal.valueOf(100))
                                                .cashBalance(BigDecimal.valueOf(100)))
                                        .equity(new GetAccountSummaryResponseAccountSummaryEquity()
                                                .equityValue(BigDecimal.valueOf(400)))
                        )),
                List.of(new GetAccountPerformanceResponse()
                        .performance(List.of(
                                        new GetAccountPerformanceResponsePerformanceInner()
                                                .date(LocalDate.now())
                                                .realizedDayPL(BigDecimal.valueOf(100))
                                                .unrealizedDayPL(BigDecimal.valueOf(100))
                                                .cash(BigDecimal.valueOf(100))
                                                .equity(BigDecimal.valueOf(300))
                                )
                        )
                ),
                List.of(new PortfolioGetResponse().name("Name")),
                List.of(new GetAccountDepositsResponseInner().timestamp(OffsetDateTime.now()).amount(BigDecimal.valueOf(1000f))),
                List.of(new GetAccountWithdrawalsResponseInner().timestamp(OffsetDateTime.now()).amount(BigDecimal.valueOf(0f))), "EUR");
        assertNotNull(result);
        assertEquals("EUR", result.getValuation().getCurrency());
        assertEquals("926.540000", result.getNetCashTotal().getAmount().toString());
        assertEquals("463.270000", result.getValuation().getAmount().toString());

        assertEquals("185.308000", result.getPerformanceMTD().getAmount().toString());
        assertEquals("-1.0", result.getPerformanceMTDpct().toString());
        assertEquals("185.308000", result.getPerformanceYTD().getAmount().toString());
        assertEquals("-1.0", result.getPerformanceYTDpct().toString());
        assertEquals("463.270000", result.getValuation().getAmount().toString());


        assertTrue(List.of(new PortfoliosAssetClass()
                        .name("Equity")
                        .valuePct(BigDecimal.valueOf(80d)),
                new PortfoliosAssetClass()
                        .name("Cash")
                        .valuePct(BigDecimal.valueOf(20d))
        ).containsAll(result.getAssetClasses()));


    }

    @Test
    void testMapPerformanceBenchmarkData() throws IOException {
        List<CumulativePerformanceChartItem> result = accountPerformanceMapper.mapPerformanceBenchmarkData(TestFactory.getHistoricalChartResponseMock());

        assertNotNull(result);
        assertEquals(252, result.size());

    }


    private GetAccountPerformanceResponsePerformanceInner getPerformance(LocalDate dateTime, BigDecimal equity, BigDecimal cache) {
        return new GetAccountPerformanceResponsePerformanceInner()
                .date(dateTime)
                .equity(equity)
                .cash(cache)
                .deposits(BigDecimal.valueOf(100d))
                .withdrawals(BigDecimal.valueOf(0d));
    }

}