package com.example.salarygrows;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.text.NumberFormat;
import java.util.Locale;

public class SalaryCalc {
    public static final long SALARY = 2500000L;
    public static double fraction() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime start = now.withDayOfMonth(1).toLocalDate().atStartOfDay(now.getZone());
        ZonedDateTime end = start.plusMonths(1);
        double elapsed = Duration.between(start, now).toMillis();
        double total = Duration.between(start, end).toMillis();
        return Math.max(0, Math.min(1, elapsed / total));
    }
    public static String won(long n) { return NumberFormat.getNumberInstance(Locale.KOREA).format(n) + "원"; }
    public static long earned() { return (long)Math.floor(SALARY * fraction()); }
    public static String percent() { return String.format(Locale.KOREA, "%.4f%%", fraction()*100); }
    public static String period() { return LocalDate.now().getMonthValue() + "월 월급 · 매월 10일 지급"; }
}
