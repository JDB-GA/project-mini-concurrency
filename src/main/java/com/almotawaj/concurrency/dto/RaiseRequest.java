package com.almotawaj.concurrency.dto;

import com.almotawaj.concurrency.constant.ErrorConstants;
import com.almotawaj.concurrency.constant.RaiseConstants;
import com.almotawaj.concurrency.exception.InvalidRaiseRequestException;
import com.almotawaj.concurrency.model.RaiseRules;
import com.almotawaj.concurrency.model.Role;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

public record RaiseRequest(BigDecimal director, BigDecimal manager, BigDecimal employee, BigDecimal yearly) {

    public RaiseRules toRules() {
        Map<Role, BigDecimal> roleRates = new EnumMap<>(Role.class);
        roleRates.put(Role.DIRECTOR, toRate(director, Role.DIRECTOR.getRaiseRate()));
        roleRates.put(Role.MANAGER, toRate(manager, Role.MANAGER.getRaiseRate()));
        roleRates.put(Role.EMPLOYEE, toRate(employee, Role.EMPLOYEE.getRaiseRate()));

        return new RaiseRules(roleRates, toRate(yearly, RaiseConstants.YEARLY_RATE));
    }

    private static BigDecimal toRate(BigDecimal percent, BigDecimal fallback) {
        if (percent == null) {
            return fallback;
        }

        if (percent.signum() < 0 || percent.compareTo(RaiseConstants.PERCENT) > 0) {
            throw new InvalidRaiseRequestException(ErrorConstants.INVALID_RATE);
        }

        return percent.divide(RaiseConstants.PERCENT, RaiseConstants.RATE_SCALE, RoundingMode.HALF_UP);
    }
}
