package com.backbase.modelbank.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.buildingblocks.presentation.errors.UnauthorizedException;
import com.backbase.drivewealth.clients.instrument.api.InstrumentApi;
import com.backbase.drivewealth.clients.instrument.model.InstrumentDetail;
import com.backbase.drivewealth.clients.marketdata.api.MarketDataApi;
import com.backbase.drivewealth.clients.marketdata.model.HistoricalChartResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
class DriveWealthServiceTest {

    @Mock
    private InstrumentApi instrumentApi;
    @Mock
    private MarketDataApi marketDataApi;
    @InjectMocks
    private DriveWealthService driveWealthService;

    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void testGetInstrument() throws IOException {

        // Given
        Mockito.when(instrumentApi.getInstrumentById(Mockito.matches("a67422af-8504-43df-9e63-7361eb0bd99e")))
            .thenReturn(
                objectMapper.readValue(ResourceUtils.getFile("classpath:response/instrument-details.json"),
                    InstrumentDetail.class));

        // When
        var result = driveWealthService.getInstrument("a67422af-8504-43df-9e63-7361eb0bd99e");

        // Then
        verify(instrumentApi, times(1)).getInstrumentById(Mockito.matches("a67422af-8504-43df-9e63-7361eb0bd99e"));
        Assert.notNull(result.getSymbol(), "AAPL");

    }

    @Test
    void testGetInstrumentWhenTokenExpire() throws IOException {

        // Given
        Mockito.when(instrumentApi.getInstrumentById(Mockito.matches("a67422af-8504-43df-9e63-7361eb0bd99e")))
            .thenThrow(
                HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Token Expire", HttpHeaders.EMPTY, null, null))
            .thenReturn(
                objectMapper.readValue(ResourceUtils.getFile("classpath:response/instrument-details.json"),
                    InstrumentDetail.class));

        // When
        UnauthorizedException thrown = assertThrows(
            UnauthorizedException.class,
            () -> driveWealthService.getInstrument("a67422af-8504-43df-9e63-7361eb0bd99e"),
            "Expected getConsolidatedQuote() to throw Unauthorized, but it didn't"
        );

        assertTrue(thrown.getMessage().contentEquals("401 Token Expire"));
    }

    @Test
    void testGetInstrumentWhenInternalServiceError() throws IOException {

        // Given
        Mockito.when(instrumentApi.getInstrumentById(Mockito.matches("a67422af-8504-43df-9e63-7361eb0bd99e")))
            .thenThrow(HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong",
                HttpHeaders.EMPTY, null, null))
            .thenReturn(
                objectMapper.readValue(ResourceUtils.getFile("classpath:response/instrument-details.json"),
                    InstrumentDetail.class));

        // When and Then
        assertThrows(InternalServerErrorException.class,
            () -> driveWealthService.getInstrument("a67422af-8504-43df-9e63-7361eb0bd99e"));
    }

    @Test
    void testGetInstrumentHistoricalChart() throws IOException {

        // Given
        Mockito.when(
                marketDataApi.getHistoricalChart(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(
                objectMapper.readValue(ResourceUtils.getFile("classpath:response/history-charts.json"),
                    HistoricalChartResponse.class));

        // When
        var result = driveWealthService.getInstrumentHistoricalChart("a67422af-8504-43df-9e63-7361eb0bd99e", 0,
            OffsetDateTime.parse("2023-01-12T00:00:00Z"), OffsetDateTime.parse("2023-01-12T23:59:59Z"), "0");

        // Then
        verify(marketDataApi, times(1)).getHistoricalChart(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any());
        Assert.notNull(result.getInstrumentID(), "a67422af-8504-43df-9e63-7361eb0bd99e");

    }

    @Test
    void testGetInstrumentHistoricalChartWhenTokenExpire() throws IOException {

        // Given
        Mockito.when(
                marketDataApi.getHistoricalChart(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenThrow(
                HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Token Expire", HttpHeaders.EMPTY, null, null))
            .thenReturn(
                objectMapper.readValue(ResourceUtils.getFile("classpath:response/history-charts.json"),
                    HistoricalChartResponse.class));

        // When
        UnauthorizedException thrown = assertThrows(
            UnauthorizedException.class,
            () -> driveWealthService.getInstrumentHistoricalChart("a67422af-8504-43df-9e63-7361eb0bd99e", 0,
                OffsetDateTime.parse("2023-01-12T00:00:00Z"), OffsetDateTime.parse("2023-01-12T23:59:59Z"), "0"),
            "Expected getConsolidatedQuote() to throw Unauthorized, but it didn't"
        );

        assertTrue(thrown.getMessage().contentEquals("401 Token Expire"));
    }

    @Test
    void testGetInstrumentHistoricalChartWhenDwReturnInternalServiceError() throws IOException {
        // Given
        var startDate = OffsetDateTime.parse("2023-01-12T00:00:00Z");
        var endDate = OffsetDateTime.parse("2023-01-12T23:59:59Z");
        Mockito.when(
                marketDataApi.getHistoricalChart(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenThrow(
                HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong",
                    HttpHeaders.EMPTY, null, null))
            .thenReturn(
                objectMapper.readValue(ResourceUtils.getFile("classpath:response/history-charts.json"),
                    HistoricalChartResponse.class));

        // When and Then
        var result = driveWealthService.getInstrumentHistoricalChart("a67422af-8504-43df-9e63-7361eb0bd99e", 0,
                startDate, endDate, "0");

        assertNotNull(result);
    }

    @Test
    void testGetConsolidatedQuote() throws IOException {

        // Given
        Mockito.when(
                marketDataApi.getConsolidatedQuote(Mockito.any()))
            .thenReturn(
                objectMapper.readValue(ResourceUtils.getFile("classpath:response/quotes-response.json"),
                    new TypeReference<>() {
                    }));

        // When
        var result = driveWealthService.getConsolidatedQuote("AAPL");

        // Then
        verify(marketDataApi, times(1)).getConsolidatedQuote(Mockito.any());
        Assert.notNull(result.getSymbol(), "AAPL");
    }

    @Test
    void testGetConsolidatedQuoteWhenTokenExpire() throws IOException {

        // Given
        Mockito.when(
                marketDataApi.getConsolidatedQuote(Mockito.any()))
            .thenThrow(
                HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Token Expire", HttpHeaders.EMPTY, null, null))
            .thenReturn(
                objectMapper.readValue(ResourceUtils.getFile("classpath:response/quotes-response.json"),
                    new TypeReference<>() {
                    }));

        // When
        UnauthorizedException thrown = assertThrows(
            UnauthorizedException.class,
            () -> driveWealthService.getConsolidatedQuote("AAPL"),
            "Expected getConsolidatedQuote() to throw Unauthorized, but it didn't"
        );

        assertTrue(thrown.getMessage().contentEquals("401 Token Expire"));

    }

    @Test
    void testGetConsolidatedQuoteWhenInternalServiceError() throws IOException {

        // Given
        Mockito.when(
                marketDataApi.getConsolidatedQuote(Mockito.any()))
            .thenThrow(
                HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong",
                    HttpHeaders.EMPTY, null, null))
            .thenReturn(
                objectMapper.readValue(ResourceUtils.getFile("classpath:response/quotes-response.json"),
                    new TypeReference<>() {
                    }));

        // When and Then
        var result = driveWealthService.getConsolidatedQuote("AAPL");

        assertNotNull(result);
    }

}