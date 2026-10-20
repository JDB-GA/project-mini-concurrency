package com.almotawaj.concurrency.model;

import com.almotawaj.concurrency.constant.RaiseConstants;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

public record RaiseRules(Map<Role, BigDecimal> roleRates, BigDecimal yearlyRate) {

    public RaiseRules {
        roleRates = Map.copyOf(roleRates);
    }

    public static RaiseRules defaults() {
        return new RaiseRules(defaultRoleRates(), RaiseConstants.YEARLY_RATE);
    }

    public BigDecimal roleRate(Role role) {
        return roleRates.getOrDefault(role, role.getRaiseRate());
    }

    private static Map<Role, BigDecimal> defaultRoleRates() {
        Map<Role, BigDecimal> rates = new EnumMap<>(Role.class);
        for (Role role : Role.values()) {
            rates.put(role, role.getRaiseRate());
        }
        return rates;
    }
}
