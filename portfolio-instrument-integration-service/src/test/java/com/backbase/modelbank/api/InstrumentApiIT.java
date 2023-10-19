package com.backbase.modelbank.api;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.buildingblocks.test.http.TestRestTemplateConfiguration;
import com.backbase.drivewealth.clients.instrument.api.InstrumentApi;
import com.backbase.drivewealth.clients.instrument.model.InstrumentDetail;
import com.backbase.drivewealth.clients.login.api.LoginApi;
import com.backbase.drivewealth.clients.login.model.LoginResponse;
import com.backbase.drivewealth.clients.marketdata.api.MarketDataApi;
import com.backbase.drivewealth.clients.marketdata.model.HistoricalChartResponse;
import com.backbase.drivewealth.clients.marketdata.model.QuotesResponse;
import com.backbase.modelbank.Application;
import com.backbase.modelbank.config.DWAccessTokenConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.ResourceUtils;

@SpringBootTest(classes = Application.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
@ActiveProfiles("it")
class InstrumentApiIT {

    @MockBean
    private LoginApi loginApi;
    @MockBean
    private InstrumentApi instrumentApi;
    @MockBean
    private MarketDataApi marketDataApi;
    @MockBean
    private DWAccessTokenConfig accessTokenConfig;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;

    @Test
    void testInstrumentDetails() throws Exception {

        // Given
        Mockito.when(loginApi.getAuthToken(Mockito.any())).thenReturn(new LoginResponse().accessToken("accessToken"));
        Mockito.when(instrumentApi.getInstrumentById(Mockito.matches("a67422af-8504-43df-9e63-7361eb0bd99e"))).thenReturn(
            mapper.readValue(ResourceUtils.getFile("classpath:response/instrument-details.json"),
                InstrumentDetail.class));
        Mockito.when(marketDataApi.getConsolidatedQuote(Mockito.matches("AAPL"))).thenReturn(
            Arrays.stream(mapper.readValue(ResourceUtils.getFile("classpath:response/quotes-response.json"),
                QuotesResponse[].class)).toList());

        // When and Then
        mvc.perform(
                get("/service-api/v1/instruments/a67422af-8504-43df-9e63-7361eb0bd99e/details").header("Authorization",
                    TestRestTemplateConfiguration.TEST_SERVICE_BEARER)).andDo(print()).andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.priceData.price.amount", is("134.76")))
            .andExpect(jsonPath("$.priceData.price.currencyCode", is("USD")))
            .andExpect(jsonPath("$.priceData.pricePerformance", is("1.00")))
            .andExpect(jsonPath("$.priceData.pricePerformanceAbs.amount", is("1.35")))
            .andExpect(jsonPath("$.priceData.pricePerformanceAbs.currencyCode", is("USD")))
            .andExpect(jsonPath("$.priceData.askPrice", is("134.71")))
            .andExpect(jsonPath("$.priceData.bidPrice", is("134.7")))
            .andExpect(jsonPath("$.priceData.askVolume", is("0"))).andExpect(jsonPath("$.priceData.bidVolume", is("0")))
            .andExpect(jsonPath("$.priceData.totalVolume", is("57809719")))
            .andExpect(jsonPath("$.priceData.openPrice", is("132.03")))
            .andExpect(jsonPath("$.priceData.closePrice", is("134.76")))
            .andExpect(jsonPath("$.priceData.lowPrice", is("131.66")))
            .andExpect(jsonPath("$.priceData.highPrice", is("134.92")))
            .andExpect(jsonPath("$.priceData.week52Range.min", is("124.17")))
            .andExpect(jsonPath("$.priceData.week52Range.max", is("179.61")))
            .andExpect(jsonPath("$.priceData.dayRange.min", is("131.66")))
            .andExpect(jsonPath("$.priceData.dayRange.max", is("134.92")))
            .andExpect(jsonPath("$.keyStatistics.fundStatus", is("ACTIVE")))
            .andExpect(jsonPath("$.keyStatistics.totalAssets", is("2134088000000")))
            .andExpect(jsonPath("$.keyStatistics.portfolioAssetsAllocation", is("EQUITY")))
            .andExpect(jsonPath("$.keyStatistics.priceToEarningsRatio", is("21.86")))
            .andExpect(jsonPath("$.keyStatistics.sharesOutstanding", is("15825127")))
            .andExpect(jsonPath("$.keyStatistics.priceToBookRatio", is("42.40")))
            .andExpect(jsonPath("$.keyStatistics.earningsPerShare", is("6.10")))
            .andExpect(jsonPath("$.keyStatistics.dividendYield", is("0.69"))).andReturn();

    }


    @Test
    void testInstrumentHistoryCharts() throws Exception {

        // Given
        Mockito.when(loginApi.getAuthToken(Mockito.any())).thenReturn(new LoginResponse().accessToken("accessToken"));
        Mockito.when(
                marketDataApi.getHistoricalChart(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(
                mapper.readValue(ResourceUtils.getFile("classpath:response/history-charts.json"),
                    HistoricalChartResponse.class));

        // When and Then
        mvc.perform(
                get("/service-api/v1/instruments/a67422af-8504-43df-9e63-7361eb0bd99e/chart-prices")
                    .param("fromDate", "2023-01-13")
                    .param("toDate", "2023-01-13")
                    .header("Authorization",
                        TestRestTemplateConfiguration.TEST_SERVICE_BEARER)).andDo(print()).andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.chartData[0].date", is("2022-01-14T00:00:00Z")))
            .andExpect(jsonPath("$.chartData[0].price", is("173.06")))
            .andExpect(jsonPath("$.chartData[0].currency", is("USD")))
            .andExpect(jsonPath("$.chartData[0].priceType", is("CLOSE")))
            .andReturn();

    }

}
