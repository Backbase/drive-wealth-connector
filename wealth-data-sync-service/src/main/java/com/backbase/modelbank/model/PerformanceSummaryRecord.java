package com.backbase.modelbank.model;


import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountPerformanceResponse;
import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountSummaryResponse;
import com.backbase.drivewealth.reactive.clients.money.model.GetAccountDepositsResponseInner;
import com.backbase.drivewealth.reactive.clients.money.model.GetAccountWithdrawalsResponseInner;

import java.util.List;

public record PerformanceSummaryRecord(
        String accountNo,
        String accountId,
        GetAccountSummaryResponse todaySummaryList,
        GetAccountPerformanceResponse ytdPerformance ,
        List<GetAccountDepositsResponseInner> depositsList,
        List<GetAccountWithdrawalsResponseInner> withdrawalList ) {
}
