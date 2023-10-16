package com.backbase.modelbank.mapper;

import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountSummaryResponseAccountSummaryEquityEquityPositionsInner;
import com.backbase.modelbank.utils.Constants;
import com.backbase.portfolio.api.service.integration.v1.model.InstrumentHoldingsPostRequest;
import com.backbase.portfolio.api.service.integration.v1.model.InstrumentHoldingsPutRequest;
import com.backbase.portfolio.api.service.integration.v1.model.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class InstrumentHoldingsMapperTest {
    public static final String INSTRUMENT_HOLDING_EXTERNAL_ID = "instrumentHoldingExternalId";
    private static final List<String> PORTFOLIO_CODES = List.of("code1");
    private static final Double TOTAL_EQUITY = 100D;
    private static final Double TOTAL_CASH = 100D;
    InstrumentHoldingsMapper instrumentHoldingsMapper = new com.backbase.modelbank.mapper.InstrumentHoldingsMapperImpl();

    @Test
    void testMapInstrumentHoldings_InstrumentHoldingsPutRequest() {
        InstrumentHoldingsPutRequest result = instrumentHoldingsMapper.mapInstrumentHoldings(INSTRUMENT_HOLDING_EXTERNAL_ID, PORTFOLIO_CODES, TOTAL_EQUITY, TOTAL_CASH, getPostionList(), InstrumentHoldingsPutRequest.class);
        assertNotNull(result);
        assertNotNull(result.getPortfolioIds());
        assertEquals(1, result.getPortfolioIds().size());
        assertEquals(new Money()
                .currencyCode(Constants.USD_CURRENCY)
                .amount(BigDecimal.valueOf(100d)), result.getBuyPrice());
        assertEquals(new Money()
                .currencyCode(Constants.USD_CURRENCY)
                .amount(BigDecimal.valueOf(1d)), result.getTodayPL());
        assertEquals(new Money()
                        .currencyCode(Constants.USD_CURRENCY)
                        .amount(BigDecimal.valueOf(10d)), result.getTotalPL());
        assertEquals(BigDecimal.valueOf(10d), result.getTotalPLPct());
        assertEquals(BigDecimal.valueOf(1d), result.getTodayPLPct());

    }

    @Test
    void testMapInstrumentHoldings_InstrumentHoldingsPostRequest() {
        InstrumentHoldingsPostRequest result = instrumentHoldingsMapper.mapInstrumentHoldings(INSTRUMENT_HOLDING_EXTERNAL_ID, PORTFOLIO_CODES, TOTAL_EQUITY, TOTAL_CASH, getPostionList(), InstrumentHoldingsPostRequest.class);

        assertNotNull(result);
        assertNotNull(result.getPortfolioIds());
        assertEquals(1, result.getPortfolioIds().size());
        assertEquals(new Money()
                .currencyCode(Constants.USD_CURRENCY)
                .amount(BigDecimal.valueOf(100d)), result.getBuyPrice());
        assertEquals(new Money()
                .currencyCode(Constants.USD_CURRENCY)
                .amount(BigDecimal.valueOf(1d)), result.getTodayPL());
        assertEquals(new Money()
                .currencyCode(Constants.USD_CURRENCY)
                .amount(BigDecimal.valueOf(10d)), result.getTotalPL());
        assertEquals(BigDecimal.valueOf(10d), result.getTotalPLPct());
        assertEquals(BigDecimal.valueOf(1d), result.getTodayPLPct());
    }

    private List<GetAccountSummaryResponseAccountSummaryEquityEquityPositionsInner> getPostionList() {
        return List.of(new GetAccountSummaryResponseAccountSummaryEquityEquityPositionsInner()
                .instrumentID("instrumentId")
                .avgPrice(BigDecimal.valueOf(100))
                .openQty(BigDecimal.ONE)
                .marketValue(BigDecimal.valueOf(100))
                .symbol("APPL")
                .unrealizedPL(BigDecimal.valueOf(10))
                .unrealizedDayPL(BigDecimal.valueOf(1))
                .unrealizedDayPLPercent(BigDecimal.valueOf(1))
        );
    }
}