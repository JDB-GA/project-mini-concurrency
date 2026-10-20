package com.almotawaj.concurrency.model;

import com.almotawaj.concurrency.constant.RaiseConstants;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        return Arrays.stream(Role.values())
                .collect(Collectors.toMap(Function.identity(), Role::getRaiseRate,
                        (a, b) -> a, () -> new EnumMap<>(Role.class)));
    }
}
