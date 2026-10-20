package com.almotawaj.concurrency.model;

public record SalaryChange(
        int id,
        String name,
        Role role,
        int yearsWorked,
        double completion,
        double oldSalary,
        double newSalary,
        double rate,
        String thread) {

    public double increase() {
        return Math.round((newSalary - oldSalary) * 100.0) / 100.0;
    }
}
