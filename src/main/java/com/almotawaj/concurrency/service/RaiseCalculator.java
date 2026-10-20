package com.almotawaj.concurrency.service;

import com.almotawaj.concurrency.constant.RaiseConstants;
import com.almotawaj.concurrency.model.Employee;
import com.almotawaj.concurrency.model.RaiseRules;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;

@Component
public class RaiseCalculator {

    private final Clock clock;

    public RaiseCalculator(Clock clock) {
        this.clock = clock;
    }

    public double rateFor(Employee employee, RaiseRules rules) {
        if (employee.getCompletion() < RaiseConstants.MIN_COMPLETION) {
            return RaiseConstants.NO_RAISE;
        }

        return roleRate(employee, rules) + tenureRate(employee, rules);
    }

    public int yearsWorked(Employee employee) {
        return Math.max(0, Period.between(employee.getJoinedDate(), LocalDate.now(clock)).getYears());
    }

    private double roleRate(Employee employee, RaiseRules rules) {
        double rate = rules.roleRate(employee.getRole());

        if (employee.getCompletion() > RaiseConstants.BONUS_COMPLETION) {
            return rate * RaiseConstants.BONUS_MULTIPLIER;
        }

        return rate;
    }

    private double tenureRate(Employee employee, RaiseRules rules) {
        int years = yearsWorked(employee);

        if (years < RaiseConstants.MIN_YEARS_FOR_TENURE) {
            return RaiseConstants.NO_RAISE;
        }

        return years * rules.yearlyRate();
    }
}
