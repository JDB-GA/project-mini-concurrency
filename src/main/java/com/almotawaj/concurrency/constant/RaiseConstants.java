package com.almotawaj.concurrency.constant;

import java.math.BigDecimal;

public final class RaiseConstants {
    public static final BigDecimal YEARLY_RATE = new BigDecimal("0.02");
    public static final BigDecimal BONUS_MULTIPLIER = new BigDecimal("1.5");
    public static final BigDecimal NO_RAISE = BigDecimal.ZERO;
    public static final BigDecimal PERCENT = new BigDecimal("100");
    public static final int MIN_YEARS_FOR_TENURE = 1;
    public static final double MIN_COMPLETION = 0.6;
    public static final double BONUS_COMPLETION = 0.8;
    public static final int MONEY_SCALE = 2;
    public static final int RATE_SCALE = 4;

    private RaiseConstants() {
    }
}
