package com.backbase.modelbank.mapper;

import static com.backbase.modelbank.util.InstrumentConstant.COMMA_SEPARATOR;
import static com.backbase.modelbank.util.InstrumentConstant.DATE_INDEX;
import static com.backbase.modelbank.util.InstrumentConstant.HUNDRED;
import static com.backbase.modelbank.util.InstrumentConstant.PIPE_SEPARATOR;
import static com.backbase.modelbank.util.InstrumentConstant.USD_CURRENCY;

import com.backbase.dbs.portfolio.service.instrument.rest.spec.model.InstrumentDetailsGet;
import com.backbase.dbs.portfolio.service.instrument.rest.spec.model.InstrumentViewChartItem;
import com.backbase.dbs.portfolio.service.instrument.rest.spec.model.Money;
import com.backbase.drivewealth.clients.instrument.model.InstrumentDetail;
import com.backbase.drivewealth.clients.marketdata.model.HistoricalChartResponse;
import com.backbase.drivewealth.clients.marketdata.model.QuotesResponse;
import com.backbase.modelbank.model.PriceType;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;


@Mapper(componentModel = "spring")
public interface InstrumentMapper {

    @Mapping(target = "priceData.price.amount", source = "instrumentDetail", qualifiedByName = "mapPrice")
    @Mapping(target = "priceData.price.currencyCode", constant = "USD")
    @Mapping(target = "priceData.pricePerformance", source = "instrumentDetail", qualifiedByName = "mapPricePerformance")
    @Mapping(target = "priceData.pricePerformanceAbs", source = "instrumentDetail", qualifiedByName = "mapPricePerformanceAbs")
    @Mapping(target = "priceData.askPrice", source = "instrumentDetail", qualifiedByName = "mapAskPrice")
    @Mapping(target = "priceData.bidPrice", source = "instrumentDetail", qualifiedByName = "mapBidPrice")
    @Mapping(target = "priceData.askVolume", expression = "java(quotes.getAskSize())")
    @Mapping(target = "priceData.bidVolume", expression = "java(quotes.getBidSize())")
    @Mapping(target = "priceData.totalVolume", source = "instrumentDetail.fundamentalDataModel.cumulativeVolume")
    @Mapping(target = "priceData.closePrice", source = "instrumentDetail", qualifiedByName = "mapClosePrice")
    @Mapping(target = "priceData.openPrice", source = "instrumentDetail.fundamentalDataModel.openPrice")
    @Mapping(target = "priceData.lowPrice", source = "instrumentDetail.fundamentalDataModel.lowPrice")
    @Mapping(target = "priceData.highPrice", source = "instrumentDetail.fundamentalDataModel.highPrice")
    @Mapping(target = "priceData.dayRange.min", source = "instrumentDetail.fundamentalDataModel.lowPrice")
    @Mapping(target = "priceData.dayRange.max", source = "instrumentDetail.fundamentalDataModel.highPrice")
    @Mapping(target = "priceData.week52Range.min", source = "instrumentDetail.fundamentalDataModel.fiftyTwoWeekLowPrice")
    @Mapping(target = "priceData.week52Range.max", source = "instrumentDetail.fundamentalDataModel.fiftyTwoWeekHighPrice")
    @Mapping(target = "keyStatistics.fundStatus", constant = "ACTIVE")
    @Mapping(target = "keyStatistics.totalAssets", source = "instrumentDetail.fundamentalDataModel.marketCap")
    @Mapping(target = "keyStatistics.portfolioAssetsAllocation", source = "instrumentDetail.type")
    @Mapping(target = "keyStatistics.priceToEarningsRatio", source = "instrumentDetail.fundamentalDataModel.peRatio", qualifiedByName = "decimalScale")
    @Mapping(target = "keyStatistics.sharesOutstanding", source = "instrumentDetail.fundamentalDataModel.sharesOutstanding")
    @Mapping(target = "keyStatistics.priceToBookRatio", source = "instrumentDetail.fundamentalDataModel.pbRatio", qualifiedByName = "decimalScale")
    @Mapping(target = "keyStatistics.earningsPerShare", source = "instrumentDetail.fundamentalDataModel.earningsPerShare", qualifiedByName = "decimalScale")
    @Mapping(target = "keyStatistics.dividendYield", source = "instrumentDetail.fundamentalDataModel.dividendYield", qualifiedByName = "decimalScale")
    InstrumentDetailsGet map(InstrumentDetail instrumentDetail, @Context QuotesResponse quotes);

    @Named("mapPrice")
    default BigDecimal mapPrice(InstrumentDetail instrumentDetail, @Context QuotesResponse quotes) {
        var output = firstNonZeroBigDecimal(
            Arrays.asList(quotes.getLastTrade(), quotes.getClose(), quotes.getPriorClose(),
                instrumentDetail.getClose(), instrumentDetail.getClosePrior()));

        return output.orElse(BigDecimal.ZERO);
    }

