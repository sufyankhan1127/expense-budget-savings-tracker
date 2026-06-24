package com.ebstracker.expense_budget_savings_tracker.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DISPLAY_FORMAT);
    }

    public static LocalDate getStartOfCurrentMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }

    public static LocalDate getEndOfCurrentMonth() {
        return LocalDate.now().withDayOfMonth(
                LocalDate.now().lengthOfMonth());
    }
}