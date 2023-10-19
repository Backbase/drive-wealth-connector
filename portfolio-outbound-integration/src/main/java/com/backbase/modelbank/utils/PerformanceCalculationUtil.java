package com.backbase.modelbank.utils;

import com.backbase.dbs.portfolio.integration.portfolio.rest.spec.model.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public interface PerformanceCalculationUtil {


    static OffsetDateTime get1stDayOfYear() {
        return OffsetDateTime.now().withDayOfMonth(1).withMonth(1);
    }

    static LocalDate get1stDateOfMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }


    static OffsetDateTime toOffsetDateTime(LocalDate dateTime) {
        return OffsetDateTime.of(dateTime, LocalTime.MIN, ZoneOffset.UTC);
    }

    static Money getMoneyValue(BigDecimal value) {
        return getMoneyValue(value, Constants.USD_CURRENCY);
    }

    static Money getMoneyValue(BigDecimal value, String currency) {
        return new Money()
                .amount(value)
                .currency(currency);
    }
}
