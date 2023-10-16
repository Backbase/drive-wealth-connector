package com.backbase.modelbank.mapper;

import com.backbase.dbs.arrangement.api.service.v2.model.AccountArrangementItems;
import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountPerformanceResponse;
import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountPerformanceResponsePerformanceInner;
import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountSummaryResponse;
import com.backbase.drivewealth.reactive.clients.money.model.GetAccountDepositsResponseInner;
import com.backbase.drivewealth.reactive.clients.money.model.GetAccountWithdrawalsResponseInner;
import com.backbase.modelbank.config.DriveWealthConfigurationProperties;
import com.backbase.modelbank.model.PerformanceSummaryRecord;
import com.backbase.modelbank.utils.Constants;
import com.backbase.modelbank.utils.PerformanceCalculationUtil;
import com.backbase.portfolio.api.service.integration.v1.model.*;
import com.backbase.stream.portfolio.model.Money;
import com.backbase.stream.portfolio.model.*;
import org.mapstruct.Mapper;
import reactor.util.function.Tuple4;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static org.springframework.util.CollectionUtils.isEmpty;


/**
 * Drive wealth portfolio mapper
 */
@Mapper(componentModel = "spring")
public interface DriveWealthPortfolioMapper {

    String REGION_CLASSIFIER_NAME = "North America";
    String COUNTRY_CLASSIFIER_NAME = "US";
    String EQUITY_CLASSIFIER_NAME = "Equity";
    String CASH_CLASSIFIER_NAME = "Cash";
    String RISK_LEVEL = "riskLevel";
    String DEFAULT_RISK_LEVEL = "Low";

    default PortfolioBundle mapPortfolioBundle(PerformanceSummaryRecord performanceSummary,
                                               AccountArrangementItems arrangementItems, PortfolioPositionsHierarchy hierarchy,
                                               DriveWealthConfigurationProperties configurationProperties) {
        return new PortfolioBundle()
                .portfolio(mapPortfolioPerformance(performanceSummary, arrangementItems, configurationProperties))
                .allocations(mapPortfolioAllocation(performanceSummary.todaySummaryList()))
                .hierarchies(List.of(hierarchy));
    }

    default PerformanceSummaryRecord mapPerformanceSummary(Tuple4<GetAccountSummaryResponse, GetAccountPerformanceResponse, List<GetAccountDepositsResponseInner>, List<GetAccountWithdrawalsResponseInner>> tuple) {

        GetAccountSummaryResponse todaySummaryList = tuple.getT1();
        GetAccountPerformanceResponse ytdPerformance = tuple.getT2();
        List<GetAccountDepositsResponseInner> depositsList = tuple.getT3();
        List<GetAccountWithdrawalsResponseInner> withdrawalList = tuple.getT4();

        Objects.requireNonNull(ytdPerformance.getPerformance(), "DriveWealth YTD Performance list is null");

        return new PerformanceSummaryRecord(
                requireNonNull(todaySummaryList.getAccountSummary()).getAccountNo(),
                todaySummaryList.getAccountSummary().getAccountID(),
                todaySummaryList,
                ytdPerformance,
                depositsList,
                withdrawalList);
    }

