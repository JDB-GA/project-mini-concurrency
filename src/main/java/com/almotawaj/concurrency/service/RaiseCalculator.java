package com.almotawaj.concurrency.service;

import com.almotawaj.concurrency.constant.RaiseConstants;
import com.almotawaj.concurrency.model.Employee;
import com.almotawaj.concurrency.model.RaiseRules;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;

@Component
public class RaiseCalculator {

    private final Clock clock;

    public RaiseCalculator(Clock clock) {
        this.clock = clock;
    }

    public BigDecimal rateFor(Employee employee, RaiseRules rules) {
        if (employee.getCompletion() < RaiseConstants.MIN_COMPLETION) {
            return RaiseConstants.NO_RAISE;
        }

        return roleRate(employee, rules)
                .add(tenureRate(employee, rules))
                .setScale(RaiseConstants.RATE_SCALE, RoundingMode.HALF_UP);
    }

    public int yearsWorked(Employee employee) {
        return Math.max(0, Period.between(employee.getJoinedDate(), LocalDate.now(clock)).getYears());
    }

    private BigDecimal roleRate(Employee employee, RaiseRules rules) {
        BigDecimal rate = rules.roleRate(employee.getRole());

        if (employee.getCompletion() > RaiseConstants.BONUS_COMPLETION) {
            return rate.multiply(RaiseConstants.BONUS_MULTIPLIER);
        }

        return rate;
    }

    private BigDecimal tenureRate(Employee employee, RaiseRules rules) {
        int years = yearsWorked(employee);

        if (years < RaiseConstants.MIN_YEARS_FOR_TENURE) {
            return RaiseConstants.NO_RAISE;
        }

        return rules.yearlyRate().multiply(BigDecimal.valueOf(years));
    }
}
