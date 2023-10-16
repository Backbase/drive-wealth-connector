package com.backbase.modelbank.api;

import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.buildingblocks.test.http.TestRestTemplateConfiguration;
import com.backbase.dbs.portfolio.integration.portfolio.rest.spec.model.Money;
import com.backbase.dbs.portfolio.integration.portfolio.rest.spec.model.PortfolioSummaryAggregationGet;
import com.backbase.modelbank.PortfolioOutboundIntegrationApplication;
import com.backbase.modelbank.advise.ControllerExceptionHandler;
import com.backbase.modelbank.service.PortfolioPerformanceService;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("it")
@AutoConfigureMockMvc
@Import({TestRestTemplateConfiguration.class})
@DirtiesContext
@SpringBootTest(classes = {PortfolioOutboundIntegrationApplication.class, ControllerExceptionHandler.class})
class PortfolioAggregationClientControllerTest {
    private static final String CURRENCY_CODE = "USD";
    private static final List<String> PORTFOLIOS_IDS_LIST = List.of("portfolio1", "portfolio2");
    public static final String URL = "/service-api/v1/portfolios/aggregations";

    static {
        System.setProperty("SIG_SECRET_KEY", TestRestTemplateConfiguration.TEST_JWT_SIG_KEY);
    }

    @MockBean
    private PortfolioPerformanceService service;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetAggregation() throws Exception {

        // Given
        when(service.getAggregation(any(), anyString()))
                .thenReturn(getPortfolioSummaryAggregationGet());

        // When
        // Then
        this.mockMvc.perform(get(URL)
                        .header(HttpHeaders.AUTHORIZATION, TestRestTemplateConfiguration.TEST_SERVICE_BEARER)
                        .param("portfolioIds", PORTFOLIOS_IDS_LIST.get(0))
                        .param("portfolioIds", PORTFOLIOS_IDS_LIST.get(1))
                        .param("currencyCode", CURRENCY_CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfoliosCount").value(2))
                .andExpect(jsonPath("$.valuation").isNotEmpty())
                .andExpect(jsonPath("$.inCashTotal").isNotEmpty());

    }

    @Test
    void testGetAggregation_Error() throws Exception {

        // Given
        when(service.getAggregation(any(), anyString()))
                .thenThrow(InternalServerErrorException.class);

        // When
        // Then
        this.mockMvc.perform(get(URL)
                        .header(HttpHeaders.AUTHORIZATION, TestRestTemplateConfiguration.TEST_SERVICE_BEARER)
                        .param("portfolioIds", PORTFOLIOS_IDS_LIST.get(0))
                        .param("portfolioIds", PORTFOLIOS_IDS_LIST.get(1))
                        .param("currencyCode", CURRENCY_CODE))
                .andExpect(status().is5xxServerError());

    }

    private PortfolioSummaryAggregationGet getPortfolioSummaryAggregationGet() {
        return new PortfolioSummaryAggregationGet()
                .portfoliosCount(BigDecimal.valueOf(2))
                .inCashTotal(getMoney(100))
                .outCashTotal(getMoney(0))
                .valuation(getMoney(1000))
                .performanceYTD(getMoney(100));
    }

    private Money getMoney(int i) {
        return new Money()
                .currency(CURRENCY_CODE)
                .amount(BigDecimal.valueOf(i));
    }
}

