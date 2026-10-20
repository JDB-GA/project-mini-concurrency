package com.almotawaj.concurrency.service;

import com.almotawaj.concurrency.constant.ErrorConstants;
import com.almotawaj.concurrency.exception.ProcessingException;
import com.almotawaj.concurrency.model.Employee;
import com.almotawaj.concurrency.model.RaiseRules;
import com.almotawaj.concurrency.model.SalaryChange;
import com.almotawaj.concurrency.model.SalaryUpdate;

import java.math.BigDecimal;
import java.util.concurrent.Semaphore;

public class SalaryTask implements Runnable {

    private final Employee employee;
    private final RaiseRules rules;
    private final RaiseCalculator calculator;
    private final Semaphore permits;
    private final ProcessingStats stats;

    public SalaryTask(Employee employee, RaiseRules rules, RaiseCalculator calculator,
                      Semaphore permits, ProcessingStats stats) {
        this.employee = employee;
        this.rules = rules;
        this.calculator = calculator;
        this.permits = permits;
        this.stats = stats;
    }

    @Override
    public void run() {
        BigDecimal rate = calculator.rateFor(employee, rules);
        stats.record(toChange(rate, updateSalary(rate)));
    }

    private SalaryUpdate updateSalary(BigDecimal rate) {
        try {
            permits.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProcessingException(ErrorConstants.PROCESSING_INTERRUPTED, e);
        }

        try {
            return employee.applyRaise(rate);
        } finally {
            permits.release();
        }
    }

    private SalaryChange toChange(BigDecimal rate, SalaryUpdate update) {
        return new SalaryChange(
                employee.getId(),
                employee.getName(),
                employee.getRole(),
                calculator.yearsWorked(employee),
                employee.getCompletion(),
                update.before(),
                update.after(),
                rate,
                Thread.currentThread().getName());
    }
}
