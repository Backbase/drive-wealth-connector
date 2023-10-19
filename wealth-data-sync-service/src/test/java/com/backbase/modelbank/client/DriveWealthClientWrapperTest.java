package com.backbase.modelbank.client;

import static com.backbase.modelbank.util.TestFactory.CREATE_ORDER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.drivewealth.reactive.clients.accounts.api.AccountsApi;
import com.backbase.drivewealth.reactive.clients.accounts.model.Account;
import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountPerformanceResponse;
import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountPerformanceResponsePerformanceInner;
import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountSummaryResponse;
import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountSummaryResponseAccountSummary;
import com.backbase.drivewealth.reactive.clients.instrument.api.InstrumentApi;
import com.backbase.drivewealth.reactive.clients.instrument.model.InstrumentDetail;
import com.backbase.drivewealth.reactive.clients.money.api.DepositsApi;
import com.backbase.drivewealth.reactive.clients.money.api.WithdrawalsApi;
import com.backbase.drivewealth.reactive.clients.money.model.GetAccountDepositsResponseInner;
import com.backbase.drivewealth.reactive.clients.money.model.GetAccountWithdrawalsResponseInner;
import com.backbase.drivewealth.reactive.clients.transactions.api.TransactionsApi;
import com.backbase.drivewealth.reactive.clients.transactions.model.Transaction;
import com.backbase.modelbank.config.DriveWealthConfigurationProperties;
import com.backbase.modelbank.config.DriveWealthConfigurationProperties.Event;
import com.backbase.modelbank.config.DriveWealthConfigurationProperties.Transactions;
import com.backbase.modelbank.mapper.DriveWealthPortfolioMapperImpl;
import com.backbase.modelbank.model.PerformanceSummaryRecord;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class DriveWealthClientWrapperTest {

    public static final String USER_ID = "userId";
    public static final String ACCOUNT_ID = "accountId";
    @Mock
    AccountsApi accountsApi;
    @Mock
    DepositsApi depositsApi;
    @Mock
    WithdrawalsApi withdrawalsApi;
    @Mock
    InstrumentApi instrumentApi;
    @Mock
    TransactionsApi transactionsApi;
    @Mock
    DriveWealthConfigurationProperties properties;
    @Mock
    DriveWealthPortfolioMapperImpl driveWealthPortfolioMapper;
    @InjectMocks
    DriveWealthClientWrapper driveWealthClientWrapper;


    @Test
    void testGetAccountsByUserId() {
        // Given
        when(accountsApi.getAccountByUserId(USER_ID))
            .thenReturn(Flux.just(new Account()));

        // When
        var result = driveWealthClientWrapper.getAccountsByUserId(USER_ID).block();

        // Then
        Assertions.assertNotNull(result);
        verify(accountsApi, times(1)).getAccountByUserId(USER_ID);
    }


    @Test
    void testGetPerformanceSummary() {
        // Given
        when(accountsApi.getAccountPerformanceById(eq(ACCOUNT_ID), any(), any(), any()))
            .thenReturn(Mono.just(new GetAccountPerformanceResponse()
                .performance(List.of())));
        when(accountsApi.getAccountSummaryById(ACCOUNT_ID))
            .thenReturn(Mono.just(new GetAccountSummaryResponse()
                .accountSummary(new GetAccountSummaryResponseAccountSummary())));
        when(depositsApi.listAccountDeposits(ACCOUNT_ID))
            .thenReturn(Flux.fromIterable(List.of(
                new GetAccountDepositsResponseInner())));
        when(withdrawalsApi.listAccountWithdrawals(ACCOUNT_ID))
            .thenReturn(Flux.fromIterable(List.of(
                new GetAccountWithdrawalsResponseInner())));
        when(driveWealthPortfolioMapper.mapPerformanceSummary(any()))
                .thenCallRealMethod();

        // When
        PerformanceSummaryRecord result = driveWealthClientWrapper.getPerformanceSummary(ACCOUNT_ID)
            .block();

        // Then
        Assertions.assertNotNull(result);
        verify(accountsApi, times(1)).getAccountSummaryById(ACCOUNT_ID);
        verify(accountsApi, times(1)).getAccountPerformanceById(eq(ACCOUNT_ID), any(), any(), any());
        verify(depositsApi, times(1)).listAccountDeposits(ACCOUNT_ID);
        verify(withdrawalsApi, times(1)).listAccountWithdrawals(ACCOUNT_ID);
    }

    @Test
    void testGetPerformanceSummaryWhenNonEmptyPerformanceDate() {
        // Given
        when(accountsApi.getAccountPerformanceById(eq(ACCOUNT_ID), any(), any(), any()))
            .thenReturn(Mono.just(new GetAccountPerformanceResponse()
                .performance(List.of(new GetAccountPerformanceResponsePerformanceInner().date(LocalDate.now())))));
        when(accountsApi.getAccountSummaryById(ACCOUNT_ID))
            .thenReturn(Mono.just(new GetAccountSummaryResponse()
                .accountSummary(new GetAccountSummaryResponseAccountSummary())));
        when(depositsApi.listAccountDeposits(ACCOUNT_ID))
            .thenReturn(Flux.fromIterable(List.of(
                new GetAccountDepositsResponseInner())));
        when(withdrawalsApi.listAccountWithdrawals(ACCOUNT_ID))
            .thenReturn(Flux.fromIterable(List.of(
                new GetAccountWithdrawalsResponseInner())));
        when(driveWealthPortfolioMapper.mapPerformanceSummary(any()))
                .thenCallRealMethod();
        // When
        PerformanceSummaryRecord result = driveWealthClientWrapper.getPerformanceSummary(ACCOUNT_ID)
            .block();

        // Then
        Assertions.assertNotNull(result);
        verify(accountsApi, times(1)).getAccountSummaryById(ACCOUNT_ID);
        verify(accountsApi, times(1)).getAccountPerformanceById(eq(ACCOUNT_ID), any(), any(), any());
        verify(depositsApi, times(1)).listAccountDeposits(ACCOUNT_ID);
        verify(withdrawalsApi, times(1)).listAccountWithdrawals(ACCOUNT_ID);
    }

    @Test
    void testGetInstruments() {
        // Given
        when(instrumentApi.getInstrumentById("1234")).thenReturn(Mono.just(new InstrumentDetail().symbol("APPL")));

        // When
        var result = driveWealthClientWrapper.getInstruments(Optional.of("1234")).block();

        // Then
        assertNotNull(result);
        assertEquals("APPL", result.getSymbol());
    }

    @Test
    void testGetInstrumentsWhenEmptylInput() {
        // When
        var result = driveWealthClientWrapper.getInstruments(Optional.empty()).block();

        // Then
        assertNull(result);
    }

    @Test
    void testGetTransactionsByAccount() {
        // Given
        var transaction = new Transactions();
        transaction.setDaysRefreshWindow(Collections.singletonList(new Event(CREATE_ORDER, 1)));
        when(transactionsApi.getTransactionsByAccount(any(), any(), any(), any(), any(), any()))
            .thenReturn(Flux.just(new Transaction()));
        when(properties.getTransactions()).thenReturn(transaction);

        // When
        var result = driveWealthClientWrapper.getTransactionsByAccount("accountId", CREATE_ORDER).collectList().block();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetAccountSummary() {
        // Given
        when(accountsApi.getAccountSummaryById("1234"))
            .thenReturn(Mono.just(new GetAccountSummaryResponse()));

        // When
        var result = driveWealthClientWrapper.getAccountSummary("1234").block();

        // Then
        assertNotNull(result);
    }
}