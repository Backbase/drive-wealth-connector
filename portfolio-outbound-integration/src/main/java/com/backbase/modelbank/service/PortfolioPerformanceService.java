package com.backbase.modelbank.service;

import com.backbase.arrangments.api.integration.v2.ArrangementsApi;
import com.backbase.arrangments.api.integration.v2.model.BaseArrangement;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.dbs.portfolio.integration.portfolio.rest.spec.model.*;
import com.backbase.drivewealth.clients.accounts.api.AccountsApi;
import com.backbase.drivewealth.clients.accounts.model.GetAccountPerformanceResponse;
import com.backbase.drivewealth.clients.marketdata.api.MarketDataApi;
import com.backbase.drivewealth.clients.money.api.DepositsApi;
import com.backbase.drivewealth.clients.money.api.WithdrawalsApi;
import com.backbase.drivewealth.clients.money.model.GetAccountDepositsResponseInner;
import com.backbase.drivewealth.clients.money.model.GetAccountWithdrawalsResponseInner;
import com.backbase.modelbank.config.DriveWealthConfigurationProperties;
import com.backbase.modelbank.mapper.AccountPerformanceMapper;
import com.backbase.modelbank.mapper.AccountValuationMapper;
import com.backbase.modelbank.utils.PerformanceCalculationUtil;
import com.backbase.portfolio.api.integration.v2.PortfolioManagementApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Stream;

import static com.backbase.modelbank.utils.Constants.EUR_CURRENCY;
import static com.backbase.modelbank.utils.Constants.USD_CURRENCY;


@Log4j2
@Service
@RequiredArgsConstructor
public class PortfolioPerformanceService {

    private static final Integer COMPRESSION_DAILY = 0;
    private final DriveWealthConfigurationProperties driveWealthConfigurationProperties;
    private final AccountPerformanceMapper accountPerformanceMapper;
    private final AccountValuationMapper accountValuationMapper;
    private final MarketDataApi marketDataApi;
    private final AccountsApi accountsApi;
    private final ArrangementsApi arrangementsApi;
    private final PortfolioManagementApi portfolioManagementApi;
    private final WithdrawalsApi withdrawalsApi;
    private final DepositsApi depositsApi;

    public CumulativePerformanceBenchmarkDataGet performanceBenchmarkDataByPortfolioId(String portfolioId, String uuid, OffsetDateTime fromDate, OffsetDateTime toDate) {
        log.debug("Trying to get Performance Benchmark for portfolio [{}] ", portfolioId);

        try {
            var benchmarkPerformance = marketDataApi.getHistoricalChart(uuid, COMPRESSION_DAILY, fromDate, toDate, null);
            List<CumulativePerformanceChartItem> data = this.accountPerformanceMapper.mapPerformanceBenchmarkData(benchmarkPerformance);

            return new CumulativePerformanceBenchmarkDataGet().uuid(UUID.randomUUID().toString()).data(data);
        } catch (RestClientException ex) {
            log.error("Error while trying to get benchmark data", ex);
            throw new InternalServerErrorException(ex.getMessage());
        }
    }

    public List<PerformanceBenchmarkGetItem> performanceBenchmarksByPortfolioId() {
        return driveWealthConfigurationProperties.getPortfolioBenchMarkList()
                .stream()
                .map(portfolioBenchMarkRecord -> new PerformanceBenchmarkGetItem()
                        .name(portfolioBenchMarkRecord.name())
                        .uuid(portfolioBenchMarkRecord.uuid()))
                .toList();
    }

    public CumulativePerformanceChartDataGet performanceChartDataByPortfolioId(String portfolioId, OffsetDateTime fromDate, OffsetDateTime toDate) {
        log.debug("Trying to get Cumulative Performance for portfolio [{}] ", portfolioId);

        try {
            Optional<String> dwAccountIdOptional = getDwAccountId(portfolioId);
            if (dwAccountIdOptional.isPresent()) {
                var accountPerformanceById = accountsApi.getAccountPerformanceById(dwAccountIdOptional.get(), null, fromDate, toDate);
                var performanceChartItems = accountPerformanceMapper.mapCumulativePerformanceChartData(accountPerformanceById.getPerformance());
                return new CumulativePerformanceChartDataGet().chartData(performanceChartItems);
            } else {
                log.error("unexpected dw accountId is not present for portfolio [{}]", portfolioId);
                throw new InternalServerErrorException("Error occurred while trying to get performance data");
            }

        } catch (RestClientException ex) {
            log.error("Error while trying to get Performance chart data", ex);
            throw new InternalServerErrorException(ex.getMessage());
        }

    }

