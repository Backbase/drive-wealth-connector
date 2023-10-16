package com.backbase.modelbank.mapper;

import com.backbase.dbs.portfolio.integration.portfolio.rest.spec.model.Granularity;
import com.backbase.dbs.portfolio.integration.portfolio.rest.spec.model.Money;
import com.backbase.dbs.portfolio.integration.portfolio.rest.spec.model.ValuationChartItem;
import com.backbase.drivewealth.clients.accounts.model.GetAccountPerformanceResponse;
import com.backbase.drivewealth.clients.accounts.model.GetAccountPerformanceResponsePerformanceInner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.backbase.modelbank.utils.PerformanceCalculationUtil.toOffsetDateTime;
import static java.util.stream.Collectors.groupingBy;

@Component
public class AccountValuationMapper {

    public List<ValuationChartItem> mapValuationChartData(GetAccountPerformanceResponse dwPerformanceItem, Granularity granularity) {
        return switch (granularity) {
            case DAILY -> enrichVariation(calculateDailyValuation(dwPerformanceItem));
            case WEEKLY -> enrichVariation(calculateWeaklyValuation(dwPerformanceItem));
            case MONTHLY -> enrichVariation(calculateMonthlyValuation(dwPerformanceItem));
            case QUARTERLY -> enrichVariation(calculateQuarterlyValuation(dwPerformanceItem));
        };
    }

    private List<ValuationChartItem> enrichVariation(List<ValuationChartItem> valuationChartItems) {
        AtomicReference<Double> bar1Value = new AtomicReference<>((double) 0);
        return valuationChartItems
                .stream()
                .map(valuationChartItem -> {
                    double bar2Value = valuationChartItem.getValue().getAmount().doubleValue();
                    double variation = bar1Value.get() == 0d ? 0d : ((bar2Value - bar1Value.get()) / bar1Value.get()) * 100d;
                    bar1Value.set(bar2Value);
                    return valuationChartItem.valuePct(BigDecimal.valueOf(variation));
                })
                .toList();
    }

    private List<ValuationChartItem> calculateDailyValuation(GetAccountPerformanceResponse performanceResponse) {
        return performanceResponse.getPerformance()
                .stream()
                .map(item -> new ValuationChartItem()
                        .dateTo(toOffsetDateTime(item.getDate()))
                        .dateFrom(toOffsetDateTime(item.getDate()))
                        .value(calculateValuation(item))
                )
                .toList();
    }

    private List<ValuationChartItem> calculateWeaklyValuation(GetAccountPerformanceResponse performanceResponse) {

        Map<Integer, List<ValuationChartItem>> grouping = performanceResponse
                .getPerformance()
                .stream()
                .filter(Objects::nonNull)
                .map(item -> new ValuationChartItem()
                        .dateTo(toOffsetDateTime(item.getDate()))
                        .dateFrom(toOffsetDateTime(item.getDate()))
                        .value(calculateValuation(item))
                )
                .collect(groupingBy(item -> item.getDateFrom().get(ChronoField.ALIGNED_WEEK_OF_YEAR)));

        return grouping
                .keySet()
                .stream()
                .map(weekPerformance -> grouping.get(weekPerformance)
                        .stream().max(Comparator.comparing(ValuationChartItem::getDateFrom)).orElse(null)
                ).toList();
    }

    private List<ValuationChartItem> calculateMonthlyValuation(GetAccountPerformanceResponse performanceResponse) {
        Map<Integer, List<ValuationChartItem>> grouping = performanceResponse
                .getPerformance()
                .stream()
                .filter(Objects::nonNull)
                .map(item -> new ValuationChartItem()
                        .dateTo(toOffsetDateTime(item.getDate()))
                        .dateFrom(toOffsetDateTime(item.getDate()))
                        .value(calculateValuation(item))
                )
                .collect(groupingBy(item -> item.getDateFrom().get(ChronoField.MONTH_OF_YEAR)));

        return grouping
                .keySet()
                .stream()
                .map(monthPerformance -> grouping.get(monthPerformance)
                        .stream().max(Comparator.comparing(ValuationChartItem::getDateFrom)).orElse(null)
                ).toList();
    }

    private List<ValuationChartItem> calculateQuarterlyValuation(GetAccountPerformanceResponse performanceResponse) {
        Map<Integer, List<ValuationChartItem>> grouping = performanceResponse
                .getPerformance()
                .stream()
                .filter(Objects::nonNull)
                .map(item -> new ValuationChartItem()
                        .dateTo(toOffsetDateTime(item.getDate()))
                        .dateFrom(toOffsetDateTime(item.getDate()))
                        .value(calculateValuation(item))
                )
                .collect(groupingBy(x -> x.getDateFrom().get(IsoFields.QUARTER_OF_YEAR)));

        return grouping
                .keySet()
                .stream()
                .map(quarterPerformance -> grouping.get(quarterPerformance)
                        .stream().max(Comparator.comparing(ValuationChartItem::getDateFrom)).orElse(null)
                ).toList();
    }

    private Money calculateValuation(GetAccountPerformanceResponsePerformanceInner dwPerformanceItem) {
        BigDecimal equity = Optional.ofNullable(dwPerformanceItem.getEquity()).orElse(BigDecimal.ZERO);
        BigDecimal cash = Optional.ofNullable(dwPerformanceItem.getCash()).orElse(BigDecimal.ZERO);
        return new Money().amount(equity.add(cash)).currency("USD");
    }


}
