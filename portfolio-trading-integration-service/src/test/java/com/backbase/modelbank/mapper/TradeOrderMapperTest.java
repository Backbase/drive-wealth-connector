package com.backbase.modelbank.mapper;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.portfolio.service.trading.rest.spec.model.Money;
import com.backbase.dbs.portfolio.service.trading.rest.spec.model.OrderType;
import com.backbase.dbs.portfolio.service.trading.rest.spec.model.PlaceOrderRequest;
import com.backbase.dbs.portfolio.service.trading.rest.spec.model.TradeDirection;
import com.backbase.drivewealth.clients.instrument.model.InstrumentDetail;
import com.backbase.drivewealth.clients.orders.model.CreateOrderRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TradeOrderMapperTest {

    private final TradeOrderMapper tradeOrderMapper = new TradeOrderMapperImpl();

    @Test
    @DisplayName("Should map PlaceOrderRequest to CreateOrderRequest")
    void shouldMapPlaceOrderRequestToCreateOrderRequest() {
        // Given
        String portfolioId = "portfolio-123";
        OrderType orderType = OrderType.LIMIT_ORDER;
        String symbol = "AAPL";
        TradeDirection direction = TradeDirection.BUY;
        BigDecimal quantity = BigDecimal.valueOf(10);
        BigDecimal limitPrice = BigDecimal.valueOf(100);
        LocalDate expirationDate = LocalDate.now().plusDays(1);

        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest();
        placeOrderRequest.setPortfolioId(portfolioId);
        placeOrderRequest.setOrderType(orderType);
        placeOrderRequest.setDirection(direction);
        placeOrderRequest.setQuantity(quantity);
        placeOrderRequest.setLimitPrice(new Money().amount(limitPrice).currency("USD"));
        placeOrderRequest.setExpirationDate(expirationDate);

        InstrumentDetail instrumentResponse = new InstrumentDetail();
        instrumentResponse.setSymbol(symbol);

        // When
        CreateOrderRequest createOrderRequest = tradeOrderMapper.mapToCreateOrderRequest(placeOrderRequest,
            instrumentResponse);

        // Then
        Assertions.assertNotNull(createOrderRequest);
        Assertions.assertEquals(portfolioId, createOrderRequest.getAccountNo());
        Assertions.assertEquals("LIMIT", createOrderRequest.getOrderType());
        Assertions.assertEquals(symbol, createOrderRequest.getSymbol());
        Assertions.assertEquals("Buy", createOrderRequest.getSide());
        Assertions.assertEquals(quantity, createOrderRequest.getQuantity());
        Assertions.assertEquals(limitPrice, createOrderRequest.getPrice());
        Assertions.assertEquals(expirationDate.toString(), createOrderRequest.getExpiration());
        Assertions.assertNull(createOrderRequest.getAmountCash());
        Assertions.assertNull(createOrderRequest.getCommission());
        Assertions.assertNull(createOrderRequest.getClientNotes());
        Assertions.assertNull(createOrderRequest.getPreventQueuing());
        Assertions.assertNull(createOrderRequest.getExtendedHours());
        Assertions.assertNull(createOrderRequest.getMetadata());
        Assertions.assertNull(createOrderRequest.getTimeInForce());

        // Given
        placeOrderRequest.setOrderType(OrderType.MARKET_ORDER);
        placeOrderRequest.setExpirationDate(null);

        // When
        tradeOrderMapper.mapToCreateOrderRequest(placeOrderRequest, instrumentResponse);
    }

    @Test
    @DisplayName("Should throw BadRequestException for unsupported OrderType")
    void shouldThrowBadRequestExceptionForUnsupportedOrderType() {
        // Given
        OrderType orderType = null;

        // When/Then
        Assertions.assertThrows(BadRequestException.class, () -> tradeOrderMapper.orderType(orderType));
    }

}
