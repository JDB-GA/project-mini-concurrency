package com.almotawaj.concurrency.dto;

import com.almotawaj.concurrency.constant.ErrorConstants;
import com.almotawaj.concurrency.constant.RaiseConstants;
import com.almotawaj.concurrency.exception.InvalidRaiseRequestException;
import com.almotawaj.concurrency.model.RaiseRules;
import com.almotawaj.concurrency.model.Role;

import java.util.EnumMap;
import java.util.Map;

public record RaiseRequest(Double director, Double manager, Double employee, Double yearly) {

    public RaiseRules toRules() {
        Map<Role, Double> roleRates = new EnumMap<>(Role.class);
        roleRates.put(Role.DIRECTOR, toRate(director, Role.DIRECTOR.getRaiseRate()));
        roleRates.put(Role.MANAGER, toRate(manager, Role.MANAGER.getRaiseRate()));
        roleRates.put(Role.EMPLOYEE, toRate(employee, Role.EMPLOYEE.getRaiseRate()));

        return new RaiseRules(roleRates, toRate(yearly, RaiseConstants.YEARLY_RATE));
    }

    private static double toRate(Double percent, double fallback) {
        if (percent == null) {
            return fallback;
        }

        if (percent < 0 || percent > RaiseConstants.PERCENT) {
            throw new InvalidRaiseRequestException(ErrorConstants.INVALID_RATE);
        }

        return percent / RaiseConstants.PERCENT;
    }
}