    public ValuationChartDataGet valuationChartDataByPortfolioId(String portfolioId, OffsetDateTime fromDate, OffsetDateTime toDate, Granularity granularity) {
        log.debug("Trying to get Valuation Performance for portfolio [{}] ", portfolioId);

        try {
            Optional<String> dwAccountIdOptional = getDwAccountId(portfolioId);

            if (dwAccountIdOptional.isPresent()) {
                var accountPerformance = accountsApi.getAccountPerformanceById(dwAccountIdOptional.get(), null, fromDate, toDate);
                var performanceChartItems = accountValuationMapper.mapValuationChartData(accountPerformance, granularity);
                return new ValuationChartDataGet().chartData(performanceChartItems);
            } else {
                log.error("Unexpected DW accountID is not present for portfolio [{}]", portfolioId);
                throw new InternalServerErrorException("Error occurred while trying to get performance data for portfolioId [" + portfolioId + "]");
            }
        } catch (RestClientException ex) {
            log.error("Error while trying to get valuation chart data", ex);
            throw new InternalServerErrorException(ex.getMessage());
        }
    }

    public PortfolioSummaryAggregationGet getAggregation(List<String> portfolioIds, String currencyCode) {
        log.debug("Trying to get aggregated Performance for portfolio [{}] ", portfolioIds);

        if (StringUtils.hasText(currencyCode)
            && !(USD_CURRENCY.equalsIgnoreCase(currencyCode) || EUR_CURRENCY.equals(currencyCode))) {
            log.warn("Currency Code [{}] is not supported", currencyCode);
            throw new BadRequestException("Currency is not supported + [" + currencyCode + "]");
        }

        try {
            List<String> dwAccountIds = getDwAccountIds(portfolioIds);

            var todaysSummaryList = dwAccountIds
                    .stream()
                    .map(accountsApi::getAccountSummaryById)
                    .toList();

            List<GetAccountPerformanceResponse> ytdPerformance = dwAccountIds
                    .stream()
                    .map(portfolioId -> accountsApi.getAccountPerformanceById(portfolioId, null, PerformanceCalculationUtil.get1stDayOfYear(), OffsetDateTime.now()))
                    .toList();

            var portfolios = portfolioIds.stream().map(portfolioManagementApi::getPortfolio).toList();

            // Calculate total in / out
            List<GetAccountDepositsResponseInner> totalDeposits = dwAccountIds.stream()
                    .map(depositsApi::listAccountDeposits)
                    .flatMap(Collection::stream)
                    .toList();

            List<GetAccountWithdrawalsResponseInner> totalWithdrawals = dwAccountIds.stream()
                    .map(withdrawalsApi::listAccountWithdrawals)
                    .flatMap(Collection::stream)
                    .toList();

            // Calculate Aggregated Performance
            return accountPerformanceMapper.mapAggregationPerformance(dwAccountIds.size(), todaysSummaryList, ytdPerformance, portfolios, totalDeposits, totalWithdrawals, currencyCode);

        } catch (RestClientException ex) {
            log.error("Error while trying to get valuation chart data", ex);
            throw new InternalServerErrorException(ex.getMessage());
        }
    }

    @NotNull
    private Optional<String> getDwAccountId(String portfolioId) {
        var arrangementItemResponseBodyOptional = arrangementsApi.getArrangements(Collections.singletonList(portfolioId))
                .stream()
                .filter(arr -> portfolioId.contains(arr.getId())).findFirst();
        return arrangementItemResponseBodyOptional.map(BaseArrangement::getBBAN);
    }

    @NotNull
    private List<String> getDwAccountIds(List<String> portfolioId) {
        return arrangementsApi.getArrangements(portfolioId)
                .stream()
                .filter(arr -> portfolioId.contains(arr.getId()))
                .map(BaseArrangement::getBBAN)
                .toList();
    }


}
