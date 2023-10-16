package com.backbase.modelbank.service;

import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.buildingblocks.presentation.errors.UnauthorizedException;
import com.backbase.drivewealth.clients.instrument.api.InstrumentApi;
import com.backbase.drivewealth.clients.instrument.model.InstrumentDetail;
import com.backbase.drivewealth.clients.marketdata.api.MarketDataApi;
import com.backbase.drivewealth.clients.marketdata.model.HistoricalChartResponse;
import com.backbase.drivewealth.clients.marketdata.model.QuotesResponse;
import java.time.OffsetDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@AllArgsConstructor
@Service
@Slf4j
public class DriveWealthService {
    private static final String TOKEN_ERROR_MSG = "Token Invalid, seems DW client Id or secret isn't valid";

    private final InstrumentApi instrumentApi;
    private final MarketDataApi marketDataApi;

    public InstrumentDetail getInstrument(String id) {
        try {
            return instrumentApi.getInstrumentById(id);
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn(TOKEN_ERROR_MSG);
            throw new UnauthorizedException(e.getMessage());
        } catch (Exception e) {
            log.error("Error from Instrument API", e);
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public HistoricalChartResponse getInstrumentHistoricalChart(String id, Integer compression,
        OffsetDateTime startDate,
        OffsetDateTime endDate, String tradingDays) {
        try {
            return marketDataApi.getHistoricalChart(id, compression, startDate, endDate, tradingDays);
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn(TOKEN_ERROR_MSG);
            throw new UnauthorizedException(e.getMessage());
        } catch (Exception e) {
            log.error("Error from Market Data API", e);
           return new HistoricalChartResponse();
        }
    }

    public QuotesResponse getConsolidatedQuote(String symbol) {
        try {
            return marketDataApi.getConsolidatedQuote(symbol).stream()
                .filter(quotesResponse -> Objects.equals(symbol, quotesResponse.getSymbol()))
                .findFirst().orElse(new QuotesResponse());
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn(TOKEN_ERROR_MSG);
            throw new UnauthorizedException(e.getMessage());
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Bad request from Market Data API", e);
            return new QuotesResponse();
        }catch (Exception e) {
            log.error("Error from Market Data API", e);
            return new QuotesResponse();
        }
    }

}
