package com.backbase.modelbank.model;

import com.backbase.drivewealth.reactive.clients.accounts.model.Account;

import java.util.List;

public record PortfolioSyncTask(List<Account> accounts , String dwUserId, String eventName) {

}
