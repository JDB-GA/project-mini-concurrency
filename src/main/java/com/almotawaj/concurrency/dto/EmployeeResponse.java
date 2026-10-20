package com.almotawaj.concurrency.dto;

import com.almotawaj.concurrency.model.Employee;
import com.almotawaj.concurrency.model.Role;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeResponse(
        int id,
        String name,
        BigDecimal salary,
        LocalDate joinedDate,
        Role role,
        double completion) {

    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                employee.getSalary(),
                employee.getJoinedDate(),
                employee.getRole(),
                employee.getCompletion());
    }
}
