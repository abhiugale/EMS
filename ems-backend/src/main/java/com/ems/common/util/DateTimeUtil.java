package com.ems.common.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class DateTimeUtil {

    public static Instant getStartOfDay(LocalDate date, String timezone) {
        ZoneId zoneId = ZoneId.of(timezone != null ? timezone : "Asia/Kolkata");
        return date.atStartOfDay(zoneId).toInstant();
    }

    public static Instant getEndOfDay(LocalDate date, String timezone) {
        ZoneId zoneId = ZoneId.of(timezone != null ? timezone : "Asia/Kolkata");
        return date.plusDays(1).atStartOfDay(zoneId).toInstant();
    }
}
