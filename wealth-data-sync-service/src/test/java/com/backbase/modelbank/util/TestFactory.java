package com.backbase.modelbank.util;

import com.backbase.dbs.arrangement.api.service.v2.model.AccountArrangementItems;
import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountPerformanceResponse;
import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountPerformanceResponsePerformanceInner;
import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountSummaryResponse;
import com.backbase.drivewealth.reactive.clients.money.model.GetAccountDepositsResponseInner;
import com.backbase.drivewealth.reactive.clients.money.model.GetAccountWithdrawalsResponseInner;
import com.backbase.drivewealth.reactive.clients.transactions.model.Transaction;
import com.backbase.drivewealth.sqs.event.spec.v1.OrdersCompletedEvent;
import com.backbase.drivewealth.sqs.event.spec.v1.OrdersUpdatedEvent;
import com.backbase.modelbank.model.PerformanceSummaryRecord;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.util.ResourceUtils;

public class TestFactory {

    public static final String ACCOUNT_NUMBER = "DSEF000003";
    public static final String ACCOUNT_ID = "f1968bbc-4fbe-4b48-bb2d-6e5c4b6c7908.1675847949353";
    public static final String USER_INTERNAL_ID = "internalId";
    public static final String USER_USERNAME = "james";
    public static final String DRIVE_WEALTH_USER_ID_ATTRIBUTE_NAME = "dwUserId";
    public static final String DRIVE_WEALTH_USER_ID = "drive wealth user id";
    public static final String CREATE_ORDER = "Create Order";

    public static GetAccountSummaryResponse getAccountSummary() throws IOException {
        return getObjectMapper().readValue(
            ResourceUtils.getFile("classpath:response/account-summary.json"),
            GetAccountSummaryResponse.class);
    }

    public static List<Transaction> getTransactions() throws IOException {
        return getObjectMapper().readValue(
            ResourceUtils.getFile("classpath:response/transactions.json"),
            new TypeReference<>() {
            });
    }

    private static ObjectMapper getObjectMapper() {
        var objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

    public static PerformanceSummaryRecord getPortfolioSummaryRecord() throws IOException {
        return new PerformanceSummaryRecord(ACCOUNT_NUMBER, ACCOUNT_ID,
                        getAccountSummary(),
                new GetAccountPerformanceResponse().performance(List.of(getGetAccountPerformanceResponsePerformanceInnerMock())),
                List.of(new GetAccountDepositsResponseInner()
                        .timestamp(OffsetDateTime.now())
                        .amount(BigDecimal.valueOf(100f))),
                List.of(new GetAccountWithdrawalsResponseInner()
                        .timestamp(OffsetDateTime.now())
                        .amount(BigDecimal.valueOf(100f))
                ));
    }

    public static GetAccountPerformanceResponsePerformanceInner getGetAccountPerformanceResponsePerformanceInnerMock() {
        return new GetAccountPerformanceResponsePerformanceInner()
                .cash(BigDecimal.valueOf(100))
                .realizedDayPL(BigDecimal.valueOf(100f))
                .unrealizedDayPL(BigDecimal.valueOf(100f))
                .equity(BigDecimal.valueOf(100))
                .date(LocalDate.now())
                .unrealizedDayPL(BigDecimal.valueOf(15));
    }

    public static AccountArrangementItems getAccountArrangementItems() throws IOException {
        return getObjectMapper().readValue(
            ResourceUtils.getFile("classpath:response/arrangement-response.json"),
            AccountArrangementItems.class);
    }

    public static OrdersCompletedEvent getOrdersCompletedEvent() throws IOException {
        return getObjectMapper().readValue(
            ResourceUtils.getFile("classpath:response/order-completed-event.json"),
            OrdersCompletedEvent.class);
    }

    public static OrdersUpdatedEvent getOrdersUpdatedEvent() throws IOException {
        return getObjectMapper().readValue(
            ResourceUtils.getFile("classpath:response/order-completed-event.json"),
            OrdersUpdatedEvent.class);
    }

}
