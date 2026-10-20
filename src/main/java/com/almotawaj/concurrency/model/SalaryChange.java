package com.almotawaj.concurrency.model;

import java.math.BigDecimal;

public record SalaryChange(
        int id,
        String name,
        Role role,
        int yearsWorked,
        double completion,
        BigDecimal oldSalary,
        BigDecimal newSalary,
        BigDecimal rate,
        String thread) {

    public BigDecimal increase() {
        return newSalary.subtract(oldSalary);
    }
}
