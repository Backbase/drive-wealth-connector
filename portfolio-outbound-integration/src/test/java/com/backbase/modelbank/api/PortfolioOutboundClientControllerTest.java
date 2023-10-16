package com.backbase.modelbank.api;

import com.backbase.buildingblocks.test.http.TestRestTemplateConfiguration;
import com.backbase.dbs.portfolio.integration.portfolio.rest.spec.model.*;
import com.backbase.modelbank.PortfolioOutboundIntegrationApplication;
import com.backbase.modelbank.service.PortfolioPerformanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("it")
@AutoConfigureMockMvc
@Import({TestRestTemplateConfiguration.class})
@DirtiesContext
@SpringBootTest(classes = {PortfolioOutboundIntegrationApplication.class})
class PortfolioOutboundClientControllerTest {
    public static final String BENCHMARK_HISTORY_URL = "/client-api/v2/portfolios/{portfolioId}/performance-benchmark-histories";
    public static final String BENCHMARK_URL = "/service-api/v1/portfolios/{portfolioId}/performance-benchmarks";
    public static final String PERFORMANCE_CHART_URL = "/service-api/v1/portfolios/{portfolioId}/performance-charts";
    public static final String VALUATION_URL = "/service-api/v1/portfolios/{portfolioId}/valuation-charts";
    private static final String PORTFOLIO_ID = "id";
    public static final String UUID = "uuid";
    private static final String BENCHMARK_NAME = "benchmarkName";

    static {
        System.setProperty("SIG_SECRET_KEY", TestRestTemplateConfiguration.TEST_JWT_SIG_KEY);
    }

    @MockBean
    private PortfolioPerformanceService service;


    @Autowired
    private MockMvc mockMvc;

    @Test
    void testPerformanceBenchmarkDataByPortfolioId() throws Exception {

        // Given
        when(service.performanceBenchmarksByPortfolioId())
                .thenReturn(List.of(new PerformanceBenchmarkGetItem()
                        .name(BENCHMARK_NAME)
                        .uuid(UUID)));

        // When
        // Then
        this.mockMvc.perform(get(BENCHMARK_URL.replace("{portfolioId}", PORTFOLIO_ID))
                        .header(HttpHeaders.AUTHORIZATION, TestRestTemplateConfiguration.TEST_SERVICE_BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)));

    }

    @Test
    void testPerformanceBenchmarksByPortfolioId() throws Exception {
        // Given
        when(service.performanceBenchmarkDataByPortfolioId(eq(PORTFOLIO_ID), any(), any(), any()))
                .thenReturn(new CumulativePerformanceBenchmarkDataGet()
                        .uuid(UUID)
                        .data(List.of(new CumulativePerformanceChartItem()
                                .value(BigDecimal.ZERO)
                                .dateFrom(OffsetDateTime.MIN)
                                .dateTo(OffsetDateTime.MAX))));

        // When
        // Then
        this.mockMvc.perform(get(BENCHMARK_HISTORY_URL.replace("{portfolioId}", PORTFOLIO_ID))
                        .header(HttpHeaders.AUTHORIZATION, TestRestTemplateConfiguration.TEST_SERVICE_BEARER)
                        .param("uuid", "")
                        .param("fromDate", OffsetDateTime.MIN.toString())
                        .param("toDate", OffsetDateTime.MAX.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(1)));

    }

    @Test
    void testPerformanceChartDataByPortfolioId() throws Exception {
        // Given
        when(service.performanceChartDataByPortfolioId(anyString(), any(), any()))
                .thenReturn(new CumulativePerformanceChartDataGet()
                        .chartData(List.of(
                                new CumulativePerformanceChartItem()
                                        .value(BigDecimal.ZERO)
                                        .dateFrom(OffsetDateTime.MIN)
                                        .dateTo(OffsetDateTime.MAX)
                        )));

        // When
        // Then
        this.mockMvc.perform(get(PERFORMANCE_CHART_URL.replace("{portfolioId}", PORTFOLIO_ID))
                        .header(HttpHeaders.AUTHORIZATION, TestRestTemplateConfiguration.TEST_SERVICE_BEARER)
                        .param("fromDate", OffsetDateTime.MIN.toString())
                        .param("toDate", OffsetDateTime.MAX.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chartData").isNotEmpty())
                .andExpect(jsonPath("$.chartData").isArray())
                .andExpect(jsonPath("$.chartData", hasSize(1)));

    }

    @Test
    void testValuationChartDataByPortfolioId() throws Exception {
        // Given
        when(service.valuationChartDataByPortfolioId(anyString(), any(), any(), any()))
                .thenReturn(new ValuationChartDataGet()
                        .chartData(List.of(
                                new ValuationChartItem()
                                        .value(new Money()
                                                .amount(BigDecimal.ONE)
                                                .currency("USD"))
                                        .dateFrom(OffsetDateTime.MIN)
                                        .dateTo(OffsetDateTime.MAX)
                        )));

        // When
        // Then
        this.mockMvc.perform(get(VALUATION_URL.replace("{portfolioId}", PORTFOLIO_ID))
                        .header(HttpHeaders.AUTHORIZATION, TestRestTemplateConfiguration.TEST_SERVICE_BEARER)
                        .param("fromDate", OffsetDateTime.MIN.toString())
                        .param("toDate", OffsetDateTime.MAX.toString())
                        .param("granularity", Granularity.MONTHLY.getValue())

                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chartData").isNotEmpty())
                .andExpect(jsonPath("$.chartData").isArray())
                .andExpect(jsonPath("$.chartData", hasSize(1)));
    }
}

