package com.backbase.modelbank.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.dbs.portfolio.service.trading.rest.spec.model.CommissionsOrderResponse;
import com.backbase.dbs.portfolio.service.trading.rest.spec.model.PlaceOrderRequest;
import com.backbase.dbs.portfolio.service.trading.rest.spec.model.PlaceOrderResponse;
import com.backbase.drivewealth.clients.instrument.api.InstrumentApi;
import com.backbase.drivewealth.clients.instrument.model.InstrumentDetail;
import com.backbase.drivewealth.clients.orders.api.OrdersApi;
import com.backbase.drivewealth.clients.orders.model.CreateOrderRequest;
import com.backbase.drivewealth.clients.orders.model.CreateOrderResponse;
import com.backbase.drivewealth.clients.orders.model.UpdateOrderRequest;
import com.backbase.drivewealth.clients.orders.model.UpdateOrderResponse;
import com.backbase.modelbank.mapper.TradeOrderMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class TradeOrderServiceTest {

    @Mock
    private OrdersApi ordersApi;

    @Mock
    private InstrumentApi instrumentApi;

    @Mock
    private TradeOrderMapper orderMapper;

    @InjectMocks
    private TradeOrderService tradeOrderService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateOrder() {
        // Mocking
        String instrumentId = "instrument-1";
        String orderId = "order-1";
        InstrumentDetail instrumentResponse = new InstrumentDetail()
            .symbol("AAPL")
            .id("instrument-1");
        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest()
            .instrumentId(instrumentId)
            .quantity(BigDecimal.TEN);
        CreateOrderResponse createOrderResponse = new CreateOrderResponse()
            .orderID(orderId)
            .orderNo("123");
        when(instrumentApi.getInstrumentById(instrumentId)).thenReturn(instrumentResponse);
        when(orderMapper.mapToCreateOrderRequest(placeOrderRequest, instrumentResponse)).thenReturn(createOrderRequest);
        when(ordersApi.createOrder(createOrderRequest)).thenReturn(createOrderResponse);

        // Test
        PlaceOrderResponse result = tradeOrderService.create(placeOrderRequest);

        // Assertions
        verify(instrumentApi).getInstrumentById(instrumentId);
        verify(orderMapper).mapToCreateOrderRequest(placeOrderRequest, instrumentResponse);
        verify(ordersApi).createOrder(createOrderRequest);
        verify(orderMapper).mapToPlaceOrderResponse(createOrderResponse.getOrderID());
    }

    @Test
    void testCreateOrderThrowsException() {
        // Mocking
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest();
        when(instrumentApi.getInstrumentById(any())).thenThrow(new RuntimeException());

        // Test & Assertions
        Assertions.assertThrows(InternalServerErrorException.class, () -> tradeOrderService.create(placeOrderRequest));
    }

    @Test
    void testCancelOrder() {
        // Mocking
        String orderId = "order-1";
        UpdateOrderRequest updateOrderRequest = new UpdateOrderRequest();
        UpdateOrderResponse placeOrderResponse = new UpdateOrderResponse()
            .orderId(orderId);
        when(ordersApi.updateOrder(orderId, updateOrderRequest)).thenReturn(placeOrderResponse);

        // Test
        tradeOrderService.cancel(orderId);

        // Assertions
        verify(ordersApi).updateOrder(orderId, updateOrderRequest);
        verify(orderMapper).mapToPlaceOrderResponse(placeOrderResponse.getOrderId());
    }

    @Test
    void testCancelOrderThrowsException() {
        // Mocking
        String orderId = "order-1";
        UpdateOrderRequest updateOrderRequest = new UpdateOrderRequest();
        when(ordersApi.updateOrder(orderId, updateOrderRequest)).thenThrow(new RuntimeException());

        // Test & Assertions
        Assertions.assertThrows(InternalServerErrorException.class, () -> tradeOrderService.cancel(orderId));
    }

    @Test
    void testCalculateFee() {
        CommissionsOrderResponse commissionsOrderResponse = tradeOrderService.calculateFee();
        assertNotNull(commissionsOrderResponse);
        assertNotNull(commissionsOrderResponse.getCommissionFees());
        assertNotNull(commissionsOrderResponse.getFxCurrencyCommission());
        assertEquals(BigDecimal.ZERO, commissionsOrderResponse.getCommissionFees().getAmount());
        assertEquals("USD", commissionsOrderResponse.getCommissionFees().getCurrency());
        assertEquals(BigDecimal.ZERO, commissionsOrderResponse.getFxCurrencyCommission().getAmount());
        assertEquals("USD", commissionsOrderResponse.getFxCurrencyCommission().getCurrency());
    }
}