    @Named("mapAskPrice")
    default BigDecimal mapAskPrice(InstrumentDetail instrumentDetail, @Context QuotesResponse quotes) {
        BigDecimal askPrice = BigDecimal.ZERO;
        if (Objects.nonNull(instrumentDetail.getFundamentalDataModel())) {
            askPrice = instrumentDetail.getFundamentalDataModel().getAskPrice();
        }
        var output = firstNonZeroBigDecimal(Arrays.asList(quotes.getAsk(), askPrice));

        return output.orElse(BigDecimal.ZERO);
    }

    @Named("mapBidPrice")
    default BigDecimal mapBidPrice(InstrumentDetail instrumentDetail, @Context QuotesResponse quotes) {
        BigDecimal bidPrice = BigDecimal.ZERO;
        if (Objects.nonNull(instrumentDetail.getFundamentalDataModel())) {
            bidPrice = instrumentDetail.getFundamentalDataModel().getBidPrice();
        }
        var output = firstNonZeroBigDecimal(Arrays.asList(quotes.getBid(), bidPrice));

        return output.orElse(BigDecimal.ZERO);
    }

    @Named("decimalScale")
    default BigDecimal decimalScale(BigDecimal input) {
        return input != null ? input.setScale(2, RoundingMode.HALF_UP) : null;
    }

    @Named("mapPricePerformance")
    default BigDecimal mapPricePerformance(InstrumentDetail instrumentDetail, @Context QuotesResponse quotes) {
        if (quotes.getChange() != null) {
            return (Objects.requireNonNull(quotes.getChange())
                .divide(quotes.getPriorClose(), 2, RoundingMode.HALF_UP)).multiply(new BigDecimal(HUNDRED));
        } else if (quotes.getPriorClose() != null && quotes.getClose() != null && !Objects.equals(quotes.getClose(),
            BigDecimal.ZERO)) {
            return quotes.getClose().subtract(quotes.getPriorClose()).divide(quotes.getClose(), MathContext.DECIMAL32)
                .multiply(new BigDecimal(HUNDRED)).setScale(2, RoundingMode.DOWN);
        } else if (instrumentDetail.getClosePrior() != null && instrumentDetail.getClose() != null && !Objects.equals(
            instrumentDetail.getClose(),
            BigDecimal.ZERO)) {
            return instrumentDetail.getClose().subtract(instrumentDetail.getClosePrior())
                .divide(instrumentDetail.getClose(), MathContext.DECIMAL32)
                .multiply(new BigDecimal(HUNDRED)).setScale(2, RoundingMode.DOWN);
        }
        return BigDecimal.ZERO;
    }

    @Named("mapPricePerformanceAbs")
    default Money mapPricePerformanceAbs(InstrumentDetail instrumentDetail, @Context QuotesResponse quotes) {
        BigDecimal change = BigDecimal.ZERO;
        if (quotes.getChange() != null) {
            change = quotes.getChange();
        } else if (quotes.getClose() != null && quotes.getPriorClose() != null) {
            change = quotes.getClose().subtract(quotes.getPriorClose());
        } else if (instrumentDetail.getClose() != null && instrumentDetail.getClosePrior() != null) {
            change = instrumentDetail.getClose().subtract(instrumentDetail.getClosePrior());
        }
        return new Money().amount(change).currencyCode(USD_CURRENCY);
    }

    @Named("mapClosePrice")
    default BigDecimal mapClosePrice(InstrumentDetail instrumentDetail, @Context QuotesResponse quotes) {

        var output = firstNonZeroBigDecimal(Arrays.asList(quotes.getClose(), quotes.getPriorClose(),
            instrumentDetail.getClose(), instrumentDetail.getClosePrior()));

        return output.orElse(BigDecimal.ZERO);
    }

    default List<InstrumentViewChartItem> mapHistoryPrice(HistoricalChartResponse dwHistoryResponse) {
        List<InstrumentViewChartItem> historyPrices = new ArrayList<>();
        if (dwHistoryResponse != null && StringUtils.isNotBlank(dwHistoryResponse.getData())) {
            Arrays.asList(dwHistoryResponse.getData().split(PIPE_SEPARATOR)).forEach(bar -> {
                var barData = bar.split(COMMA_SEPARATOR);
                for (PriceType type : PriceType.values()) {
                    historyPrices.add(new InstrumentViewChartItem().price(new BigDecimal(barData[type.getIndex()]))
                        .currency(USD_CURRENCY)
                        .priceType(type.name())
                        .date(OffsetDateTime.parse(barData[DATE_INDEX])));
                }
            });
        }
        return historyPrices;
    }

    private static boolean nonZero(BigDecimal input) {
        return input != null && !Objects.equals(input, BigDecimal.ZERO);
    }

    static Optional<BigDecimal> firstNonZeroBigDecimal(List<BigDecimal> list) {
        return list.stream().filter(Objects::nonNull).filter(InstrumentMapper::nonZero).findFirst();
    }

}