    /**
     * Map portfolio performance
     *
     * @param performanceSummaryRecord Custom Object storing performance summary
     * @param arrangementItems         DBS arrangement response
     * @param configurationProperties  DW configuration properties
     * @return Portfolio
     */
    default Portfolio mapPortfolioPerformance(PerformanceSummaryRecord performanceSummaryRecord, AccountArrangementItems arrangementItems, DriveWealthConfigurationProperties configurationProperties) {

        GetAccountSummaryResponse todaySummary = performanceSummaryRecord.todaySummaryList();
        GetAccountPerformanceResponse ytdPerformance = performanceSummaryRecord.ytdPerformance();
        List<GetAccountDepositsResponseInner> depositsList = performanceSummaryRecord.depositsList();

        double todayValue = todaySummary.getAccountSummary().getEquity().getEquityValue().doubleValue() +
                todaySummary.getAccountSummary().getCash().getCashBalance().doubleValue();

        // Calculate ytd
        GetAccountPerformanceResponsePerformanceInner janFirstPerformance = ytdPerformance.getPerformance()
                .stream()
                .min(Comparator.comparing(GetAccountPerformanceResponsePerformanceInner::getDate))
                .orElse(new GetAccountPerformanceResponsePerformanceInner()
                        .cash(BigDecimal.ZERO)
                        .date(LocalDate.now())
                        .equity(BigDecimal.ZERO)
                        .realizedDayPL(BigDecimal.ZERO)
                        .unrealizedDayPL(BigDecimal.ZERO)
                        .cumRealizedPL(BigDecimal.ZERO)
                );

        double janFirstValue = janFirstPerformance.getEquity().doubleValue() + janFirstPerformance.getCash().doubleValue();
        double janFirstTotalDeposits = depositsList.stream()
                .filter(deposit -> deposit.getTimestamp().isAfter(PerformanceCalculationUtil.get1stDayOfYear()))
                .mapToDouble(deposit -> deposit.getAmount().doubleValue())
                .sum();
        double ytd = janFirstValue + janFirstTotalDeposits == 0 ? 0 :
                (todayValue - janFirstValue - janFirstTotalDeposits) / (janFirstValue + janFirstTotalDeposits / 2d);

        double ytdValue = ytdPerformance.getPerformance()
                .stream()
                .filter(performance -> performance.getDate().isAfter(PerformanceCalculationUtil.get1stDayOfYear().toLocalDate()))
                .mapToDouble(item -> item.getUnrealizedDayPL().doubleValue() + item.getRealizedDayPL().doubleValue())
                .sum();

        // Calculate mtd
        GetAccountPerformanceResponsePerformanceInner monthFirstPerformance = ytdPerformance.getPerformance()
                .stream()
                .filter(Objects::nonNull)
                .filter(performance -> performance.getDate().getMonthValue() == PerformanceCalculationUtil.get1stDateOfMonth().getMonthValue())
                .min(Comparator.comparing(GetAccountPerformanceResponsePerformanceInner::getDate))
                .orElse(new GetAccountPerformanceResponsePerformanceInner()
                        .cash(BigDecimal.ZERO)
                        .date(LocalDate.now())
                        .equity(BigDecimal.ZERO)
                        .realizedDayPL(BigDecimal.ZERO)
                        .unrealizedDayPL(BigDecimal.ZERO)
                        .cumRealizedPL(BigDecimal.ZERO)
                );

        double monthFirstValue = monthFirstPerformance.getEquity().doubleValue() + monthFirstPerformance.getCash().doubleValue();
        double monthFirstTotalDeposits = depositsList.stream()
                .filter(deposit -> deposit.getTimestamp().isAfter(PerformanceCalculationUtil.get1stDateOfMonthOffset()))
                .mapToDouble(deposit -> deposit.getAmount().doubleValue())
                .sum();
        double mtd = monthFirstValue + monthFirstTotalDeposits == 0? 0 :
                (todayValue - monthFirstValue - monthFirstTotalDeposits) / (monthFirstValue + monthFirstTotalDeposits / 2d);

        double mtdValue = ytdPerformance.getPerformance()
                .stream()
                .filter(performance -> performance.getDate().isAfter(PerformanceCalculationUtil.get1stDateOfMonth()))
                .mapToDouble(item -> item.getUnrealizedDayPL().doubleValue() + item.getRealizedDayPL().doubleValue())
                .sum();

        // Calculate total in / out
        double totalDeposits = performanceSummaryRecord.depositsList()
                .stream()
                .map(GetAccountDepositsResponseInner::getAmount)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        double totalWithdrawals = performanceSummaryRecord.withdrawalList()
                .stream()
                .map(GetAccountWithdrawalsResponseInner::getAmount)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        // Calculate Valuation
        BigDecimal valuation = sumPortfolioValuation(todaySummary);

        // Calculate Performance
        double inOutNetValue = totalDeposits - totalWithdrawals;

        if (arrangementItems.getTotalElements() == 1L && !isEmpty(arrangementItems.getArrangementElements())) {

            var arrangement = arrangementItems.getArrangementElements().get(0);
            return new Portfolio()
                    .arrangementId(arrangement.getExternalArrangementId())
                    .code(arrangement.getExternalArrangementId())
                    .iban(arrangement.getExternalArrangementId())
                    .name(arrangement.getName())
                    .alias(arrangement.getName())
                    .riskLevel(requireNonNull(arrangement.getAdditions()).getOrDefault(RISK_LEVEL, DEFAULT_RISK_LEVEL))
                    .managers(configurationProperties.getPortfolioManagers())
                    .attorneys(configurationProperties.getPortfolioAttorneys())
                    .valuation(getMoneyValue(valuation))
                    .inValue(getMoneyValue(BigDecimal.valueOf(totalDeposits)))
                    .outValue(getMoneyValue(BigDecimal.valueOf(totalWithdrawals)))
                    .netValue(getMoneyValue(BigDecimal.valueOf(inOutNetValue)))
                    .ytdPerformanceValue(getMoneyValue(BigDecimal.valueOf(ytdValue)))
                    .mtdPerformanceValue(getMoneyValue(BigDecimal.valueOf(mtdValue)))
                    .ytdPerformance(BigDecimal.valueOf(ytd))
                    .mtdPerformance(BigDecimal.valueOf(mtd))
                    .valuationRefreshDate(OffsetDateTime.now());
        }

        return null;
    }

