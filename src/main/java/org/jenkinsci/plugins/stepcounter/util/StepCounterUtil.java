package org.jenkinsci.plugins.stepcounter.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public final class StepCounterUtil {

    /** */
    private static final NumberFormat PERCENT_FORMAT = new DecimalFormat("0.0");

    private StepCounterUtil() {}

    /**
     * Percent
     * @param numerator
     * @param denominator
     * @return
     */
    public static String convertPercent(long numerator, long denominator) {
        return (denominator <= 0) ? ("0.0") : (PERCENT_FORMAT.format(100 * numerator / denominator));
    }
}
