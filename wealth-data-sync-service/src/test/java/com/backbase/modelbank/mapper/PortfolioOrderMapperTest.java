package com.backbase.modelbank.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.backbase.drivewealth.reactive.clients.orders.model.Order;
import com.backbase.drivewealth.reactive.clients.orders.model.OrderStatusMessage;
import com.backbase.drivewealth.sqs.event.spec.v1.Payload______;
import com.backbase.drivewealth.sqs.event.spec.v1.StatusMessage;
import com.backbase.modelbank.util.TestFactory;
import com.backbase.portfolio.trading.integration.api.service.v1.ChangeExternalOrderStatusRequest;
import com.backbase.portfolio.trading.integration.api.service.v1.ExternalOrderPostRequest;
import com.backbase.portfolio.trading.integration.api.service.v1.OrderType;
import com.backbase.portfolio.trading.integration.api.service.v1.TargetOrderStatus;
import com.backbase.portfolio.trading.integration.api.service.v1.TradeDirection;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PortfolioOrderMapperTest {

    private final PortfolioOrderMapper mapper = new com.backbase.modelbank.mapper.PortfolioOrderMapperImpl();

    @Test
    void shouldMapOrderToExternalOrderPostRequest() {
        // given
        String instrumentId = "AAPL";
        Order order = getOrder();

        // when
        ExternalOrderPostRequest externalOrderPostRequest = mapper.toExternalOrderPostRequests(order, instrumentId);

        // then
        assertNotNull(externalOrderPostRequest);
        assertEquals("orderId", externalOrderPostRequest.getOrderId());
        assertEquals(OrderType.LIMIT_ORDER, externalOrderPostRequest.getOrderType());
        assertEquals("AAPL", externalOrderPostRequest.getInstrumentId());
        assertEquals("portfolioId", externalOrderPostRequest.getPortfolioId());
        assertEquals("portfolioId", externalOrderPostRequest.getAccountId());
        assertEquals(BigDecimal.valueOf(10), externalOrderPostRequest.getQuantity());
        assertEquals(BigDecimal.valueOf(5), externalOrderPostRequest.getConfirmedQuantity());
        assert externalOrderPostRequest.getConfirmedValue() != null;
        assertEquals(BigDecimal.valueOf(100), externalOrderPostRequest.getConfirmedValue().getAmount());
        assertEquals("USD", externalOrderPostRequest.getConfirmedValue().getCurrency());
        assert externalOrderPostRequest.getConfirmedCommissionFees() != null;
        assertEquals(BigDecimal.valueOf(10), externalOrderPostRequest.getConfirmedCommissionFees().getAmount());
        assertEquals("USD", externalOrderPostRequest.getConfirmedCommissionFees().getCurrency());
        assertEquals(BigDecimal.valueOf(50), externalOrderPostRequest.getInstrumentPrice().getAmount());
        assertEquals("USD", externalOrderPostRequest.getInstrumentPrice().getCurrency());
        assertEquals(TradeDirection.BUY, externalOrderPostRequest.getDirection());
        assert externalOrderPostRequest.getLimitPrice() != null;
        assertEquals(BigDecimal.valueOf(55), externalOrderPostRequest.getLimitPrice().getAmount());
        assertEquals("USD", externalOrderPostRequest.getLimitPrice().getCurrency());
        assertEquals(LocalDate.now(), externalOrderPostRequest.getExpirationDate());
        assertEquals(TargetOrderStatus.PENDING, externalOrderPostRequest.getStatus());
        assertEquals("error message", externalOrderPostRequest.getReason());
        assertNotNull(externalOrderPostRequest.getFulfillmentDateTime());
    }

    @Test
    void shouldMapChangeOrderStatusRequest() {
        // given
        Order order = getOrder();
        order.setStatus("CANCELED");
        order.getStatusMessage().setMessage("Order expired.");

        // when
        var changeOrderStatusRequest = mapper.toChangeOrderStatusRequest(order);

        // then
        assertNotNull(changeOrderStatusRequest);
        assertEquals(TargetOrderStatus.EXPIRED, changeOrderStatusRequest.getStatus());
    }

    @Test
    void toChangeOrderStatusRequest_shouldMapPayloadToChangeOrderStatusRequest() throws IOException {
        // given
        Payload______ payload = TestFactory.getOrdersCompletedEvent().getPayload();

        // when
        ChangeExternalOrderStatusRequest changeOrderStatusRequest = mapper.toChangeOrderStatusRequest(payload);

        // then
        assertNotNull(changeOrderStatusRequest);
    }

    @Test
    void getStatusFromPayload_shouldReturnTargetOrderStatus() throws IOException {
        // given
        Payload______ payload = TestFactory.getOrdersCompletedEvent().getPayload();

        // when
        TargetOrderStatus status = mapper.getStatusFromPayload(payload);

        // then
        assertNotNull(status);
    }

    @Test
    void getReasonFromPayload_shouldReturnReasonString() {
        // given
        StatusMessage statusMessage = new StatusMessage().withMessage("something went wrong").withErrorCode("1232");

        // when
        String reason = mapper.getReasonFromPayload(statusMessage);

        // then
        assertNotNull(reason);
    }

    @NotNull
    private static Order getOrder() {
        Order order = new Order();
        order.setId("orderId");
        order.setType("LIMIT");
        order.setAccountNo("portfolioId");
        order.setQuantity(BigDecimal.valueOf(10));
        order.setCumulativeQuantity(BigDecimal.valueOf(5));
        order.setTotalOrderAmount(BigDecimal.valueOf(100));
        order.setFees(BigDecimal.valueOf(10));
        order.setAveragePriceRaw(BigDecimal.valueOf(50));
        order.setSide("BUY");
        order.setPrice(BigDecimal.valueOf(55));
        order.setOrderExpires(OffsetDateTime.now());
        order.setStatus("New");
        OrderStatusMessage orderStatusMessage = new OrderStatusMessage();
        orderStatusMessage.setErrorCode("error");
        orderStatusMessage.setMessage("message");
        order.setStatusMessage(orderStatusMessage);
        order.lastExecuted(OffsetDateTime.now());
        return order;
    }

}