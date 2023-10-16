package com.backbase.modelbank.service;

import static com.backbase.modelbank.util.TestFactory.ACCOUNT_ID;
import static com.backbase.modelbank.util.TestFactory.ACCOUNT_NUMBER;
import static com.backbase.modelbank.util.TestFactory.CREATE_ORDER;
import static com.backbase.modelbank.util.TestFactory.DRIVE_WEALTH_USER_ID;
import static com.backbase.modelbank.util.TestFactory.USER_INTERNAL_ID;
import static com.backbase.modelbank.util.TestFactory.USER_USERNAME;
import static com.backbase.modelbank.util.TestFactory.getAccountArrangementItems;
import static com.backbase.modelbank.util.TestFactory.getPortfolioSummaryRecord;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.drivewealth.reactive.clients.accounts.model.Account;
import com.backbase.drivewealth.reactive.clients.accounts.model.AccountAccountType;
import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountSummaryResponse;
import com.backbase.drivewealth.reactive.clients.instrument.model.InstrumentDetail;
import com.backbase.drivewealth.reactive.clients.orders.model.Order;
import com.backbase.drivewealth.reactive.clients.transactions.model.Transaction;
import com.backbase.modelbank.client.DriveWealthClientWrapper;
import com.backbase.modelbank.client.PortfolioIntegrationClientWrapper;
import com.backbase.modelbank.mapper.PortfolioOrderMapper;
import com.backbase.modelbank.mapper.PositionBundleMapper;
import com.backbase.modelbank.util.TestFactory;
import com.backbase.portfolio.trading.integration.api.service.v1.ExternalOrderPostRequest;
import com.backbase.stream.portfolio.model.PositionBundle;
import com.backbase.stream.portfolio.model.PositionTransaction;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class WealthOrchestrationServiceTest {

    @Mock
    DriveWealthClientWrapper driveWealthClientWrapper;
    @Mock
    PortfolioIntegrationClientWrapper portfolioIntegrationClientWrapper;
    @Mock
    PortfolioOrderMapper portfolioOrderMapper;
    @Mock
    PositionBundleMapper mapper;
    @InjectMocks
    WealthOrchestrationService wealthOrchestrationService;

    @Test
    void testTriggerSynchronization() throws IOException {
        when(portfolioIntegrationClientWrapper.getUserIdFromIdentity(USER_INTERNAL_ID))
            .thenReturn(Mono.just(DRIVE_WEALTH_USER_ID));
        when(portfolioIntegrationClientWrapper.getArrangementByCode(ACCOUNT_NUMBER))
                .thenReturn(Mono.just(getAccountArrangementItems()));
        when(portfolioIntegrationClientWrapper.updatePortfolio(eq(getPortfolioSummaryRecord()), any()))
                .thenReturn(Mono.empty());
        when(portfolioIntegrationClientWrapper.upsertHierarchy(any())).thenReturn(Mono.empty());

        when(portfolioIntegrationClientWrapper.updatePortfolioInstrumentHoldings(any(), anyString()))
                .thenReturn(Flux.empty());

        when(mapper.mapPositions(any(GetAccountSummaryResponse.class))).thenReturn(Collections.emptyList());
        when(mapper.mapPositionBundles(any(), any(), any())).thenReturn(singletonList(new PositionBundle()
            .transactions(singletonList(new PositionTransaction().officialCode("code")))));
        when(mapper.updateInstrumentDetails(any(), any())).thenReturn(new PositionBundle());
        when(driveWealthClientWrapper.getOrdersByAccountId(any(), any()))
            .thenReturn(
                Flux.just(new Order().symbol("AAPL")));
        when(portfolioOrderMapper.toExternalOrderPostRequests(any(), any()))
            .thenReturn(new ExternalOrderPostRequest());
        when(portfolioIntegrationClientWrapper.insertOrder(any(), any())).thenReturn(Mono.empty());


        when(driveWealthClientWrapper.getAccountsByUserId(DRIVE_WEALTH_USER_ID))
                .thenReturn(Mono.just(Collections.singletonList(
                        new Account()
                                .id(ACCOUNT_ID)
                                .accountNo(ACCOUNT_NUMBER)
                                .status(new AccountAccountType().name("OPEN")))
                ));
        when(driveWealthClientWrapper.getPerformanceSummary(ACCOUNT_ID))
                .thenReturn(Mono.just(TestFactory.getPortfolioSummaryRecord()));
        when(driveWealthClientWrapper.getAccountSummary(ACCOUNT_ID))
                .thenReturn(Mono.just(
                   new GetAccountSummaryResponse()
                ));
        when(driveWealthClientWrapper.getTransactionsByAccount(ACCOUNT_ID, CREATE_ORDER))
                .thenReturn(Flux.just(
                        new Transaction()
                                .tranAmount(BigDecimal.ONE)
                                .accountBalance(BigDecimal.ONE)
                ));
        when(driveWealthClientWrapper.getInstruments(Optional.of("code")))
            .thenReturn(Mono.just(new InstrumentDetail()));
        when(driveWealthClientWrapper.getInstrument(any()))
            .thenReturn(Mono.just(new InstrumentDetail()));

        wealthOrchestrationService.triggerSynchronization(USER_INTERNAL_ID, USER_USERNAME, CREATE_ORDER);
        verify(portfolioIntegrationClientWrapper, times(1)).updatePortfolio(any(), any());
    }
}