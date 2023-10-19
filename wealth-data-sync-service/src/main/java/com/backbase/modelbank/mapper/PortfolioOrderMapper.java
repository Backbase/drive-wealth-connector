package com.backbase.modelbank.mapper;

import static com.backbase.modelbank.utils.Constants.ORDER_EXPIRED_MESSAGE;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.commons.lang3.StringUtils.joinWith;

import com.backbase.drivewealth.reactive.clients.orders.model.Order;
import com.backbase.drivewealth.reactive.clients.orders.model.OrderStatusMessage;
import com.backbase.drivewealth.sqs.event.spec.v1.Payload______;
import com.backbase.drivewealth.sqs.event.spec.v1.Payload________;
import com.backbase.drivewealth.sqs.event.spec.v1.StatusMessage;
import com.backbase.portfolio.trading.integration.api.service.v1.ChangeExternalOrderStatusRequest;
import com.backbase.portfolio.trading.integration.api.service.v1.ExternalOrderPostRequest;
import com.backbase.portfolio.trading.integration.api.service.v1.OrderType;
import com.backbase.portfolio.trading.integration.api.service.v1.TargetOrderStatus;
import com.backbase.portfolio.trading.integration.api.service.v1.TradeDirection;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring")
public interface PortfolioOrderMapper {

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderType", source = "order.type", qualifiedByName = "getOrderType")
    @Mapping(target = "instrumentId", source = "instrumentId")
    @Mapping(target = "portfolioId", source = "order.accountNo")
    @Mapping(target = "accountId", source = "order.accountNo")
    @Mapping(target = "quantity", source = "order.quantity")
    @Mapping(target = "confirmedQuantity", source = "order.cumulativeQuantity")
    @Mapping(target = "confirmedValue.amount", source = "order.totalOrderAmount")
    @Mapping(target = "confirmedValue.currency", constant = "USD")
    @Mapping(target = "commissionFees.amount", source = "order.fees")
    @Mapping(target = "commissionFees.currency", constant = "USD")
    @Mapping(target = "confirmedCommissionFees.amount", source = "order.fees")
    @Mapping(target = "confirmedCommissionFees.currency", constant = "USD")
    @Mapping(target = "instrumentPrice.amount", source = "order.averagePriceRaw")
    @Mapping(target = "instrumentPrice.currency", constant = "USD")
    @Mapping(target = "confirmedInstrumentPrice.amount", source = "order.averagePriceRaw")
    @Mapping(target = "confirmedInstrumentPrice.currency", constant = "USD")
    @Mapping(target = "confirmedFXCurrencyCommission.amount", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "confirmedFXCurrencyCommission.currency", constant = "USD")
    @Mapping(target = "fxCurrencyCommission.amount", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "fxCurrencyCommission.currency", constant = "USD")
    @Mapping(target = "direction", source = "order.side", qualifiedByName = "getSide")
    @Mapping(target = "limitPrice.amount", source = "order.price", qualifiedByName = "getLimitPrice")
    @Mapping(target = "limitPrice.currency", constant = "USD")
    @Mapping(target = "expirationDate", source = "order.orderExpires", qualifiedByName = "getExpirationDate")
    @Mapping(target = "status", source = "order", qualifiedByName = "getStatus")
    @Mapping(target = "reason", source = "order.statusMessage", qualifiedByName = "getReason")
    @Mapping(target = "fulfillmentDateTime", source = "order.lastExecuted")
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    ExternalOrderPostRequest toExternalOrderPostRequests(Order order, String instrumentId);

    @Named("getLimitPrice")
    default BigDecimal getLimitPrice(BigDecimal price) {
        if (Objects.nonNull(price)) {
            return price;
        }
        return BigDecimal.ZERO;
    }

    @Named("getSide")
    default TradeDirection getSide(String orderSide) {
        if (Objects.equals(orderSide, "BUY")) {
            return TradeDirection.BUY;
        }
        return TradeDirection.SELL;
    }

