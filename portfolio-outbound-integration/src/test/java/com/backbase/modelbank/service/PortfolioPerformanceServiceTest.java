package com.backbase.modelbank.service;

import com.backbase.arrangments.api.integration.v2.ArrangementsApi;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.dbs.portfolio.integration.portfolio.rest.spec.model.*;
import com.backbase.drivewealth.clients.accounts.api.AccountsApi;
import com.backbase.drivewealth.clients.marketdata.api.MarketDataApi;
import com.backbase.drivewealth.clients.money.api.DepositsApi;
import com.backbase.drivewealth.clients.money.api.WithdrawalsApi;
import com.backbase.modelbank.config.DriveWealthConfigurationProperties;
import com.backbase.modelbank.mapper.AccountPerformanceMapper;
import com.backbase.modelbank.mapper.AccountValuationMapper;
import com.backbase.modelbank.models.PortfolioBenchMarkRecord;
import com.backbase.portfolio.api.integration.v2.PortfolioManagementApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static com.backbase.modelbank.util.TestFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioPerformanceServiceTest {
    @Mock
    DriveWealthConfigurationProperties driveWealthConfigurationProperties;
    @Mock
    AccountPerformanceMapper accountPerformanceMapper;
    @Mock
    AccountValuationMapper accountValuationMapper;
    @Mock
    MarketDataApi marketDataApi;
    @Mock
    AccountsApi accountsApi;
    @Mock
    ArrangementsApi arrangementsApi;
    @Mock
    PortfolioManagementApi portfolioManagementApi;
    @Mock
    WithdrawalsApi withdrawalsApi;
    @Mock
    DepositsApi depositsApi;
    @InjectMocks
    PortfolioPerformanceService portfolioPerformanceService;

    @Test
    void testPerformanceBenchmarkDataByPortfolioId() throws IOException {
        // Given
        when(marketDataApi.getHistoricalChart(any(), any(), any(), any(), any())).thenReturn(getHistoricalChartResponseMock());
        when(accountPerformanceMapper.mapPerformanceBenchmarkData(any())).thenCallRealMethod();

        // When
        CumulativePerformanceBenchmarkDataGet result = portfolioPerformanceService.performanceBenchmarkDataByPortfolioId("portfolioId", "uuid", OffsetDateTime.MIN, OffsetDateTime.MAX);

        // Then
        assertNotNull(result);
        assertNotNull(result.getUuid());
        assertEquals(252, result.getData().size());

    }

    @Test
    void testPerformanceBenchmarksByPortfolioId() {
        // Given
        when(driveWealthConfigurationProperties.getPortfolioBenchMarkList()).thenReturn(List.of(new PortfolioBenchMarkRecord("uuid", "name")));

        // When
        List<PerformanceBenchmarkGetItem> result = portfolioPerformanceService.performanceBenchmarksByPortfolioId();

        // Then
        assertNotNull(result);

        assertEquals(List.of(new PerformanceBenchmarkGetItem()
                .name("name")
                .uuid("uuid")), result);
    }

    @Test
    void testPerformanceChartDataByPortfolioId() throws IOException {
        // Given
        when(accountsApi.getAccountPerformanceById(any(), any(), any(), any()))
                .thenReturn(getAccountPerformanceResponse());
        when(accountPerformanceMapper.mapCumulativePerformanceChartData(any())).thenCallRealMethod();
        when(arrangementsApi.getArrangements(any())).thenReturn(getArrangementListMock());

        // When
        CumulativePerformanceChartDataGet result = portfolioPerformanceService.performanceChartDataByPortfolioId("portfolioId", OffsetDateTime.MIN, OffsetDateTime.MAX);

        // Then
        assertNotNull(result);
        assertNotNull(result.getChartData());
        assertEquals(37, result.getChartData().size());
    }


    @ParameterizedTest
    @EnumSource(Granularity.class)
    void testValuationChartDataByPortfolioId(Granularity granularity) throws IOException {
        // Given
        when(accountsApi.getAccountPerformanceById(any(), any(), any(), any())).thenReturn(getAccountPerformanceResponse());
        when(accountValuationMapper.mapValuationChartData(any(), any())).thenCallRealMethod();
        when(arrangementsApi.getArrangements(any())).thenReturn(getArrangementListMock());

        // When
        ValuationChartDataGet result = portfolioPerformanceService.valuationChartDataByPortfolioId("portfolioId", OffsetDateTime.MIN, OffsetDateTime.MAX, granularity);

        // Then
        assertNotNull(result);
        assertNotNull(result.getChartData());
        switch (granularity) {
            case DAILY -> assertEquals(37, result.getChartData().size());
            case WEEKLY -> assertEquals(8, result.getChartData().size());
            case MONTHLY -> assertEquals(2, result.getChartData().size());
            case QUARTERLY -> assertEquals(1, result.getChartData().size());
        }


    }

    @Test
    void testGetAggregation() throws IOException {
        // Given
        when(accountsApi.getAccountPerformanceById(any(), any(), any(), any())).thenReturn(getAccountPerformanceResponse());
        when(accountsApi.getAccountSummaryById(any())).thenReturn(getAccountSummaryResponseMock());
        when(arrangementsApi.getArrangements(any())).thenReturn(getArrangementListMock());

        when(accountPerformanceMapper.mapAggregationPerformance(anyInt(), any(), any(), any(), any(), any(), anyString()))
                .thenReturn(new PortfolioSummaryAggregationGet());

        // When
        PortfolioSummaryAggregationGet result = portfolioPerformanceService.getAggregation(List.of("portfolioId"), "USD");

        // Then
        assertNotNull(result);
    }

    @Test
    void testGetAggregation_NotSupportedCurrency()  {
        // Given
        // When
        // Then
        assertThrows(BadRequestException.class, () -> portfolioPerformanceService.getAggregation(null, "EGP"));
    }

    @Test
    void testCumulativePerformanceBenchmarkDataGet_Error()  {
        // Given
        when(marketDataApi.getHistoricalChart(any(),any(),any(),any(),any())).thenThrow(RestClientException.class);
        // When
        // Then
        assertThrows(InternalServerErrorException.class, () -> portfolioPerformanceService.performanceBenchmarkDataByPortfolioId("portfolioId", null, null, null));
    }

    @Test
    void testPerformanceChartDataByPortfolioId_error()  {
        // Given
        when(arrangementsApi.getArrangements(any())).thenReturn(getArrangementListMock());
        when(accountsApi.getAccountPerformanceById(any(),any(),any(),any())).thenThrow(RestClientException.class);
        // When
        // Then
        assertThrows(InternalServerErrorException.class, () -> portfolioPerformanceService.performanceChartDataByPortfolioId("portfolioId", null, null));
    }

    @Test
    void testValuationChartDataByPortfolioId_error() throws IOException {
        // Given
        when(accountsApi.getAccountPerformanceById(any(), any(), any(), any())).thenThrow(RestClientException.class);
        when(arrangementsApi.getArrangements(any())).thenReturn(getArrangementListMock());
        // When
        // Then
        assertThrows(InternalServerErrorException.class, () -> portfolioPerformanceService.valuationChartDataByPortfolioId("portfolioId", null, null, Granularity.DAILY));
    }
}