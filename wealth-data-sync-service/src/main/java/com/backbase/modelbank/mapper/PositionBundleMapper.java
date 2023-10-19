package com.backbase.modelbank.mapper;

import static com.backbase.modelbank.utils.Constants.ACCEPTED_STATUS;
import static com.backbase.modelbank.utils.Constants.DIVISION_SCALE;
import static com.backbase.modelbank.utils.Constants.HYPHEN;
import static com.backbase.modelbank.utils.Constants.PLACE_T_MOVE;
import static com.backbase.modelbank.utils.Constants.SECURITY_POSITION_TYPE;
import static com.backbase.modelbank.utils.Constants.TXN_STATUS_ID;
import static com.backbase.modelbank.utils.Constants.UNREALIZED_PL_LOCAL_ADDITION;
import static com.backbase.modelbank.utils.Constants.UNREALIZED_PL_PCT_LOCAL_ADDITION;
import static com.backbase.modelbank.utils.Constants.USD_CURRENCY;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.joinWith;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.backbase.drivewealth.reactive.clients.accounts.model.GetAccountSummaryResponse;
import com.backbase.drivewealth.reactive.clients.instrument.model.InstrumentDetail;
import com.backbase.drivewealth.reactive.clients.orders.model.Order;
import com.backbase.drivewealth.reactive.clients.transactions.model.Transaction;
import com.backbase.drivewealth.reactive.clients.transactions.model.TransactionInstrument;
import com.backbase.portfolio.trading.integration.api.service.v1.OrderType;
import com.backbase.stream.portfolio.model.Money;
import com.backbase.stream.portfolio.model.Position;
import com.backbase.stream.portfolio.model.PositionBundle;
import com.backbase.stream.portfolio.model.PositionTransaction;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

/**
 * Position bundle mapper.
 */
@Mapper(componentModel = "spring")
public interface PositionBundleMapper {

    default List<Position> mapPositions(GetAccountSummaryResponse accountSummaryResponse) {
        if (nonNull(accountSummaryResponse) && !isEmpty(requireNonNull(
            requireNonNull(accountSummaryResponse.getAccountSummary()).getEquity()).getEquityPositions())) {
            return accountSummaryResponse.getAccountSummary().getEquity().getEquityPositions().stream()
                .map(dwPosition -> new Position()
                    .externalId(joinWith(HYPHEN, accountSummaryResponse.getAccountSummary().getAccountNo(),
                        dwPosition.getSymbol()))
                    .instrumentId(dwPosition.getInstrumentID())
                    .absolutePerformance(new Money().amount(dwPosition.getUnrealizedPL()).currencyCode(USD_CURRENCY))
                    .relativePerformance(requireNonNull(dwPosition.getUnrealizedPL())
                        .divide(dwPosition.getCostBasis(), DIVISION_SCALE, RoundingMode.HALF_UP)
                        .movePointLeft(PLACE_T_MOVE))
                    .portfolioCode(accountSummaryResponse.getAccountSummary().getAccountNo())
                    .purchasePrice(new Money().amount(dwPosition.getAvgPrice()).currencyCode(USD_CURRENCY))
                    .unrealizedPLPct(
                        requireNonNull(dwPosition.getUnrealizedPL())
                            .divide(dwPosition.getCostBasis(), DIVISION_SCALE, RoundingMode.HALF_UP)
                            .movePointLeft(PLACE_T_MOVE))
                    .unrealizedPLLocalPct(requireNonNull(dwPosition.getUnrealizedPL())
                        .divide(dwPosition.getCostBasis(), DIVISION_SCALE, RoundingMode.HALF_UP)
                        .movePointLeft(PLACE_T_MOVE))
                    .unrealizedPL(new Money().amount(dwPosition.getUnrealizedPL()).currencyCode(USD_CURRENCY))
                    .unrealizedPLLocal(new Money().amount(dwPosition.getUnrealizedPL()).currencyCode(USD_CURRENCY))
                    .todayPLPct(dwPosition.getUnrealizedDayPLPercent())
                    .todayPL(new Money().amount(dwPosition.getUnrealizedDayPL()).currencyCode(USD_CURRENCY))
                    .accruedInterest(new Money().amount(ZERO).currencyCode(USD_CURRENCY))
                    .quantity(dwPosition.getOpenQty())
                    .valuation(new Money().amount(dwPosition.getMarketValue()).currencyCode(USD_CURRENCY))
                    .costPrice(new Money().amount(dwPosition.getCostBasis()).currencyCode(USD_CURRENCY))
                    .costExchangeRate(new Money().amount(ONE).currencyCode(USD_CURRENCY))
                    .percentAssetClass(requireNonNull(dwPosition.getMarketValue())
                        .divide(accountSummaryResponse.getAccountSummary().getEquity().getEquityValue(), DIVISION_SCALE,
                            RoundingMode.HALF_UP).movePointLeft(PLACE_T_MOVE))
                    .percentParent(requireNonNull(dwPosition.getMarketValue())
                        .divide(accountSummaryResponse.getAccountSummary().getEquity().getEquityValue(), DIVISION_SCALE,
                            RoundingMode.HALF_UP).movePointLeft(PLACE_T_MOVE))
                    .percentPortfolio(requireNonNull(dwPosition.getMarketValue())
                        .divide(requireNonNull(accountSummaryResponse.getAccountSummary().getEquity().getEquityValue())
                                .add(requireNonNull(accountSummaryResponse.getAccountSummary().getCash()).getCashBalance()),
                            DIVISION_SCALE, RoundingMode.HALF_UP).movePointLeft(PLACE_T_MOVE))
                    .positionType(SECURITY_POSITION_TYPE))
                .toList();
        }
        return Collections.emptyList();
    }

