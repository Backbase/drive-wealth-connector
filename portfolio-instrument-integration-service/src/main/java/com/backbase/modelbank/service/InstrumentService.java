package com.backbase.modelbank.service;

import static com.backbase.modelbank.util.InstrumentConstant.DAILY_COMPRESSION;
import static com.backbase.modelbank.util.InstrumentConstant.DW_DATE_PATTERN;
import static com.backbase.modelbank.util.InstrumentConstant.FIVE_MINUTE_COMPRESSION;
import static com.backbase.modelbank.util.InstrumentConstant.THIRTY_MINUTE_COMPRESSION;
import static com.backbase.modelbank.util.InstrumentConstant.UNSET_TRADING_DAYS;
import static com.backbase.modelbank.util.InstrumentConstant.WEEKLY_COMPRESSION;
import static java.time.LocalTime.MAX;
import static java.time.LocalTime.MIN;
import static java.time.OffsetDateTime.parse;
import static java.time.format.DateTimeFormatter.ofPattern;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.portfolio.service.instrument.rest.spec.model.InstrumentDetailsGet;
import com.backbase.dbs.portfolio.service.instrument.rest.spec.model.InstrumentViewChartDataGet;
import com.backbase.modelbank.mapper.InstrumentMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Range;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class InstrumentService {

    private final InstrumentMapper mapper;
    private final DriveWealthService dwService;


    public InstrumentDetailsGet getInstrumentDetails(String id) {
        var instrumentDetail = dwService.getInstrument(id);
        var quotes = dwService.getConsolidatedQuote(instrumentDetail.getSymbol());
        return mapper.map(instrumentDetail, quotes);
    }

    public InstrumentViewChartDataGet getInstrumentChartData(String id, LocalDate fromDate, LocalDate toDate) {
        validateDates(fromDate, toDate);

        return new InstrumentViewChartDataGet()
            .chartData(mapper.mapHistoryPrice(dwService.getInstrumentHistoricalChart(id,
                getCompression(fromDate, toDate),
                parse(LocalDateTime.of(fromDate, MIN.plusSeconds(1)).format(ofPattern(DW_DATE_PATTERN))),
                parse(LocalDateTime.of(toDate, MAX).format(ofPattern(DW_DATE_PATTERN))), UNSET_TRADING_DAYS)))
            .updateDate(OffsetDateTime.now());
    }

    private void validateDates(LocalDate fromDate, LocalDate toDate) {
        if (toDate.isBefore(fromDate)) {
            throw new BadRequestException("End Date should be after Start Date");
        }
    }

    private Integer getCompression(LocalDate fromDate, LocalDate toDate) {

        var period = ChronoUnit.DAYS.between(fromDate, toDate);
        if (Range.between(0L, 5L).contains(period)) {
            return FIVE_MINUTE_COMPRESSION;
        } else if (Range.between(6L, 7L).contains(period)) {
            return THIRTY_MINUTE_COMPRESSION;
        } else if (Range.between(90L, 366L).contains(period)) {
            return DAILY_COMPRESSION;
        } else {
            return WEEKLY_COMPRESSION;
        }
    }
}
