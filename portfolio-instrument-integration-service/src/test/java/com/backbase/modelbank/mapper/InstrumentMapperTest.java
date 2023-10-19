package com.backbase.modelbank.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.backbase.drivewealth.clients.instrument.model.InstrumentDetail;
import com.backbase.drivewealth.clients.marketdata.model.HistoricalChartResponse;
import com.backbase.drivewealth.clients.marketdata.model.QuotesResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class InstrumentMapperTest {

    private static ObjectMapper objectMapper;
    private final InstrumentMapperImpl instrumentMapper = new InstrumentMapperImpl();

    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void testInstrumentDetailsMapper() throws IOException {

        var instrumentDetails = objectMapper.readValue(
            ResourceUtils.getFile("classpath:response/instrument-details.json"),
            InstrumentDetail.class);
        var quotesResponse = objectMapper.readValue(ResourceUtils.getFile("classpath:response/quotes-response.json"),
            new TypeReference<List<QuotesResponse>>() {
            }).get(0);

        var result = instrumentMapper.map(instrumentDetails, quotesResponse);

        assertEquals(new BigDecimal("134.76"), result.getPriceData().getPrice().getAmount());
        assertEquals("USD", result.getPriceData().getPrice().getCurrencyCode());
        assertEquals(BigInteger.ONE, result.getPriceData().getPricePerformance().toBigInteger());
        assertEquals("134.71", result.getPriceData().getAskPrice().toString());
        assertEquals("134.7", result.getPriceData().getBidPrice().toString());
        assertEquals("0", result.getPriceData().getAskVolume().toString());
        assertEquals("0", result.getPriceData().getBidVolume().toString());
        assertEquals("57809719", result.getPriceData().getTotalVolume().toString());
        assertEquals("132.03", result.getPriceData().getOpenPrice().toString());
        assertEquals("134.76", result.getPriceData().getClosePrice().toString());
        assertEquals("134.92", result.getPriceData().getHighPrice().toString());
        assertEquals("131.66", result.getPriceData().getLowPrice().toString());
        assertEquals("124.17", result.getPriceData().getWeek52Range().getMin().toString());
        assertEquals("179.61", result.getPriceData().getWeek52Range().getMax().toString());
        assertEquals("131.66", result.getPriceData().getDayRange().getMin().toString());
        assertEquals("134.92", result.getPriceData().getDayRange().getMax().toString());
        assertEquals("ACTIVE", result.getKeyStatistics().getFundStatus());
        assertEquals("2134088000000", result.getKeyStatistics().getTotalAssets().toString());
        assertEquals("EQUITY", result.getKeyStatistics().getPortfolioAssetsAllocation());
        assertEquals("21.86", result.getKeyStatistics().getPriceToEarningsRatio().toString());
        assertEquals("15825127", result.getKeyStatistics().getSharesOutstanding().toString());
        assertEquals("42.40", result.getKeyStatistics().getPriceToBookRatio().toString());
        assertEquals("6.10", result.getKeyStatistics().getEarningsPerShare().toString());
        assertEquals("0.69", result.getKeyStatistics().getDividendYield().toString());

    }

    @Test
    void testInstrumentDetailsMapperWhenAskPriceIsNull() throws IOException {

        var instrumentDetails = objectMapper.readValue(
            ResourceUtils.getFile("classpath:response/instrument-details.json"),
            InstrumentDetail.class);
        var quotesResponse = objectMapper.readValue(ResourceUtils.getFile("classpath:response/quotes-response.json"),
            new TypeReference<List<QuotesResponse>>() {
            }).get(0);
        quotesResponse.setAsk(null);

        var result = instrumentMapper.map(instrumentDetails, quotesResponse);

        assertEquals(new BigDecimal("134.76"), result.getPriceData().getPrice().getAmount());
        assertEquals("USD", result.getPriceData().getPrice().getCurrencyCode());
        assertEquals(BigInteger.ONE, result.getPriceData().getPricePerformance().toBigInteger());
        assertEquals("134.57", result.getPriceData().getAskPrice().toString());
        assertEquals("134.7", result.getPriceData().getBidPrice().toString());
        assertEquals("0", result.getPriceData().getAskVolume().toString());
        assertEquals("0", result.getPriceData().getBidVolume().toString());
        assertEquals("57809719", result.getPriceData().getTotalVolume().toString());
        assertEquals("132.03", result.getPriceData().getOpenPrice().toString());
        assertEquals("134.76", result.getPriceData().getClosePrice().toString());
        assertEquals("134.92", result.getPriceData().getHighPrice().toString());
        assertEquals("131.66", result.getPriceData().getLowPrice().toString());
        assertEquals("124.17", result.getPriceData().getWeek52Range().getMin().toString());
        assertEquals("179.61", result.getPriceData().getWeek52Range().getMax().toString());
        assertEquals("131.66", result.getPriceData().getDayRange().getMin().toString());
        assertEquals("134.92", result.getPriceData().getDayRange().getMax().toString());
        assertEquals("ACTIVE", result.getKeyStatistics().getFundStatus());
        assertEquals("2134088000000", result.getKeyStatistics().getTotalAssets().toString());
        assertEquals("EQUITY", result.getKeyStatistics().getPortfolioAssetsAllocation());
        assertEquals("21.86", result.getKeyStatistics().getPriceToEarningsRatio().toString());
        assertEquals("15825127", result.getKeyStatistics().getSharesOutstanding().toString());
        assertEquals("42.40", result.getKeyStatistics().getPriceToBookRatio().toString());
        assertEquals("6.10", result.getKeyStatistics().getEarningsPerShare().toString());
        assertEquals("0.69", result.getKeyStatistics().getDividendYield().toString());

        quotesResponse.setAsk(null);
        quotesResponse.setClose(null);
        instrumentMapper.map(instrumentDetails, quotesResponse);

        quotesResponse.setAsk(null);
        quotesResponse.setClose(null);
        quotesResponse.setPriorClose(null);
        instrumentMapper.map(instrumentDetails, quotesResponse);

        quotesResponse.setBid(null);
        quotesResponse.close(BigDecimal.TEN);
        instrumentMapper.map(instrumentDetails, quotesResponse);

        quotesResponse.setBid(null);
        quotesResponse.close(null);
        quotesResponse.setPriorClose(BigDecimal.TEN);
        instrumentMapper.map(instrumentDetails, quotesResponse);

        quotesResponse.setBid(null);
        quotesResponse.setClose(null);
        quotesResponse.setPriorClose(null);
        instrumentMapper.map(instrumentDetails, quotesResponse);

    }

    @Test
    void testInstrumentDetailsMapperWhenDwReturnChange() throws IOException {

        var instrumentDetails = objectMapper.readValue(
            ResourceUtils.getFile("classpath:response/instrument-details.json"),
            InstrumentDetail.class);
        var quotesResponse = objectMapper.readValue(ResourceUtils.getFile("classpath:response/quotes-response.json"),
            new TypeReference<List<QuotesResponse>>() {
            }).get(0);
        quotesResponse.setChange(BigDecimal.TEN);

        var result = instrumentMapper.map(instrumentDetails, quotesResponse);

        assertEquals(new BigDecimal("134.76"), result.getPriceData().getPrice().getAmount());
        assertEquals("USD", result.getPriceData().getPrice().getCurrencyCode());
        assertEquals("7", result.getPriceData().getPricePerformance().toBigInteger().toString());
        assertEquals("134.71", result.getPriceData().getAskPrice().toString());
        assertEquals("134.7", result.getPriceData().getBidPrice().toString());
        assertEquals("0", result.getPriceData().getAskVolume().toString());
        assertEquals("0", result.getPriceData().getBidVolume().toString());
        assertEquals("57809719", result.getPriceData().getTotalVolume().toString());
        assertEquals("132.03", result.getPriceData().getOpenPrice().toString());
        assertEquals("134.76", result.getPriceData().getClosePrice().toString());
        assertEquals("134.92", result.getPriceData().getHighPrice().toString());
        assertEquals("131.66", result.getPriceData().getLowPrice().toString());
        assertEquals("124.17", result.getPriceData().getWeek52Range().getMin().toString());
        assertEquals("179.61", result.getPriceData().getWeek52Range().getMax().toString());
        assertEquals("131.66", result.getPriceData().getDayRange().getMin().toString());
        assertEquals("134.92", result.getPriceData().getDayRange().getMax().toString());
        assertEquals("ACTIVE", result.getKeyStatistics().getFundStatus());
        assertEquals("2134088000000", result.getKeyStatistics().getTotalAssets().toString());
        assertEquals("EQUITY", result.getKeyStatistics().getPortfolioAssetsAllocation());
        assertEquals("21.86", result.getKeyStatistics().getPriceToEarningsRatio().toString());
        assertEquals("15825127", result.getKeyStatistics().getSharesOutstanding().toString());
        assertEquals("42.40", result.getKeyStatistics().getPriceToBookRatio().toString());
        assertEquals("6.10", result.getKeyStatistics().getEarningsPerShare().toString());
        assertEquals("0.69", result.getKeyStatistics().getDividendYield().toString());

    }

    @Test
    void testInstrumentDetailsMapperWhenClosePriceIsZero() throws IOException {

        var instrumentDetails = objectMapper.readValue(
            ResourceUtils.getFile("classpath:response/instrument-details.json"),
            InstrumentDetail.class);
        var quotesResponse = objectMapper.readValue(ResourceUtils.getFile("classpath:response/quotes-response.json"),
            new TypeReference<List<QuotesResponse>>() {
            }).get(0);
        quotesResponse.setClose(BigDecimal.ZERO);

        var result = instrumentMapper.map(instrumentDetails, quotesResponse);

        assertEquals(new BigDecimal("134.76"), result.getPriceData().getPrice().getAmount());
        assertEquals("USD", result.getPriceData().getPrice().getCurrencyCode());
        assertEquals("1", result.getPriceData().getPricePerformance().toBigInteger().toString());
        assertEquals("134.71", result.getPriceData().getAskPrice().toString());
        assertEquals("134.7", result.getPriceData().getBidPrice().toString());
        assertEquals("0", result.getPriceData().getAskVolume().toString());
        assertEquals("0", result.getPriceData().getBidVolume().toString());
        assertEquals("57809719", result.getPriceData().getTotalVolume().toString());
        assertEquals("132.03", result.getPriceData().getOpenPrice().toString());
        assertEquals("133.41", result.getPriceData().getClosePrice().toString());
        assertEquals("134.92", result.getPriceData().getHighPrice().toString());
        assertEquals("131.66", result.getPriceData().getLowPrice().toString());
        assertEquals("124.17", result.getPriceData().getWeek52Range().getMin().toString());
        assertEquals("179.61", result.getPriceData().getWeek52Range().getMax().toString());
        assertEquals("131.66", result.getPriceData().getDayRange().getMin().toString());
        assertEquals("134.92", result.getPriceData().getDayRange().getMax().toString());
        assertEquals("ACTIVE", result.getKeyStatistics().getFundStatus());
        assertEquals("2134088000000", result.getKeyStatistics().getTotalAssets().toString());
        assertEquals("EQUITY", result.getKeyStatistics().getPortfolioAssetsAllocation());
        assertEquals("21.86", result.getKeyStatistics().getPriceToEarningsRatio().toString());
        assertEquals("15825127", result.getKeyStatistics().getSharesOutstanding().toString());
        assertEquals("42.40", result.getKeyStatistics().getPriceToBookRatio().toString());
        assertEquals("6.10", result.getKeyStatistics().getEarningsPerShare().toString());
        assertEquals("0.69", result.getKeyStatistics().getDividendYield().toString());

    }
    
    @Test
    void testMapHistoryPrice() throws IOException {

        // given
        HistoricalChartResponse dwHistoryResponse = objectMapper.readValue(
            ResourceUtils.getFile("classpath:response/history-charts.json"),
            HistoricalChartResponse.class);
        // when
        var result = instrumentMapper.mapHistoryPrice(dwHistoryResponse);

        // then
        assertNotNull(result);
    }

}