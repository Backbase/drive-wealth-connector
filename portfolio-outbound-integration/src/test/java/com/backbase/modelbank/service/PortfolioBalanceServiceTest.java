package com.backbase.modelbank.service;

import com.backbase.arrangments.api.integration.v2.ArrangementsApi;
import com.backbase.arrangments.balance.api.integration.v2.model.BalanceItem;
import com.backbase.drivewealth.clients.accounts.api.AccountsApi;
import com.backbase.modelbank.mapper.PortfolioBalanceMapper;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class PortfolioBalanceServiceTest {

    @Mock
    private ArrangementsApi arrangementsApi;

    @Mock
    private AccountsApi accountsApi;

    @Mock
    private PortfolioBalanceMapper balanceMapper;

    private PortfolioBalanceService portfolioBalanceService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        portfolioBalanceService = new PortfolioBalanceService(arrangementsApi, accountsApi, balanceMapper);
    }

    @Test
    void testGetBalances() {
        List<String> arrangementIds = Collections.singletonList("123");

        when(arrangementsApi.getArrangements(arrangementIds))
            .thenReturn(Collections.emptyList());

        List<BalanceItem> balanceItems = portfolioBalanceService.getBalances(arrangementIds);

        verify(arrangementsApi, times(1)).getArrangements(arrangementIds);
        verify(accountsApi, never()).getAccountSummaryById(anyString());
        verify(arrangementsApi, never()).putArrangements(any());
        verify(balanceMapper, never()).mapPutArrangement(any());
        verify(balanceMapper, never()).mapBalanceItem(any());

        assert (balanceItems.isEmpty());
    }
}
