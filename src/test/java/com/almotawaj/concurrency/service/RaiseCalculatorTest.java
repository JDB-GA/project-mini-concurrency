package com.almotawaj.concurrency.service;

import com.almotawaj.concurrency.model.Employee;
import com.almotawaj.concurrency.model.RaiseRules;
import com.almotawaj.concurrency.model.Role;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RaiseCalculatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);

    private final RaiseCalculator calculator = new RaiseCalculator(
            Clock.fixed(TODAY.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()));
    private final RaiseRules defaults = RaiseRules.defaults();

    @Test
    void noRaiseBelowSixtyPercentCompletion() {
        assertRate("0", employee(Role.DIRECTOR, TODAY.minusYears(10), 0.59), defaults);
    }

    @Test
    void exactlySixtyPercentStillGetsRaise() {
        assertRate("0.02", employee(Role.MANAGER, TODAY, 0.6), defaults);
    }

    @Test
    void roleRatesWithoutTenure() {
        assertRate("0.05", employee(Role.DIRECTOR, TODAY.minusMonths(11), 0.7), defaults);
        assertRate("0.02", employee(Role.MANAGER, TODAY.minusMonths(11), 0.7), defaults);
        assertRate("0.01", employee(Role.EMPLOYEE, TODAY.minusMonths(11), 0.7), defaults);
    }

    @Test
    void addsTwoPercentPerFullYear() {
        assertRate("0.07", employee(Role.EMPLOYEE, TODAY.minusYears(3), 0.7), defaults);
    }

    @Test
    void partialYearIsNotCounted() {
        assertRate("0.03", employee(Role.EMPLOYEE, TODAY.minusYears(2).plusDays(1), 0.7), defaults);
    }

    @Test
    void bonusMultipliesRoleRateAboveEightyPercent() {
        assertRate("0.015", employee(Role.EMPLOYEE, TODAY, 0.81), defaults);
        assertRate("0.115", employee(Role.DIRECTOR, TODAY.minusYears(2), 0.9), defaults);
    }

    @Test
    void exactlyEightyPercentHasNoBonus() {
        assertRate("0.01", employee(Role.EMPLOYEE, TODAY, 0.8), defaults);
    }

    @Test
    void usesCustomRules() {
        RaiseRules custom = new RaiseRules(Map.of(
                Role.DIRECTOR, new BigDecimal("0.08"),
                Role.MANAGER, new BigDecimal("0.04"),
                Role.EMPLOYEE, new BigDecimal("0.10")), new BigDecimal("0.05"));

        assertRate("0.20", employee(Role.EMPLOYEE, TODAY.minusYears(2), 0.7), custom);
        assertRate("0.04", employee(Role.MANAGER, TODAY, 0.7), custom);
        assertRate("0.08", employee(Role.DIRECTOR, TODAY, 0.7), custom);
    }

    @Test
    void futureJoinDateCountsAsZeroYears() {
        assertEquals(0, calculator.yearsWorked(employee(Role.EMPLOYEE, TODAY.plusYears(1), 0.7)));
    }

    private void assertRate(String expected, Employee employee, RaiseRules rules) {
        BigDecimal actual = calculator.rateFor(employee, rules);
        assertEquals(0, new BigDecimal(expected).compareTo(actual), "expected " + expected + " but was " + actual);
    }

    private Employee employee(Role role, LocalDate joined, double completion) {
        return new Employee(1, "Test", new BigDecimal("1000.00"), joined, role, completion);
    }
}
