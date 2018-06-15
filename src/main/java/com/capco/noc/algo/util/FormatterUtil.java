package com.capco.noc.algo.util;

import java.text.NumberFormat;

public class FormatterUtil {
    private static final NumberFormat formatter = NumberFormat.getCurrencyInstance();

    public static String formatDouble(double value){
        return formatter.format(value);
    }
}
