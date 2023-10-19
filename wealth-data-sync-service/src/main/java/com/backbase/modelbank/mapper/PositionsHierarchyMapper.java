package com.backbase.modelbank.mapper;

import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountSummaryResponse;
import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountSummaryResponseAccountSummaryEquity;
import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountSummaryResponseAccountSummaryEquityEquityPositionsInner;
import com.backbase.stream.portfolio.model.Money;
import com.backbase.stream.portfolio.model.PortfolioPositionsHierarchy;
import com.backbase.stream.portfolio.model.PortfolioPositionsHierarchy.ItemTypeEnum;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.backbase.modelbank.utils.Constants.*;
import static java.math.BigDecimal.ZERO;
import static java.util.Objects.requireNonNull;

/**
 * Position hierarchy mapper
 */
@Mapper(componentModel = "spring")
public interface PositionsHierarchyMapper {

    default PortfolioPositionsHierarchy map(GetAccountSummaryResponse accountSummaryResponse) {

        return getPortfolioPositionsHierarchy(ItemTypeEnum.ASSET_CLASS, ASSET_CLASS_EXTERNAL_ID,
            ASSET_CLASS_HIERARCHY_NAME,
            accountSummaryResponse).addItemsItem(
            getPortfolioPositionsHierarchy(ItemTypeEnum.REGION, REGION_EXTERNAL_ID, REGION_ITEM_HIERARCHY_NAME,
                accountSummaryResponse).addItemsItem(
                getPortfolioPositionsHierarchy(ItemTypeEnum.COUNTRY, COUNTRY_EXTERNAL_ID, COUNTRY_ITEM_HIERARCHY_NAME,
                    accountSummaryResponse))
        );

    }

    private PortfolioPositionsHierarchy getPortfolioPositionsHierarchy(ItemTypeEnum itemType, String externalId,
        String name, GetAccountSummaryResponse accountSummaryResponse) {

        var summary = accountSummaryResponse.getAccountSummary();
        return new PortfolioPositionsHierarchy()
            .itemType(itemType)
            .externalId(externalId)
            .name(name)
            .percentOfParent(new BigDecimal(HUNDRED_PERCENT))
            .percentOfPortfolio(ZERO)
            .performance(calculatePerformance(accountSummaryResponse))
            .unrealizedPL(
                new Money().amount(calculateUnrealizedPLAmount(summary.getEquity())).currencyCode(USD_CURRENCY))
            .valuation(new Money().amount(summary.getEquity().getEquityValue()).currencyCode(USD_CURRENCY))
            .accruedInterest(new Money().amount(ZERO).currencyCode(USD_CURRENCY));
    }

    default BigDecimal calculatePerformance(GetAccountSummaryResponse accountSummaryResponse) {

        var costBasis = requireNonNull(requireNonNull(
            requireNonNull(accountSummaryResponse.getAccountSummary()).getEquity()).getEquityPositions()).stream().
            map(GetAccountSummaryResponseAccountSummaryEquityEquityPositionsInner::getCostBasis)
            .reduce(ZERO, BigDecimal::add);

        var marketValue = accountSummaryResponse.getAccountSummary().getEquity().getEquityPositions().stream().
            map(GetAccountSummaryResponseAccountSummaryEquityEquityPositionsInner::getMarketValue)
            .reduce(ZERO, BigDecimal::add);

        return !costBasis.equals(ZERO) ? marketValue.min(costBasis).divide(costBasis, DIVISION_SCALE, RoundingMode.HALF_UP)
            .movePointLeft(PLACE_T_MOVE) : ZERO;

    }

    default BigDecimal calculateUnrealizedPLAmount(GetAccountSummaryResponseAccountSummaryEquity equity) {

        return requireNonNull(equity.getEquityPositions()).stream()
            .map(GetAccountSummaryResponseAccountSummaryEquityEquityPositionsInner::getUnrealizedPL)
            .reduce(ZERO, BigDecimal::add);
    }
}
