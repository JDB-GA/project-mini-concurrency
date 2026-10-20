package com.almotawaj.concurrency.model;

import java.time.LocalDate;
import java.util.concurrent.locks.ReentrantLock;

public class Employee {

    private final int id;
    private final String name;
    private final LocalDate joinedDate;
    private final Role role;
    private final double completion;
    private final ReentrantLock salaryLock = new ReentrantLock();
    private double salary;

    public Employee(int id, String name, double salary, LocalDate joinedDate, Role role, double completion) {
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

    public double getSalary() {
        salaryLock.lock();
        try {
            return salary;
        } finally {
            salaryLock.unlock();
        }
    }

    public double applyRaise(double rate) {
        salaryLock.lock();
        try {
            salary = Math.round(salary * (1 + rate) * 100.0) / 100.0;
            return salary;
        } finally {
            salaryLock.unlock();
        }
    }
}
