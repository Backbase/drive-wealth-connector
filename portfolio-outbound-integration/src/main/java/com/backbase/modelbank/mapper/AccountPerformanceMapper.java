package com.backbase.modelbank.mapper;

import com.backbase.dbs.portfolio.integration.portfolio.rest.spec.model.CumulativePerformanceChartItem;
import com.backbase.dbs.portfolio.integration.portfolio.rest.spec.model.Money;
import com.backbase.dbs.portfolio.integration.portfolio.rest.spec.model.PortfolioSummaryAggregationGet;
import com.backbase.dbs.portfolio.integration.portfolio.rest.spec.model.PortfoliosAssetClass;
import com.backbase.drivewealth.clients.accounts.model.GetAccountPerformanceResponse;
import com.backbase.drivewealth.clients.accounts.model.GetAccountPerformanceResponsePerformanceInner;
import com.backbase.drivewealth.clients.accounts.model.GetAccountSummaryResponse;
import com.backbase.drivewealth.clients.marketdata.model.HistoricalChartResponse;
import com.backbase.drivewealth.clients.money.model.GetAccountDepositsResponseInner;
import com.backbase.drivewealth.clients.money.model.GetAccountWithdrawalsResponseInner;
import com.backbase.modelbank.utils.Constants;
import com.backbase.modelbank.utils.PerformanceCalculationUtil;
import com.backbase.portfolio.api.integration.v2.model.PortfolioGetResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static com.backbase.modelbank.utils.Constants.EUR_CURRENCY;
import static com.backbase.modelbank.utils.Constants.USD_CURRENCY;
import static com.backbase.modelbank.utils.PerformanceCalculationUtil.getMoneyValue;
import static com.backbase.modelbank.utils.PerformanceCalculationUtil.toOffsetDateTime;

@Component
public class AccountPerformanceMapper {

    public List<CumulativePerformanceChartItem> mapCumulativePerformanceChartData(List<GetAccountPerformanceResponsePerformanceInner> dwPerformanceItemList) {
        if (dwPerformanceItemList == null) {
            return List.of();
        }

        return dwPerformanceItemList
                .stream()
                .map(item -> {
                    double totalEquity = item.getEquity().doubleValue() + item.getCash().doubleValue();

                    double dayCumulativePerformance = item.getEquity().doubleValue() +
                            item.getCash().doubleValue() -
                            item.getDeposits().doubleValue() -
                            item.getWithdrawals().doubleValue();

                    var cumulativePerformancePercent = totalEquity == 0 ? 0d : dayCumulativePerformance / totalEquity;

                    return new CumulativePerformanceChartItem()
                            .value(BigDecimal.valueOf(cumulativePerformancePercent))
                            .dateTo(toOffsetDateTime(item.getDate()))
                            .dateFrom(toOffsetDateTime(item.getDate()));
                }).toList();
    }

