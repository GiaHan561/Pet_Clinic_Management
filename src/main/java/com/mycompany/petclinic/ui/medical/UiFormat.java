package com.mycompany.petclinic.ui.medical;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public final class UiFormat {

    private static final DateTimeFormatter DATE_STRICT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter TIME_STRICT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private UiFormat() {
    }

    public static String money(BigDecimal value) {
        return value == null ? "" : String.format("%,.0f đ", value);
    }

    public static String days(Integer value) {
        return value == null ? "" : value + " ngày";
    }

    public static LocalDate parseDate(String text, String name) {
        try {
            return LocalDate.parse(text.trim(), DATE_STRICT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(name + " phải có dạng dd/MM/yyyy (ví dụ 24/11/2026).");
        }
    }

    public static LocalDate optionalDate(String text, String name) {
        return text.trim().isEmpty() ? null : parseDate(text, name);
    }

    public static LocalDateTime parseDateTime(String text, String name) {
        try {
            return LocalDateTime.parse(text.trim(), TIME_STRICT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(name + " phải có dạng dd/MM/yyyy HH:mm (ví dụ 24/11/2026 09:30).");
        }
    }
}