package com.almotawaj.concurrency.tools;

import com.almotawaj.concurrency.constant.ErrorConstants;
import com.almotawaj.concurrency.exception.InvalidEmployeeException;
import com.almotawaj.concurrency.model.Employee;
import com.almotawaj.concurrency.model.Role;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class EmployeeCsvMapper {

    private static final String[] HEADER = {"id", "name", "salary", "joinedDate", "role", "completion"};

    private static final int ID = 0;
    private static final int NAME = 1;
    private static final int SALARY = 2;
    private static final int JOINED_DATE = 3;
    private static final int ROLE = 4;
    private static final int COMPLETION = 5;
    private static final int FIRST_DATA_LINE = 2;

    private EmployeeCsvMapper() {
    }

    public static String[] header() {
        return HEADER.clone();
    }

    public static List<Employee> toEmployees(List<String[]> rows) {
        List<Employee> employees = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            String[] row = rows.get(i);

            if (!isBlank(row)) {
                employees.add(toEmployee(row, i + FIRST_DATA_LINE));
            }
        }

        return employees;
    }

    public static Employee toEmployee(String[] row, int lineNumber) {
        String error = String.format(ErrorConstants.INVALID_EMPLOYEE_ROW, lineNumber);

        if (row.length != HEADER.length) {
            throw new InvalidEmployeeException(error);
        }

        try {
            double completion = Double.parseDouble(row[COMPLETION].trim());

            if (completion < 0 || completion > 1) {
                throw new InvalidEmployeeException(error);
            }

            return new Employee(
                    Integer.parseInt(row[ID].trim()),
                    row[NAME].trim(),
                    new BigDecimal(row[SALARY].trim()),
                    LocalDate.parse(row[JOINED_DATE].trim()),
                    Role.fromCsv(row[ROLE]),
                    completion);
        } catch (IllegalArgumentException | DateTimeException e) {
            throw new InvalidEmployeeException(error, e);
        }
    }

    public static String[] toRow(Employee employee) {
        return new String[]{
                String.valueOf(employee.getId()),
                employee.getName(),
                employee.getSalary().toPlainString(),
                employee.getJoinedDate().toString(),
                employee.getRole().toCsv(),
                String.valueOf(employee.getCompletion())
        };
    }

    private static boolean isBlank(String[] row) {
        return row.length == 1 && row[0].isBlank();
    }
}
