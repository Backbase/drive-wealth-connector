package com.backbase.modelbank.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.backbase.arrangments.api.integration.v2.model.ArrangementItemResponseBody;
import com.backbase.arrangments.api.integration.v2.model.PutArrangement;
import com.backbase.arrangments.balance.api.integration.v2.model.BalanceItem;
import com.backbase.drivewealth.clients.accounts.model.GetAccountSummaryResponse;
import com.backbase.drivewealth.clients.accounts.model.GetAccountSummaryResponseAccountSummary;
import com.backbase.drivewealth.clients.accounts.model.GetAccountSummaryResponseAccountSummaryCash;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class PortfolioBalanceMapperTest {

    private PortfolioBalanceMapper portfolioBalanceMapper;

    @BeforeEach
    public void setUp() {
        portfolioBalanceMapper = Mappers.getMapper(PortfolioBalanceMapper.class);
    }

    @Test
    void testMapBalanceItem() {
        PutArrangement putArrangement = new PutArrangement();
        putArrangement.setId("123");
        putArrangement.setBookedBalance(BigDecimal.valueOf(100));
        putArrangement.setAvailableBalance(BigDecimal.valueOf(200));

        BalanceItem balanceItem = portfolioBalanceMapper.mapBalanceItem(putArrangement);

        assertEquals("123", balanceItem.getArrangementId());
        assertEquals(BigDecimal.valueOf(100), balanceItem.getBookedBalance());
        assertEquals(BigDecimal.valueOf(200), balanceItem.getAvailableBalance());
    }

    @Test
    void testMapPutArrangement() {
        ArrangementItemResponseBody responseBody = new ArrangementItemResponseBody();
        responseBody.setId("123");
        responseBody.setBookedBalance(BigDecimal.valueOf(100));
        responseBody.setAvailableBalance(BigDecimal.valueOf(200));

        PutArrangement putArrangement = portfolioBalanceMapper.mapPutArrangement(responseBody);

        assertEquals("123", putArrangement.getId());
        assertEquals(BigDecimal.valueOf(100), putArrangement.getBookedBalance());
        assertEquals(BigDecimal.valueOf(200), putArrangement.getAvailableBalance());
    }

    @Test
    void testMapPutArrangementWithSummary() {
        GetAccountSummaryResponse summary = mock(GetAccountSummaryResponse.class);
        GetAccountSummaryResponseAccountSummary accountSummary = mock(GetAccountSummaryResponseAccountSummary.class);
        GetAccountSummaryResponseAccountSummaryCash cash = mock(GetAccountSummaryResponseAccountSummaryCash.class);
        when(summary.getAccountSummary()).thenReturn(accountSummary);
        when(accountSummary.getCash()).thenReturn(cash);
        when(cash.getCashAvailableForTrade()).thenReturn(BigDecimal.valueOf(100));

        PutArrangement putArrangement = new PutArrangement();

        portfolioBalanceMapper.mapPutArrangement(putArrangement, summary);

        assertEquals(BigDecimal.valueOf(100), putArrangement.getBookedBalance());
        assertEquals(BigDecimal.valueOf(100), putArrangement.getAvailableBalance());
    }
}

