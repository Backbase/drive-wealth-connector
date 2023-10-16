package com.backbase.modelbank.util;

import com.backbase.dbs.portfolio.service.trading.rest.spec.model.CommissionsOrderRequest;
import com.backbase.dbs.portfolio.service.trading.rest.spec.model.Money;
import com.backbase.dbs.portfolio.service.trading.rest.spec.model.OrderType;
import com.backbase.dbs.portfolio.service.trading.rest.spec.model.PlaceOrderRequest;
import com.backbase.dbs.portfolio.service.trading.rest.spec.model.TradeDirection;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TestFactory {

    public static PlaceOrderRequest getCreateOrderRequest(OrderType orderType) {
        return new PlaceOrderRequest()
            .orderId("123")
            .amount(new Money().amount(BigDecimal.valueOf(100)).currency("USD"))
            .instrumentId("a67422af-8504-43df-9e63-7361eb0bd99e")
            .accountId("DSFX000001")
            .portfolioId("DSFX000001")
            .direction(TradeDirection.BUY)
            .orderType(orderType)
            .expirationDate(LocalDate.now());
    }

    public static CommissionsOrderRequest getCommissionsOrderRequest() {
        return new CommissionsOrderRequest()
            .amount(new Money().amount(BigDecimal.valueOf(100)).currency("USD"))
            .instrumentId("a67422af-8504-43df-9e63-7361eb0bd99e")
            .accountId("DSFX000001")
            .portfolioId("DSFX000001")
            .direction(TradeDirection.BUY)
            .orderType(OrderType.LIMIT_ORDER);
    }

}
