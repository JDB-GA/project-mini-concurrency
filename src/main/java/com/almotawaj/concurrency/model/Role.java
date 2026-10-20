package com.almotawaj.concurrency.model;

import java.math.BigDecimal;

public enum Role {
    DIRECTOR(new BigDecimal("0.05")),
    MANAGER(new BigDecimal("0.02")),
    EMPLOYEE(new BigDecimal("0.01"));

    private final BigDecimal raiseRate;

    Role(BigDecimal raiseRate) {
        this.raiseRate = raiseRate;
    }

    public BigDecimal getRaiseRate() {
        return raiseRate;
    }

    public static Role fromCsv(String value) {
        return Role.valueOf(value.trim().toUpperCase());
    }

    public String toCsv() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
