package com.backbase.modelbank.mapper;

import com.backbase.arrangments.api.integration.v2.model.ArrangementItemResponseBody;
import com.backbase.arrangments.api.integration.v2.model.PutArrangement;
import com.backbase.arrangments.balance.api.integration.v2.model.BalanceItem;
import com.backbase.drivewealth.clients.accounts.model.GetAccountSummaryResponse;
import java.util.Objects;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PortfolioBalanceMapper {

    @Mapping(target = "arrangementId", source = "id")
    @Mapping(target = "bookedBalance", source = "bookedBalance")
    @Mapping(target = "availableBalance", source = "availableBalance")
    BalanceItem mapBalanceItem(PutArrangement putArrangement);

    @Mapping(target = "productId", ignore = true)
    PutArrangement mapPutArrangement(ArrangementItemResponseBody responseBody);

    default PutArrangement mapPutArrangement(PutArrangement putArrangement, GetAccountSummaryResponse summary) {
        putArrangement
            .bookedBalance(Objects.requireNonNull(Objects.requireNonNull(summary.getAccountSummary()).getCash())
                .getCashAvailableForTrade())
            .availableBalance(summary.getAccountSummary().getCash().getCashAvailableForTrade());
        return putArrangement;
    }
}
