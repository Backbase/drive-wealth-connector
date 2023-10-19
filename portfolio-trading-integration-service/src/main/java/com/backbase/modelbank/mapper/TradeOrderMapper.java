package com.backbase.modelbank.mapper;

import static java.util.Objects.nonNull;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.portfolio.service.trading.rest.spec.model.OrderType;
import com.backbase.dbs.portfolio.service.trading.rest.spec.model.PlaceOrderRequest;
import com.backbase.dbs.portfolio.service.trading.rest.spec.model.PlaceOrderResponse;
import com.backbase.dbs.portfolio.service.trading.rest.spec.model.TradeDirection;
import com.backbase.drivewealth.clients.instrument.model.InstrumentDetail;
import com.backbase.drivewealth.clients.orders.model.CreateOrderRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface TradeOrderMapper {

    @Mapping(target = "accountNo", source = "placeOrderRequest.portfolioId")
    @Mapping(target = "orderType", source = "placeOrderRequest.orderType", qualifiedByName = "orderType")
    @Mapping(target = "symbol", source = "instrumentResponse.symbol")
    @Mapping(target = "side", source = "placeOrderRequest.direction", qualifiedByName = "side")
    @Mapping(target = "quantity", source = "placeOrderRequest.quantity")
    @Mapping(target = "amountCash", source = "placeOrderRequest", qualifiedByName = "amount")
    @Mapping(target = "price", source = "placeOrderRequest.limitPrice.amount")
    @Mapping(target = "expiration", source = "placeOrderRequest.expirationDate", qualifiedByName = "expirationDate")
    @Mapping(target = "commission", ignore = true)
    @Mapping(target = "clientNotes", ignore = true)
    @Mapping(target = "preventQueuing", ignore = true)
    @Mapping(target = "extendedHours", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    @Mapping(target = "timeInForce", ignore = true)
    CreateOrderRequest mapToCreateOrderRequest(PlaceOrderRequest placeOrderRequest,
        InstrumentDetail instrumentResponse);


    @Named("amount")
    default BigDecimal mapAmount(PlaceOrderRequest placeOrderRequest) {
        if (Objects.isNull(placeOrderRequest.getQuantity())) {
            return placeOrderRequest.getAmount().getAmount();
        }
        return null;
    }

    @Mapping(target = "status", expression = "java(com.backbase.dbs.portfolio.service.trading.rest.spec.model.PlacedOrderStatus.PENDING)")
    @Mapping(target = "orderId", source = "id")
    PlaceOrderResponse mapToPlaceOrderResponse(String id);

    @Named("orderType")
    default String orderType(OrderType orderType) {
        if (orderType == OrderType.MARKET_ORDER) {
            return "MARKET";
        } else if (orderType == OrderType.LIMIT_ORDER) {
            return "LIMIT";
        }
        throw new BadRequestException("Order Type Not Supported");
    }

    @Named("side")
    default String side(TradeDirection direction) {
        return direction.toString();
    }

    @Named("expirationDate")
    default String expirationDate(LocalDate expirationDate) {
        if(nonNull(expirationDate)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return expirationDate.format(formatter);
        }
       return null;
    }
}
