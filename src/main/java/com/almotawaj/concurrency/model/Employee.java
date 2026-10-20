package com.almotawaj.concurrency.model;

import com.almotawaj.concurrency.constant.RaiseConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.concurrent.locks.ReentrantLock;

public class Employee {

    private final int id;
    private final String name;
    private final LocalDate joinedDate;
    private final Role role;
    private final double completion;
    private final ReentrantLock salaryLock = new ReentrantLock();
    private BigDecimal salary;

    public Employee(int id, String name, BigDecimal salary, LocalDate joinedDate, Role role, double completion) {
        this.id = id;
        this.name = name;
        this.salary = salary;
        this.joinedDate = joinedDate;
        this.role = role;
        this.completion = completion;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getJoinedDate() {
        return joinedDate;
    }

    public Role getRole() {
        return role;
    }

    public double getCompletion() {
        return completion;
    }

    public BigDecimal getSalary() {
        salaryLock.lock();
        try {
            return salary;
        } finally {
            salaryLock.unlock();
        }
    }

    public SalaryUpdate applyRaise(BigDecimal rate) {
        salaryLock.lock();
        try {
            BigDecimal before = salary;
            salary = salary.multiply(BigDecimal.ONE.add(rate)).setScale(RaiseConstants.MONEY_SCALE, RoundingMode.HALF_UP);
            return new SalaryUpdate(before, salary);
        } finally {
            salaryLock.unlock();
        }
    }
}
