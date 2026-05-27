package com.carpool.carpool.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberFormatUtils {

    private static final DecimalFormat DECIMAL_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.');

        DECIMAL_FORMAT = new DecimalFormat("#0.00", symbols);
    }

    private NumberFormatUtils() {
    }

    public static String formatTwoDecimals(double value) {
        return DECIMAL_FORMAT.format(value);
    }
}