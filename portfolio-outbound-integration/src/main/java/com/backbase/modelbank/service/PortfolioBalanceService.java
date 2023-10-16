package com.backbase.modelbank.service;

import com.backbase.arrangments.api.integration.v2.ArrangementsApi;
import com.backbase.arrangments.balance.api.integration.v2.model.BalanceItem;
import com.backbase.drivewealth.clients.accounts.api.AccountsApi;
import com.backbase.modelbank.mapper.PortfolioBalanceMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioBalanceService {

    private final ArrangementsApi arrangementsApi;
    private final AccountsApi accountsApi;
    private final PortfolioBalanceMapper balanceMapper;

    public List<BalanceItem> getBalances(List<String> arrangementIds) {

        log.debug("Trying to get Balance for portfolio/External arrangement Ids [{}] ", arrangementIds);

        return arrangementsApi.getArrangements(arrangementIds)
            .stream()
            .map(balanceMapper::mapPutArrangement)
            .map(putArrangement -> {
                var summary = accountsApi.getAccountSummaryById(putArrangement.getBBAN());
                return balanceMapper.mapPutArrangement(putArrangement, summary);
            })
            .peek(arrangementsApi::putArrangements)
            .map(balanceMapper::mapBalanceItem)
            .toList();
    }
}