    default List<PositionBundle> mapPositionBundles(List<Position> positions, List<Transaction> transactions,
        Map<String, String> ordersMap) {
        var portfolioId = positions.stream().map(Position::getPortfolioCode).findFirst();

        var transactionsByPositions = portfolioId.map(id -> transactions.stream()
            .filter(transaction -> Objects.nonNull(transaction.getInstrument())).collect(
                Collectors.groupingBy(
                    transaction -> joinWith(HYPHEN, id, requireNonNull(transaction.getInstrument()).getSymbol()),
                    HashMap::new, Collectors.toCollection(ArrayList::new)))).orElse(new HashMap<>());

        return portfolioId.map(s -> transactionsByPositions.entrySet().stream()
            .map(stringArrayListEntry -> new PositionBundle()
                .position(getPosition(stringArrayListEntry.getKey(), positions,
                    requireNonNull(stringArrayListEntry.getValue().stream().map(Transaction::getInstrument).findFirst()
                        .orElse(null))))
                .portfolioId(s)
                .transactions(
                    stringArrayListEntry.getValue().stream().map(txn -> mapTransaction(txn, ordersMap)).toList()))
            .toList()).orElse(Collections.emptyList());
    }

    default Position getPosition(String key, List<Position> positions, TransactionInstrument instrument) {
        return positions.stream()
            .filter(position -> position.getExternalId().equals(key))
            .findFirst().orElse(new Position().externalId(key)
                .instrumentId(instrument.getId()).positionType(SECURITY_POSITION_TYPE));
    }

    default PositionTransaction mapTransaction(Transaction transaction, Map<String, String> ordersMap) {

        return new PositionTransaction()
            .transactionId(transaction.getFinTranID())
            .transactionDate(transaction.getTranWhen())
            .valueDate(transaction.getTranWhen())
            .transactionCategory(transaction.getFinTranTypeID())
            .orderType(getOrderType(transaction, ordersMap))
            .quantity(transaction.getFillQty())
            .price(new Money().amount(transaction.getFillPx()).currencyCode(USD_CURRENCY))
            .amount(new Money().amount(requireNonNull(transaction.getTranAmount()).abs()).currencyCode(USD_CURRENCY))
            .amountGross(
                new Money().amount(requireNonNull(transaction.getTranAmount()).abs().add(transaction.getFeeTaf())
                    .add(transaction.getFeeBase()).add(transaction.getFeeExchange()).add(transaction.getFeeSec())
                    .add(transaction.getFeeXtraShares())).currencyCode(USD_CURRENCY))
            .fxRate(new Money().amount(ZERO).currencyCode(USD_CURRENCY))
            .localTaxes(new Money().amount(ZERO).currencyCode(USD_CURRENCY))
            .localFees(new Money().amount(ZERO).currencyCode(USD_CURRENCY))
            .foreignTaxes(new Money().amount(ZERO).currencyCode(USD_CURRENCY))
            .foreignFees(new Money().amount(ZERO).currencyCode(USD_CURRENCY))
            .officialCode(requireNonNull(transaction.getInstrument()).getId())
            .balanceAsset(
                new Money().amount(requireNonNull(transaction.getAccountBalance()).abs()).currencyCode(USD_CURRENCY))
            .balanceAmount(new Money().amount(transaction.getAccountBalance().abs()).currencyCode(USD_CURRENCY))
            .statusId(TXN_STATUS_ID)
            .statusName(ACCEPTED_STATUS)
            .statusAbbr(ACCEPTED_STATUS);
    }

    private static String getOrderType(Transaction transaction, Map<String, String> ordersMap) {
        return switch (ordersMap.getOrDefault(transaction.getOrderID(), EMPTY)) {
            case "MARKET" -> OrderType.MARKET_ORDER.getValue();
            case "LIMIT" -> OrderType.LIMIT_ORDER.getValue();
            default -> null;
        };
    }

    default PositionBundle updateInstrumentDetails(PositionBundle positionBundle,
        InstrumentDetail instrumentDetail) {
        positionBundle.getTransactions()
            .forEach(transaction -> {
                if (nonNull(instrumentDetail)) {
                    transaction.officialCode(instrumentDetail.getISIN())
                        .ISIN(instrumentDetail.getISIN())
                        .exchange(instrumentDetail.getExchange());
                }
            });
        return positionBundle;
    }

    default List<PositionBundle> mapPositionBundle(Tuple3<GetAccountSummaryResponse, List<Transaction>,
        List<Order>> tuple) {
        return mapPositionBundles(mapPositions(tuple.getT1()), tuple.getT2(),
            tuple.getT3().stream().collect(Collectors.toMap(Order::getId, Order::getType)));
    }


    default PositionBundle updateInstrumentDetailsInPositionBundle(Tuple2<PositionBundle, InstrumentDetail> pTuple) {
        return updateInstrumentDetails(pTuple.getT1(), pTuple.getT2());
    }


}
