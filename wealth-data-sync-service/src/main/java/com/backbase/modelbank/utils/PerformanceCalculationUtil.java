package com.backbase.modelbank.utils;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public final class PerformanceCalculationUtil {

    private PerformanceCalculationUtil() {
    }

    public static OffsetDateTime get1stDayOfYear() {
        return OffsetDateTime.now().withDayOfMonth(1).withMonth(1);
    }

    public static LocalDate get1stDateOfMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }

    public static OffsetDateTime get1stDateOfMonthOffset() {
        return OffsetDateTime.now().withDayOfMonth(1);
    }

}