    public PortfolioSummaryAggregationGet mapAggregationPerformance(Integer count,
                                                                    List<GetAccountSummaryResponse> todaysSummaryList,
                                                                    List<GetAccountPerformanceResponse> ytdPerformance,
                                                                    List<PortfolioGetResponse> portfolios,
                                                                    List<GetAccountDepositsResponseInner> inTotalValueList,
                                                                    List<GetAccountWithdrawalsResponseInner> outTotalValueList,
                                                                    String currencyCode) {
        // Calculate Asset Class
        double equity = todaysSummaryList.stream()
                .mapToDouble(a -> a.getAccountSummary().getEquity().getEquityValue().doubleValue())
                .sum();

        double cash = todaysSummaryList.stream()
                .mapToDouble(a -> a.getAccountSummary().getCash().getCashBalance().doubleValue())
                .sum();

        double total = equity + cash;
        double cashPct = total != 0d ? (cash / total) * 100d : 0d;
        double equityPct = total != 0d ? (equity / total) * 100d : 0d;

        // Calculate Valuation
        BigDecimal todayValue = BigDecimal.valueOf(todaysSummaryList.stream()
                .map(this::sumPortfolioValuation)
                .mapToDouble(BigDecimal::doubleValue)
                .sum());

        // Calculate Performance
        double inTotalValue = inTotalValueList.stream()
                .map(GetAccountDepositsResponseInner::getAmount)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        double outTotalValue = outTotalValueList.stream()
                .map(GetAccountWithdrawalsResponseInner::getAmount)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        double inOutNetValue = inTotalValue - outTotalValue;

        // Calculate ytd
        double janFirstValue = ytdPerformance
                .stream()
                .map(x -> x.getPerformance().stream()
                        .min(Comparator.comparing(GetAccountPerformanceResponsePerformanceInner::getDate))
                        .orElse(new GetAccountPerformanceResponsePerformanceInner()
                                .cash(BigDecimal.ZERO)
                                .date(LocalDate.now())
                                .equity(BigDecimal.ZERO)
                                .realizedDayPL(BigDecimal.ZERO)
                                .unrealizedDayPL(BigDecimal.ZERO)
                                .cumRealizedPL(BigDecimal.ZERO)
                        )
                ).mapToDouble(x -> x.getEquity().doubleValue() + x.getCash().doubleValue())
                .sum();

        double janFirstTotalDeposits = inTotalValueList.stream()
                .filter(deposit -> deposit.getTimestamp().isAfter(PerformanceCalculationUtil.get1stDayOfYear()))
                .mapToDouble(deposit -> deposit.getAmount().doubleValue())
                .sum();

        double ytd = janFirstValue + janFirstTotalDeposits == 0 ? 0 :
                (todayValue.doubleValue() - janFirstValue - janFirstTotalDeposits) / (janFirstValue + janFirstTotalDeposits / 2d);

        double ytdValue = ytdPerformance.stream()
                .mapToDouble(performance -> performance.getPerformance()
                        .stream()
                        .filter(item -> item.getDate().isAfter(PerformanceCalculationUtil.get1stDayOfYear().toLocalDate()))
                        .mapToDouble(item -> item.getUnrealizedDayPL().doubleValue() + item.getRealizedDayPL().doubleValue())
                        .sum()
                ).sum();

        // Calculate mtd
        double monthFirstValue = ytdPerformance
                .stream()
                .map(x -> x.getPerformance().stream()
                        .filter(inner -> inner.getDate().getMonthValue() == PerformanceCalculationUtil.get1stDateOfMonth().getMonthValue())
                        .min(Comparator.comparing(GetAccountPerformanceResponsePerformanceInner::getDate))
                        .orElse(new GetAccountPerformanceResponsePerformanceInner()
                                .cash(BigDecimal.ZERO)
                                .date(LocalDate.now())
                                .equity(BigDecimal.ZERO)
                                .realizedDayPL(BigDecimal.ZERO)
                                .unrealizedDayPL(BigDecimal.ZERO)
                                .cumRealizedPL(BigDecimal.ZERO)
                        )
                ).mapToDouble(x -> x.getEquity().doubleValue() + x.getCash().doubleValue())
                .sum();

        double monthFirstTotalDeposits = inTotalValueList.stream()
                .filter(deposit -> deposit.getTimestamp().getMonthValue() == PerformanceCalculationUtil.get1stDateOfMonth().getMonthValue())
                .mapToDouble(deposit -> deposit.getAmount().doubleValue())
                .sum();

        double mtd = monthFirstValue + monthFirstTotalDeposits == 0 ? 0 :
                (todayValue.doubleValue() - monthFirstValue - monthFirstTotalDeposits) / (monthFirstValue + monthFirstTotalDeposits / 2d);

        double mtdValue = ytdPerformance
                .stream()
                .mapToDouble(performance -> performance.getPerformance()
                        .stream()
                        .filter(item -> item.getDate().isAfter(PerformanceCalculationUtil.get1stDateOfMonth()))
                        .mapToDouble(item -> item.getUnrealizedDayPL().doubleValue() + item.getRealizedDayPL().doubleValue())
                        .sum())
                .sum();

        return new PortfolioSummaryAggregationGet()
                .portfoliosCount(BigDecimal.valueOf(count))
                .clientName(getClientName(portfolios))
                .valuation(convertFromUsdToEur(currencyCode, todayValue))
                .inCashTotal(convertFromUsdToEur(currencyCode, BigDecimal.valueOf(inTotalValue)))
                .outCashTotal(convertFromUsdToEur(currencyCode, BigDecimal.valueOf(outTotalValue)))
                .netCashTotal(convertFromUsdToEur(currencyCode, BigDecimal.valueOf(inOutNetValue)))
                .performanceYTD(convertFromUsdToEur(currencyCode, BigDecimal.valueOf(ytdValue)))
                .performanceMTD(convertFromUsdToEur(currencyCode, BigDecimal.valueOf(mtdValue)))
                .performanceMTDpct(BigDecimal.valueOf(mtd))
                .performanceYTDpct(BigDecimal.valueOf(ytd))
                .riskClass(getRiskClass(portfolios))
                .managers(Collections.emptyList())
                .attorneys(Collections.emptyList())
                .valuationRefreshDate(OffsetDateTime.now())
                .assetClasses(Arrays.asList(
                        new PortfoliosAssetClass()
                                .name("Equity")
                                .valuePct(BigDecimal.valueOf(equityPct)),
                        new PortfoliosAssetClass()
                                .name("Cash")
                                .valuePct(BigDecimal.valueOf(cashPct))
                ));
    }

