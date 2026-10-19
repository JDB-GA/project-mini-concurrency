package com.almotawaj.concurrency.model;

public enum Role {
    DIRECTOR(0.05),
    MANAGER(0.02),
    EMPLOYEE(0.01);

    private final double raiseRate;

    Role(double raiseRate) {
        this.raiseRate = raiseRate;
    }

    public double getRaiseRate() {
        return raiseRate;
    }

    public static Role fromCsv(String value) {
        return Role.valueOf(value.trim().toUpperCase());
    }
}
