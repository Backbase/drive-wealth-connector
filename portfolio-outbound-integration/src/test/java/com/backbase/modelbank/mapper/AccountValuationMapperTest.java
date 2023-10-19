package com.backbase.modelbank.mapper;

import com.backbase.dbs.portfolio.integration.portfolio.rest.spec.model.Granularity;
import com.backbase.dbs.portfolio.integration.portfolio.rest.spec.model.Money;
import com.backbase.dbs.portfolio.integration.portfolio.rest.spec.model.ValuationChartItem;
import com.backbase.drivewealth.clients.accounts.model.GetAccountPerformanceResponse;
import com.backbase.drivewealth.clients.accounts.model.GetAccountPerformanceResponsePerformanceInner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AccountValuationMapperTest {
    AccountValuationMapper accountValuationMapper = new AccountValuationMapper();
    LocalDate dateTime = LocalDate.of(2020, 1, 1);
    LocalDate dateTimeWeak = dateTime.plusWeeks(1);
    LocalDate dateTimeMonth = dateTime.plusMonths(1);
    LocalDate dateTimeQuarter = dateTime.plusMonths(3);

    @ParameterizedTest
    @EnumSource(Granularity.class)
    void testMapValuationChartData(Granularity granularity) {


        List<ValuationChartItem> result = accountValuationMapper.mapValuationChartData(new GetAccountPerformanceResponse()
                .performance(List.of(
                        getPerformance(dateTime),
                        getPerformance(dateTimeWeak),
                        getPerformance(dateTimeMonth),
                        getPerformance(dateTimeQuarter)
                )), granularity);

        assertNotNull(result);

        switch (granularity) {
            case DAILY -> testCalculateDailyValuation(result);
            case WEEKLY -> testCalculateWeaklyValuation(result);
            case MONTHLY -> testCalculateMonthlyValuation(result);
            case QUARTERLY -> testCalculateQuarterlyValuation(result);
        }
    }

    void testCalculateDailyValuation(List<ValuationChartItem> result) {
        Assertions.assertNotNull(result);

        Assertions.assertTrue(List.of(
                getValuationChartItem(dateTime),
                getValuationChartItem(dateTimeWeak),
                getValuationChartItem(dateTimeMonth),
                getValuationChartItem(dateTimeQuarter)
        ).containsAll(result));
    }

    void testCalculateWeaklyValuation(List<ValuationChartItem> result) {
        Assertions.assertNotNull(result);

        Assertions.assertTrue(List.of(
                getValuationChartItem(dateTime),
                getValuationChartItem(dateTimeWeak),
                getValuationChartItem(dateTimeMonth),
                getValuationChartItem(dateTimeQuarter)
        ).containsAll(result));
    }

    void testCalculateMonthlyValuation(List<ValuationChartItem> result) {
        Assertions.assertNotNull(result);

        Assertions.assertTrue(List.of(
                getValuationChartItem(dateTimeWeak),
                getValuationChartItem(dateTimeMonth),
                getValuationChartItem(dateTimeQuarter)
        ).containsAll(result));
    }


    void testCalculateQuarterlyValuation(List<ValuationChartItem> result) {
        Assertions.assertNotNull(result);

        Assertions.assertTrue(List.of(
                getValuationChartItem(dateTimeMonth),
                getValuationChartItem(dateTimeQuarter)
        ).containsAll(result));
    }

    private ValuationChartItem getValuationChartItem(LocalDate dateTime) {
        return new ValuationChartItem()
                .dateFrom(OffsetDateTime.of(dateTime, LocalTime.MIN, ZoneOffset.UTC))
                .dateTo(OffsetDateTime.of(dateTime, LocalTime.MIN, ZoneOffset.UTC))
                .valuePct(BigDecimal.valueOf(0d))
                .value(new Money()
                        .currency("USD")
                        .amount(BigDecimal.valueOf(2)));
    }

    private GetAccountPerformanceResponsePerformanceInner getPerformance(LocalDate dateTime) {
        return
                new GetAccountPerformanceResponsePerformanceInner()
                        .date(dateTime)
                        .equity(BigDecimal.ONE)
                        .cash(BigDecimal.ONE);
    }
}