    public List<CumulativePerformanceChartItem> mapPerformanceBenchmarkData(HistoricalChartResponse dwHistoryResponse) {
        List<CumulativePerformanceChartItem> historyPrices = new ArrayList<>();

        if (dwHistoryResponse != null && StringUtils.isNotBlank(dwHistoryResponse.getData())) {

            List<String> data = Arrays.asList(dwHistoryResponse.getData().split(Constants.PIPE_SEPARATOR));

            if (!data.isEmpty()) {
                data.forEach(bar -> {
                    var barData = bar.split(Constants.COMMA_SEPARATOR);
                    var closePrice = new BigDecimal(barData[Constants.MARKET_DATA_CLOSE_INDEX]);
                    var basePrice = new BigDecimal(data.get(0).split(Constants.COMMA_SEPARATOR)[Constants.MARKET_DATA_CLOSE_INDEX]);
                    historyPrices.add(
                            new CumulativePerformanceChartItem()
                                    .value(calculateBenchMarkPercentage(closePrice, basePrice))
                                    .dateFrom(OffsetDateTime.parse(barData[Constants.DATE_INDEX]))
                                    .dateTo(OffsetDateTime.parse(barData[Constants.DATE_INDEX])));

                });
            }
        }
        return historyPrices;
    }

    private static Money convertFromUsdToEur(String currencyCode, BigDecimal value) {
        if (Objects.nonNull(currencyCode) && currencyCode.equalsIgnoreCase(EUR_CURRENCY)) {
            return getMoneyValue(value.multiply(BigDecimal.valueOf(0.92654)), EUR_CURRENCY);
        }
        return getMoneyValue(value, USD_CURRENCY);
    }

    private BigDecimal calculateBenchMarkPercentage(BigDecimal close, BigDecimal baseValue) {
        return BigDecimal.valueOf(((close.doubleValue() - baseValue.doubleValue()) / baseValue.doubleValue()) * 100d);
    }

    private String getRiskClass(List<PortfolioGetResponse> portfolios) {
        return portfolios.stream().findFirst().map(PortfolioGetResponse::getRiskLevel).orElse("Low");
    }

    private String getClientName(List<PortfolioGetResponse> portfolios) {
        return portfolios.stream().findFirst().map(PortfolioGetResponse::getName).orElse("");
    }

    private BigDecimal sumPortfolioValuation(GetAccountSummaryResponse portfolio) {
        var equity = BigDecimal.ZERO;
        var cash = BigDecimal.ZERO;

        if (portfolio != null) {
            if (portfolio.getAccountSummary() != null && portfolio.getAccountSummary().getEquity() != null) {
                equity = portfolio.getAccountSummary().getEquity().getEquityValue();
            }

            if (portfolio.getAccountSummary() != null && portfolio.getAccountSummary().getCash() != null) {
                cash = portfolio.getAccountSummary().getCash().getCashBalance();
            }
        }
        return equity.add(cash);
    }

}