    @Named("getExpirationDate")
    default LocalDate getExpirationDate(OffsetDateTime orderExpirationDate) {
        if (Objects.nonNull(orderExpirationDate)) {
            return orderExpirationDate.toLocalDateTime().toLocalDate();
        }
        return null;
    }

    @Named("getOrderType")
    default OrderType getOrderType(String orderType) {
        if (Objects.equals(orderType, "LIMIT")) {
            return OrderType.LIMIT_ORDER;
        }
        return OrderType.MARKET_ORDER;
    }

    @Named("getReason")
    default String getReason(OrderStatusMessage orderStatusMessage) {
        if (Objects.nonNull(orderStatusMessage)) {
            return joinWith(SPACE, orderStatusMessage.getErrorCode(), orderStatusMessage.getMessage());
        }
        return EMPTY;
    }

    @Named("getStatus")
    default TargetOrderStatus getStatus(Order order) {
        var status = getStatus(order.getStatus());

        if (status == TargetOrderStatus.CANCELED
            && Objects.nonNull(order.getStatusMessage())
            && Objects.equals(order.getStatusMessage().getMessage(), ORDER_EXPIRED_MESSAGE)) {
            status = TargetOrderStatus.EXPIRED;
        }

        return status;
    }

    @NotNull
    private static TargetOrderStatus getStatus(String status) {
        return switch (Objects.requireNonNull(status)) {
            case "PARTIAL_FILL" -> TargetOrderStatus.PARTIALLY_FILLED;
            case "FILLED" -> TargetOrderStatus.FILLED;
            case "PENDING_CANCEL" -> TargetOrderStatus.PENDING_CANCEL;
            case "REJECTED" -> TargetOrderStatus.REJECTED;
            case "CANCELED" -> TargetOrderStatus.CANCELED;
            default -> TargetOrderStatus.PENDING;
        };
    }

    @Mapping(target = "status", source = "order", qualifiedByName = "getStatus")
    @Mapping(target = "reason", source = "statusMessage", qualifiedByName = "getReason")
    @Mapping(target = "fulfillmentDateTime", source = "lastExecuted")
    @Mapping(target = "confirmedInstrumentPrice.amount", source = "averagePriceRaw")
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @UpdateOrderMapping
    ChangeExternalOrderStatusRequest toChangeOrderStatusRequest(Order order);

    @Mapping(target = "status", source = "payload", qualifiedByName = "getStatusFromPayload")
    @Mapping(target = "reason", source = "statusMessage", qualifiedByName = "getReasonFromPayload")
    @Mapping(target = "fulfillmentDateTime", source = "lastExecuted", qualifiedByName = "getFulfillmentDateTime")
    @Mapping(target = "confirmedInstrumentPrice.amount", source = "averagePrice")
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @UpdateOrderMapping
    ChangeExternalOrderStatusRequest toChangeOrderStatusRequest(Payload______ payload);

    @Named("getStatusFromPayload")
    default TargetOrderStatus getStatusFromPayload(Payload______ payload) {
        var status = getStatus(payload.getStatus());

        if (status == TargetOrderStatus.CANCELED
            && Objects.nonNull(payload.getStatusMessage())
            && Objects.equals(payload.getStatusMessage().getMessage(), ORDER_EXPIRED_MESSAGE)) {
            status = TargetOrderStatus.EXPIRED;
        }

        return status;
    }

    @Named("getReasonFromPayload")
    default String getReasonFromPayload(StatusMessage statusMessage) {
        if (Objects.nonNull(statusMessage)) {
            return joinWith(SPACE, statusMessage.getErrorCode(), statusMessage.getMessage());
        }
        return EMPTY;
    }

    @Named("getFulfillmentDateTime")
    default OffsetDateTime map(ZonedDateTime value) {
        if (value == null) {
            return  OffsetDateTime.now();
        }
        return value.withZoneSameInstant(ZoneId.systemDefault()).toOffsetDateTime();
    }


    Payload______ map(Payload________ payload);
}
