package com.backbase.modelbank.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.modelbank.config.DriveWealthConfigurationProperties;
import com.backbase.modelbank.config.DriveWealthConfigurationProperties.Holiday;
import com.backbase.modelbank.config.DriveWealthConfigurationProperties.Interval;
import com.backbase.modelbank.config.DriveWealthConfigurationProperties.Market;
import com.backbase.modelbank.mapper.MarketStatusMapper;
import com.backbase.portfolio.instrument.integration.api.service.v1.MarketManagementApi;
import com.backbase.portfolio.instrument.integration.api.service.v1.model.MarketStatusPutRequestBody;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

class MarketStatusSchedulerTest {

    @Mock
    private DriveWealthConfigurationProperties configurationProperties;

    @Mock
    private MarketStatusMapper marketStatusMapper;

    @Mock
    private MarketManagementApi marketManagementApi;

    private MarketStatusScheduler marketStatusScheduler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        marketStatusScheduler = new MarketStatusScheduler(configurationProperties, marketStatusMapper,
            marketManagementApi);

        var open = new Interval("0 0 8 * * ?");
        var close = new Interval("0 0 8 * * ?");
        var marketStatus = new DriveWealthConfigurationProperties.MarketStatus(open, close,
            List.of(new Market("par", "PAR")),
            Collections.emptyList());
        when(configurationProperties.getMarketStatus()).thenReturn(marketStatus);
    }

    @Test
    void testUpdateMarketOpenStatus_WhenMarketStatusExist() {
        // Given
        var putRequest = new MarketStatusPutRequestBody();
        when(marketStatusMapper.mapMarketStatusPutRequestBody(any())).thenReturn(putRequest);
        when(marketManagementApi.putMarketStatus("par", putRequest)).thenReturn(Mono.empty());

        // When
        marketStatusScheduler.updateMarketOpenStatus();

        // Then
        verify(marketStatusMapper).mapMarketStatusPutRequestBody(any());
        verify(marketManagementApi).putMarketStatus("par", putRequest);
    }

    @Test
    void testUpdateMarketOpenStatus_WhenPublicHoliday() {
        // Given
        var putRequest = new MarketStatusPutRequestBody();
        var marketStatus = new DriveWealthConfigurationProperties.MarketStatus(null, null,
            List.of(new Market("par", "PAR")),
            List.of(new Holiday(LocalDate.now().getDayOfMonth(), LocalDate.now().getMonthValue())));
        when(marketStatusMapper.mapMarketStatusPutRequestBody("Close")).thenReturn(putRequest);
        when(marketManagementApi.putMarketStatus("par", putRequest)).thenReturn(Mono.empty());
        when(configurationProperties.getMarketStatus()).thenReturn(marketStatus);

        // When
        marketStatusScheduler.updateMarketOpenStatus();

        // Then
        verify(marketStatusMapper).mapMarketStatusPutRequestBody("Close");
        verify(marketManagementApi).putMarketStatus("par", putRequest);
    }

    @Test
    void testUpdateMarketOpenStatus_WhenMarketStatusDoesntExist() {
        // Given
        var putRequest = new MarketStatusPutRequestBody();
        var postRequest = new com.backbase.portfolio.instrument.integration.api.service.v1.model.Market();
        when(marketStatusMapper.mapMarketStatusPutRequestBody(any())).thenReturn(putRequest);
        when(marketManagementApi.putMarketStatus("par", putRequest)).thenReturn(Mono.error(
            WebClientResponseException.create(404, "Not found", null, null, null)));
        when(marketStatusMapper.mapMarket(any(), any())).thenReturn(postRequest);
        when(marketManagementApi.postMarket(postRequest)).thenReturn(Mono.empty());

        // When
        marketStatusScheduler.updateMarketOpenStatus();

        // Then
        verify(marketStatusMapper).mapMarketStatusPutRequestBody(any());
        verify(marketManagementApi).putMarketStatus("par", putRequest);
        verify(marketManagementApi).postMarket(postRequest);
    }

    @Test
    void testUpdateMarketCloseStatus() {

        // Given
        var putRequest = new MarketStatusPutRequestBody();
        when(marketStatusMapper.mapMarketStatusPutRequestBody("Close")).thenReturn(putRequest);
        when(marketManagementApi.putMarketStatus("par", putRequest)).thenReturn(Mono.empty());

        // When
        marketStatusScheduler.updateMarketCloseStatus();

        // Then
        verify(marketStatusMapper).mapMarketStatusPutRequestBody("Close");
        verify(marketManagementApi).putMarketStatus("par", putRequest);

    }

    @Test
    void testUpdateMarketCloseStatus_WhenMarketStatusDoesntExist() {
        // Given
        var putRequest = new MarketStatusPutRequestBody();
        var postRequest = new com.backbase.portfolio.instrument.integration.api.service.v1.model.Market();
        when(marketStatusMapper.mapMarketStatusPutRequestBody("Close")).thenReturn(putRequest);
        when(marketManagementApi.putMarketStatus("par", putRequest)).thenReturn(Mono.error(
            WebClientResponseException.create(404, "Not found", null, null, null)));
        when(marketStatusMapper.mapMarket(new Market("par", "PAR"), "Close")).thenReturn(postRequest);
        when(marketManagementApi.postMarket(postRequest)).thenReturn(Mono.empty());

        // When
        marketStatusScheduler.updateMarketCloseStatus();

        // Then
        verify(marketStatusMapper).mapMarketStatusPutRequestBody("Close");
        verify(marketManagementApi).putMarketStatus("par", putRequest);
        verify(marketManagementApi).postMarket(postRequest);
    }

    @Test
    void testOnStartUp() {

        // Given
        var putRequest = new MarketStatusPutRequestBody();
        when(marketStatusMapper.mapMarketStatusPutRequestBody(any())).thenReturn(putRequest);
        when(marketManagementApi.putMarketStatus("par", putRequest)).thenReturn(Mono.empty());

        // When
        marketStatusScheduler.onStartUp();

        // Then
        verify(marketStatusMapper).mapMarketStatusPutRequestBody(any());
        verify(marketManagementApi).putMarketStatus("par", putRequest);

    }


}
