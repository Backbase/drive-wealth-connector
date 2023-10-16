package com.backbase.modelbank.util;

import com.backbase.drivewealth.clients.accounts.model.GetAccountPerformanceResponse;
import com.backbase.drivewealth.clients.accounts.model.GetAccountSummaryResponse;
import com.backbase.drivewealth.clients.marketdata.model.HistoricalChartResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.util.List;

public class TestFactory {

    private TestFactory() {
    }

    private static ObjectMapper getObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .findAndRegisterModules();
    }

    public static List<com.backbase.arrangments.api.integration.v2.model.ArrangementItemResponseBody> getArrangementListMock() {
        com.backbase.arrangments.api.integration.v2.model.ArrangementItemResponseBody arrangement = (com.backbase.arrangments.api.integration.v2.model.ArrangementItemResponseBody) new com.backbase.arrangments.api.integration.v2.model.ArrangementItemResponseBody().id("portfolioId");
        arrangement.setBBAN("portfolioId");
        return List.of(arrangement);
    }

    public static HistoricalChartResponse getHistoricalChartResponseMock() throws IOException {
        return getObjectMapper().readValue(ResourceUtils.getFile("classpath:mock-data/historical-chart.json"), HistoricalChartResponse.class);
    }

    public static GetAccountPerformanceResponse getAccountPerformanceResponse() throws IOException {
        return getObjectMapper().readValue(ResourceUtils.getFile("classpath:mock-data/account-performance.json"), GetAccountPerformanceResponse.class);
    }

    public static  GetAccountSummaryResponse getAccountSummaryResponseMock() throws IOException {
        return getObjectMapper().readValue(ResourceUtils.getFile("classpath:mock-data/account-summary.json"), GetAccountSummaryResponse.class);
    }
}