    /**
     * Map percentage
     *
     * @param profitLoss  ProfitLoss in double
     * @param todayEquity todayEquity in double
     * @return BigDecimal
     */
    default BigDecimal getPercentage(double profitLoss, double todayEquity) {
        return todayEquity == 0d ?
                BigDecimal.ZERO :
                BigDecimal.valueOf((profitLoss / todayEquity) * 100d);
    }

    /**
     * Sum portfolio valuation
     *
     * @param portfolio DW Account Summary response
     * @return BigDecimal
     */
    default BigDecimal sumPortfolioValuation(GetAccountSummaryResponse portfolio) {
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

    /**
     * Wrap value to a {@link Money} with USD currency code
     *
     * @param value Amount in BigDecimal
     * @return Money
     */
    default Money getMoneyValue(BigDecimal value) {
        return new Money()
                .amount(value)
                .currencyCode(Constants.USD_CURRENCY);
    }


    /**
     * Map portfolio allocation
     *
     * @param todaySummary DW Account Summary Response
     * @return List<Allocation>
     */
    default List<Allocation> mapPortfolioAllocation(GetAccountSummaryResponse todaySummary) {
        // Calculate Asset Class
        double equity = todaySummary.getAccountSummary().getEquity().getEquityValue().doubleValue();
        double cash = todaySummary.getAccountSummary().getCash().getCashBalance().doubleValue();

        double total = equity + cash;
        double cashPct = total != 0d ? (cash / total) * 100d : 0d;
        double equityPct = total != 0d ? (equity / total) * 100d : 0d;

        return List.of(
                new Allocation()
                        .allocationType(AllocationType.ASSET_CLASS.getValue())
                        .classifierType(AllocationClassifierType.ASSET_CLASS.getValue())
                        .classifierName(EQUITY_CLASSIFIER_NAME)
                        .valuation(new Money()
                                .amount(BigDecimal.valueOf(equity))
                                .currencyCode(Constants.USD_CURRENCY))
                        .allocationPct(BigDecimal.valueOf(equityPct))
                        .allocations(List.of(
                                        new AllocationsItem()
                                                .classifierType(AllocationClassifierType.CURRENCY.getValue())
                                                .classifierType(AllocationClassifierType.CURRENCY.getValue())
                                                .classifierName(Constants.USD_CURRENCY)
                                                .valuation(new Money()
                                                        .currencyCode(Constants.USD_CURRENCY)
                                                        .amount(BigDecimal.valueOf(equity))
                                                )
                                                .allocationPct(BigDecimal.valueOf(100))
                                )
                        ),
                new Allocation()
                        .allocationType(AllocationType.ASSET_CLASS.getValue())
                        .classifierType(AllocationClassifierType.ASSET_CLASS.getValue())
                        .classifierName(CASH_CLASSIFIER_NAME)
                        .valuation(new Money()
                                .amount(BigDecimal.valueOf(cash))
                                .currencyCode(Constants.USD_CURRENCY))
                        .allocationPct(BigDecimal.valueOf(cashPct))
                        .allocations(List.of(
                                        new AllocationsItem()
                                                .classifierType(AllocationClassifierType.CURRENCY.getValue())
                                                .classifierType(AllocationClassifierType.CURRENCY.getValue())
                                                .classifierName(Constants.USD_CURRENCY)
                                                .valuation(new Money()
                                                        .currencyCode(Constants.USD_CURRENCY)
                                                        .amount(BigDecimal.valueOf(cash))
                                                )
                                                .allocationPct(BigDecimal.valueOf(100))
                                )
                        )

                ,
                new Allocation()
                        .allocationType(AllocationType.REGION.getValue())
                        .classifierType(AllocationClassifierType.REGION.getValue())
                        .classifierName(REGION_CLASSIFIER_NAME)
                        .valuation(new Money()
                                .amount(BigDecimal.valueOf(equity + cash))
                                .currencyCode(Constants.USD_CURRENCY))
                        .allocationPct(BigDecimal.valueOf(100))
                        .allocations(
                                List.of(
                                        new AllocationsItem()
                                                .classifierType(AllocationClassifierType.COUNTRY.getValue())
                                                .classifierType(AllocationClassifierType.COUNTRY.getValue())
                                                .classifierName(COUNTRY_CLASSIFIER_NAME)
                                                .allocationPct(BigDecimal.valueOf(100))
                                                .valuation(new Money()
                                                        .currencyCode(Constants.USD_CURRENCY)
                                                        .amount(BigDecimal.valueOf(equity + cash)))
                                )
                        ),
                new Allocation()
                        .allocationType(AllocationType.CURRENCY.getValue())
                        .classifierType(AllocationClassifierType.CURRENCY.getValue())
                        .classifierName(Constants.USD_CURRENCY)
                        .valuation(new Money()
                                .amount(BigDecimal.valueOf(equity + cash))
                                .currencyCode(Constants.USD_CURRENCY))
                        .allocationPct(BigDecimal.valueOf(100))
                        .allocations(
                                List.of(
                                        new AllocationsItem()
                                                .classifierType(AllocationClassifierType.ASSET_CLASS.getValue())
                                                .classifierType(AllocationClassifierType.ASSET_CLASS.getValue())
                                                .classifierName(EQUITY_CLASSIFIER_NAME)
                                                .allocationPct(BigDecimal.valueOf(equityPct))
                                                .valuation(new Money()
                                                        .currencyCode(Constants.USD_CURRENCY)
                                                        .amount(BigDecimal.valueOf(equity))),
                                        new AllocationsItem()
                                                .classifierType(AllocationClassifierType.ASSET_CLASS.getValue())
                                                .classifierType(AllocationClassifierType.ASSET_CLASS.getValue())
                                                .classifierName(CASH_CLASSIFIER_NAME)
                                                .allocationPct(BigDecimal.valueOf(cashPct))
                                                .valuation(new Money()
                                                        .currencyCode(Constants.USD_CURRENCY)
                                                        .amount(BigDecimal.valueOf(cash)))
                                )
                        )
        );
    }

    default TradingAccountsPutRequest mapTradingAccountsPutRequest(String portfolioCode) {
        return new TradingAccountsPutRequest()
                .tradingAccounts(new TradingAccountsPutItem()
                        .addAccountsItem(new TradingAccount().id(portfolioCode)));

    }

}
