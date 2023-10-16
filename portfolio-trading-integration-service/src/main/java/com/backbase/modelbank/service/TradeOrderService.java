package com.backbase.modelbank.service;

import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.dbs.portfolio.service.trading.rest.spec.model.CommissionsOrderResponse;
import com.backbase.dbs.portfolio.service.trading.rest.spec.model.Money;
import com.backbase.dbs.portfolio.service.trading.rest.spec.model.PlaceOrderRequest;
import com.backbase.dbs.portfolio.service.trading.rest.spec.model.PlaceOrderResponse;
import com.backbase.drivewealth.clients.instrument.api.InstrumentApi;
import com.backbase.drivewealth.clients.orders.api.OrdersApi;
import com.backbase.drivewealth.clients.orders.model.UpdateOrderRequest;
import com.backbase.modelbank.mapper.TradeOrderMapper;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeOrderService {

    private static final String USD_CURRENCY = "USD";
    private static final String CANCEL_METHOD = "CANCEL";

    private final OrdersApi ordersApi;
    private final InstrumentApi instrumentApi;
    private final TradeOrderMapper orderMapper;

    public PlaceOrderResponse create(PlaceOrderRequest placeOrderRequest) {
        try {
            var instrumentResponse = instrumentApi.getInstrumentById(placeOrderRequest.getInstrumentId());
            var createOrderRequest = orderMapper.mapToCreateOrderRequest(placeOrderRequest, instrumentResponse);
            log.info("Creating an order in DW with quantity {}, instrument {} and portfolio {}",
                createOrderRequest.getQuantity(), createOrderRequest.getSymbol(), createOrderRequest.getAccountNo());
            var createOrderResponse = ordersApi.createOrder(createOrderRequest);
            log.info("Order created successfully in DW with id {} and No {}", createOrderResponse.getOrderID(),
                createOrderRequest.getAccountNo());
            return orderMapper.mapToPlaceOrderResponse(createOrderResponse.getOrderID());
        } catch (Exception e) {
            log.error("Error while trying to create an Order {}", placeOrderRequest.getOrderId(), e);
            throw new InternalServerErrorException(e.getMessage());
        }

    }

    public PlaceOrderResponse cancel(String orderId) {
        try {
            var updateOrderResponse = ordersApi.updateOrder(orderId, new UpdateOrderRequest().method(CANCEL_METHOD));
            return orderMapper.mapToPlaceOrderResponse(updateOrderResponse.getOrderId());
        } catch (Exception e) {
            log.error("Error while trying to cancel an Order {}", orderId, e);
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public CommissionsOrderResponse calculateFee() {
        return new CommissionsOrderResponse()
            .commissionFees(new Money().amount(BigDecimal.ZERO).currency(USD_CURRENCY))
            .fxCurrencyCommission(new Money().amount(BigDecimal.ZERO).currency(USD_CURRENCY));
    }
}
