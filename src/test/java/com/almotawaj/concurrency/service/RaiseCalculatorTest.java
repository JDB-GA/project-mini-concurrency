package com.almotawaj.concurrency.service;

import com.almotawaj.concurrency.model.Employee;
import com.almotawaj.concurrency.model.RaiseRules;
import com.almotawaj.concurrency.model.Role;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RaiseCalculatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);
    private static final double DELTA = 1e-9;

    private final RaiseCalculator calculator = new RaiseCalculator(
            Clock.fixed(TODAY.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()));
    private final RaiseRules rules = RaiseRules.defaults();

    @Test
    void noRaiseBelowSixtyPercentCompletion() {
        assertEquals(0.0, calculator.rateFor(employee(Role.DIRECTOR, TODAY.minusYears(10), 0.59), rules), DELTA);
    }

    @Test
    void exactlySixtyPercentStillGetsRaise() {
        assertEquals(0.02, calculator.rateFor(employee(Role.MANAGER, TODAY, 0.6), rules), DELTA);
    }

    @Test
    void roleRatesWithoutTenure() {
        assertEquals(0.05, calculator.rateFor(employee(Role.DIRECTOR, TODAY.minusMonths(11), 0.7), rules), DELTA);
        assertEquals(0.02, calculator.rateFor(employee(Role.MANAGER, TODAY.minusMonths(11), 0.7), rules), DELTA);
        assertEquals(0.01, calculator.rateFor(employee(Role.EMPLOYEE, TODAY.minusMonths(11), 0.7), rules), DELTA);
    }

    @Test
    void addsTwoPercentPerFullYear() {
        assertEquals(0.01 + 0.06, calculator.rateFor(employee(Role.EMPLOYEE, TODAY.minusYears(3), 0.7), rules), DELTA);
    }

    @Test
    void partialYearIsNotCounted() {
        assertEquals(0.01 + 0.02,
                calculator.rateFor(employee(Role.EMPLOYEE, TODAY.minusYears(2).plusDays(1), 0.7), rules), DELTA);
    }

    @Test
    void bonusMultipliesRoleRateAboveEightyPercent() {
        assertEquals(0.015, calculator.rateFor(employee(Role.EMPLOYEE, TODAY, 0.81), rules), DELTA);
        assertEquals(0.075 + 0.04, calculator.rateFor(employee(Role.DIRECTOR, TODAY.minusYears(2), 0.9), rules), DELTA);
    }

    @Test
    void exactlyEightyPercentHasNoBonus() {
        assertEquals(0.01, calculator.rateFor(employee(Role.EMPLOYEE, TODAY, 0.8), rules), DELTA);
    }

    @Test
    void usesCustomRules() {
        RaiseRules custom = new RaiseRules(Map.of(Role.EMPLOYEE, 0.10), 0.05);

        assertEquals(0.10 + 0.10, calculator.rateFor(employee(Role.EMPLOYEE, TODAY.minusYears(2), 0.7), custom), DELTA);
        assertEquals(0.02, calculator.rateFor(employee(Role.MANAGER, TODAY, 0.7), custom), DELTA);
    }

    @Test
    void futureJoinDateCountsAsZeroYears() {
        assertEquals(0, calculator.yearsWorked(employee(Role.EMPLOYEE, TODAY.plusYears(1), 0.7)));
    }

    private Employee employee(Role role, LocalDate joined, double completion) {
        return new Employee(1, "Test", 1000.0, joined, role, completion);
    }
}
