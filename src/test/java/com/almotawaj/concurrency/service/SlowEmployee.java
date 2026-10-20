package com.almotawaj.concurrency.service;

import com.almotawaj.concurrency.model.Employee;
import com.almotawaj.concurrency.model.Role;
import com.almotawaj.concurrency.model.SalaryUpdate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

class SlowEmployee extends Employee {

    private static final long UPDATE_DELAY_MILLIS = 5;

    private final AtomicInteger active;
    private final AtomicInteger peak;

    SlowEmployee(int id, LocalDate joinedDate, AtomicInteger active, AtomicInteger peak) {
        super(id, "Slow" + id, new BigDecimal("1000.00"), joinedDate, Role.EMPLOYEE, 0.7);
        this.active = active;
        this.peak = peak;
    }

    @Override
    public SalaryUpdate applyRaise(BigDecimal rate) {
        peak.accumulateAndGet(active.incrementAndGet(), Math::max);
        try {
            Thread.sleep(UPDATE_DELAY_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        active.decrementAndGet();
        return super.applyRaise(rate);
    }
}
