package com.backbase.modelbank.mapper;

import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountSummaryResponseAccountSummaryEquityEquityPositionsInner;
import com.backbase.modelbank.exceptions.MapperException;
import com.backbase.modelbank.utils.Constants;
import com.backbase.portfolio.api.service.integration.v1.model.InstrumentHoldingsData;
import com.backbase.portfolio.api.service.integration.v1.model.InstrumentHoldingsPostRequest;
import com.backbase.portfolio.api.service.integration.v1.model.InstrumentHoldingsPutRequest;
import com.backbase.portfolio.api.service.integration.v1.model.Money;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.OptionalDouble;

@Mapper(componentModel = "spring")
public interface InstrumentHoldingsMapper {

    default <T extends InstrumentHoldingsData> T mapInstrumentHoldings(String instrumentHoldingExternalId,
                                                                       List<String> portfoliosCodes,
                                                                       Double totalEquity,
                                                                       Double totalCash,
                                                                       List<GetAccountSummaryResponseAccountSummaryEquityEquityPositionsInner> positionList,
                                                                       Class<T> returnType) {

        String instrumentId = positionList.stream().findFirst().orElseThrow().getInstrumentID();


        OptionalDouble avgBuyPrice = positionList.stream().mapToDouble(position -> position.getAvgPrice().doubleValue())
                .average();

        double todayPL = positionList
                .stream()
                .mapToDouble(position -> position.getUnrealizedDayPL().doubleValue())
                .sum();

        double totalHoldings = positionList.stream()
                .mapToDouble(position -> position.getOpenQty().doubleValue())
                .sum();

        double totalPL = positionList
                .stream()
                .mapToDouble(position -> position.getUnrealizedPL().doubleValue())
                .sum();

        double totalPLPct = totalPL / (totalHoldings * avgBuyPrice.getAsDouble()) * 100d;

        OptionalDouble todayPLPct = positionList
                .stream()
                .mapToDouble(position -> position.getUnrealizedDayPLPercent().doubleValue())
                .average();

        double totalMarketValue = Math.abs(positionList.stream().mapToDouble(p -> p.getMarketValue().doubleValue()).sum());
        double pctOfPortfolios = totalEquity + totalCash == 0d ? 0d : ( totalMarketValue / (totalEquity + totalCash) ) * 100d;

        if (returnType == InstrumentHoldingsPostRequest.class) {
            return (T) new InstrumentHoldingsPostRequest()
                    .instrumentId(instrumentId)
                    .externalId(instrumentHoldingExternalId)
                    .portfolioIds(portfoliosCodes)
                    .todayPL(new Money()
                            .amount(BigDecimal.valueOf(todayPL))
                            .currencyCode(Constants.USD_CURRENCY)
                    )
                    .todayPLPct(BigDecimal.valueOf(todayPLPct.orElse(0d)))
                    .totalPL(new Money()
                            .amount(BigDecimal.valueOf(totalPL))
                            .currencyCode(Constants.USD_CURRENCY))
                    .totalPLPct(BigDecimal.valueOf(totalPLPct))
                    .pctOfPortfolios(BigDecimal.valueOf(pctOfPortfolios))
                    .buyPrice(new Money()
                            .amount(BigDecimal.valueOf(avgBuyPrice.orElse(0d)))
                            .currencyCode(Constants.USD_CURRENCY));
        }

        if (returnType == InstrumentHoldingsPutRequest.class) {
            return (T) new InstrumentHoldingsPutRequest()
                    .portfolioIds(portfoliosCodes)
                    .todayPL(new Money()
                            .amount(BigDecimal.valueOf(todayPL))
                            .currencyCode(Constants.USD_CURRENCY)
                    )
                    .todayPLPct(BigDecimal.valueOf(todayPLPct.orElse(0d)))
                    .totalPL(new Money()
                            .amount(BigDecimal.valueOf(totalPL))
                            .currencyCode(Constants.USD_CURRENCY))
                    .totalPLPct(BigDecimal.valueOf(totalPLPct))
                    .pctOfPortfolios(BigDecimal.valueOf(pctOfPortfolios))
                    .buyPrice(new Money()
                            .amount(BigDecimal.valueOf(avgBuyPrice.orElse(0d)))
                            .currencyCode(Constants.USD_CURRENCY));
        }

        throw new MapperException("Class provided is not allowed to map to, " + returnType);
    }
}
