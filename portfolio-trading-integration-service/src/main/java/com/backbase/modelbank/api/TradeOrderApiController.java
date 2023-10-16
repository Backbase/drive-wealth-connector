package com.backbase.modelbank.api;

import com.backbase.dbs.portfolio.service.trading.rest.spec.api.TradeOrderApi;
import com.backbase.dbs.portfolio.service.trading.rest.spec.model.CommissionsOrderRequest;
import com.backbase.dbs.portfolio.service.trading.rest.spec.model.CommissionsOrderResponse;
import com.backbase.dbs.portfolio.service.trading.rest.spec.model.PlaceOrderRequest;
import com.backbase.dbs.portfolio.service.trading.rest.spec.model.PlaceOrderResponse;
import com.backbase.modelbank.service.TradeOrderService;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TradeOrderApiController implements TradeOrderApi {

    private final TradeOrderService tradeOrderService;

    @Override
    public ResponseEntity<CommissionsOrderResponse> calculateOrderCommissions(
        CommissionsOrderRequest commissionsOrderRequest, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(tradeOrderService.calculateFee());
    }

    @Override
    public ResponseEntity<PlaceOrderResponse> cancelOrder(String id, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(tradeOrderService.cancel(id));
    }

    @Override
    public ResponseEntity<PlaceOrderResponse> placeOrder(PlaceOrderRequest placeOrderRequest,
        HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(tradeOrderService.create(placeOrderRequest));
    }
}
