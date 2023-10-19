package com.backbase.modelbank.mapper;

import static com.backbase.modelbank.util.TestFactory.getAccountSummary;
import static com.backbase.modelbank.util.TestFactory.getTransactions;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountSummaryResponse;
import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountSummaryResponseAccountSummary;
import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountSummaryResponseAccountSummaryEquity;
import com.backbase.drivewealth.reactive.clients.instrument.model.InstrumentDetail;
import com.backbase.stream.portfolio.model.Position;
import com.backbase.stream.portfolio.model.PositionBundle;
import com.backbase.stream.portfolio.model.PositionTransaction;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PositionBundleMapperTest {

    private final PositionBundleMapper mapper =
        new com.backbase.modelbank.mapper.PositionBundleMapperImpl();

    @Test
    void testMap() throws IOException {

        // When
        var response = mapper.mapPositions(getAccountSummary());

        // Then
        assertEquals(14, response.size());
        var positionResponse = response.get(0);
        assertEquals("DSEF000003-AAPL", positionResponse.getExternalId());
        assertEquals("a67422af-8504-43df-9e63-7361eb0bd99e", positionResponse.getInstrumentId());
        assertEquals("DSEF000003", positionResponse.getPortfolioCode());
        assertEquals(new BigDecimal("44.82"), positionResponse.getAbsolutePerformance().getAmount());
        assertEquals(new BigDecimal("2.57"), positionResponse.getRelativePerformance());
        assertEquals(new BigDecimal("153.87"), positionResponse.getPurchasePrice().getAmount());
        assertEquals(new BigDecimal("2.57"), positionResponse.getUnrealizedPLPct());
        assertEquals(new BigDecimal("44.82"), positionResponse.getUnrealizedPL().getAmount());
        assertEquals(new BigDecimal("-0.91"), positionResponse.getTodayPLPct());
        assertEquals(new BigDecimal("-16.42"), positionResponse.getTodayPL().getAmount());
        assertEquals(BigDecimal.ZERO, positionResponse.getAccruedInterest().getAmount());
        assertEquals(new BigDecimal("11.3245069"), positionResponse.getQuantity());
        assertEquals(new BigDecimal("1787.35"), positionResponse.getValuation().getAmount());
        assertEquals(new BigDecimal("1742.53"), positionResponse.getCostPrice().getAmount());
        assertEquals(BigDecimal.ONE, positionResponse.getCostExchangeRate().getAmount());
        assertEquals(new BigDecimal("33.27"), positionResponse.getPercentAssetClass());
        assertEquals(new BigDecimal("11.71"), positionResponse.getPercentPortfolio());
        assertEquals(new BigDecimal("33.27"), positionResponse.getPercentParent());
        assertEquals(new BigDecimal("44.82"), positionResponse.getUnrealizedPLLocal().getAmount());
        assertEquals(new BigDecimal("2.57"), positionResponse.getUnrealizedPLLocalPct());
        assertEquals("security", positionResponse.getPositionType());
    }

    @Test
    void testMapWhenEmptyAccountSummary() {

        // When
        var response = mapper.mapPositions(null);

        // Then
        assertEquals(0, response.size());
    }

    @Test
    void testMapWhenNoPositions() {

        // Given
        var input = new GetAccountSummaryResponse();
        input.setAccountSummary(new GetAccountSummaryResponseAccountSummary()
            .equity(new GetAccountSummaryResponseAccountSummaryEquity().equityPositions(Collections.emptyList())));

        // When
        var response = mapper.mapPositions(input);

        // Then
        assertEquals(0, response.size());
    }

    @Test
    void testPositionTransactionMapping() throws IOException {

        // Given
        List<Position> positions = List.of(
            new Position().externalId("DSEF000003-GOOGL").portfolioCode("DSEF000003"),
            new Position().externalId("GOOGL").portfolioCode("DSEF000004"));

        var orderMap = new HashMap<String, String>() {{
            put("KB.1438d2fb-605e-4351-a8f7-5eb1ba7a2953", "MARKET");
        }};

        // When
        var result = mapper.mapPositionBundles(positions, getTransactions(), orderMap);

        // Then
        assertEquals(14, result.size());
    }

    @Test
    void testUpdateInstrumentDetails() {

        // Given
        var positionBundle = new PositionBundle()
            .transactions(singletonList(new PositionTransaction()));
        var instrumentDetails = new InstrumentDetail().symbol("GOOGL").ISIN("1234").exchange("NSQ");

        // When
        var result = mapper.updateInstrumentDetails(positionBundle, instrumentDetails);

        // Then
        assertEquals(1, result.getTransactions().size());
        assertEquals("1234", result.getTransactions().get(0).getOfficialCode());
        assertEquals("1234", result.getTransactions().get(0).getISIN());
        assertEquals("NSQ", result.getTransactions().get(0).getExchange());
    }

    @Test
    void testUpdateInstrumentDetailsWhenNullInstrumentResponse() {

        // Given
        var positionBundle = new PositionBundle()
            .transactions(singletonList(new PositionTransaction()));

        // When
        var result = mapper.updateInstrumentDetails(positionBundle, null);

        // Then
        assertEquals(1, result.getTransactions().size());
        assertNull(result.getTransactions().get(0).getOfficialCode());
        assertNull(result.getTransactions().get(0).getISIN());
        assertNull(result.getTransactions().get(0).getExchange());
    }

}