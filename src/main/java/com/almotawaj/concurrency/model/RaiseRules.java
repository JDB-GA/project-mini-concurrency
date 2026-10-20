package com.almotawaj.concurrency.model;

import com.almotawaj.concurrency.constant.RaiseConstants;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public record RaiseRules(Map<Role, Double> roleRates, double yearlyRate) {

    public RaiseRules {
        roleRates = Map.copyOf(roleRates);
    }

    public static RaiseRules defaults() {
        return new RaiseRules(defaultRoleRates(), RaiseConstants.YEARLY_RATE);
    }

    public double roleRate(Role role) {
        return roleRates.getOrDefault(role, role.getRaiseRate());
    }

    private static Map<Role, Double> defaultRoleRates() {
        return Arrays.stream(Role.values())
                .collect(Collectors.toMap(Function.identity(), Role::getRaiseRate,
                        (a, b) -> a, () -> new EnumMap<>(Role.class)));
    }
}
