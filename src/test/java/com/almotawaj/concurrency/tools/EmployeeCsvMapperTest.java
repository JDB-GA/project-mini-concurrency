package com.almotawaj.concurrency.tools;

import com.almotawaj.concurrency.exception.InvalidEmployeeException;
import com.almotawaj.concurrency.model.Employee;
import com.almotawaj.concurrency.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmployeeCsvMapperTest {

    @Test
    void mapsValidRowToEmployee() {
        Employee employee = EmployeeCsvMapper.toEmployee(
                new String[]{"1", "Alice", "52000.0", "2019-05-12", "Employee", "0.8"}, 2);

        assertEquals(1, employee.getId());
        assertEquals("Alice", employee.getName());
        assertEquals(new BigDecimal("52000.0"), employee.getSalary());
        assertEquals(LocalDate.of(2019, 5, 12), employee.getJoinedDate());
        assertEquals(Role.EMPLOYEE, employee.getRole());
        assertEquals(0.8, employee.getCompletion());
    }

    @Test
    void rejectsRowWithMissingColumns() {
        InvalidEmployeeException e = assertThrows(InvalidEmployeeException.class,
                () -> EmployeeCsvMapper.toEmployee(new String[]{"1", "Alice"}, 5));

        assertEquals("Invalid employee data at line 5", e.getMessage());
    }

    @Test
    void rejectsUnknownRole() {
        assertThrows(InvalidEmployeeException.class, () -> EmployeeCsvMapper.toEmployee(
                new String[]{"1", "Alice", "52000.0", "2019-05-12", "Intern", "0.8"}, 2));
    }

    @Test
    void rejectsInvalidDate() {
        assertThrows(InvalidEmployeeException.class, () -> EmployeeCsvMapper.toEmployee(
                new String[]{"1", "Alice", "52000.0", "12-05-2019", "Employee", "0.8"}, 2));
    }

    @Test
    void rejectsCompletionOutsideRange() {
        assertThrows(InvalidEmployeeException.class, () -> EmployeeCsvMapper.toEmployee(
                new String[]{"1", "Alice", "52000.0", "2019-05-12", "Employee", "80"}, 2));
    }

    @Test
    void roundTripsThroughCsvFile(@TempDir Path dir) {
        Path file = dir.resolve("employees.csv");
        String[] row = {"7", "Grace", "71000.0", "2021-10-10", "Manager", "0.6"};

        CsvLines.writeLines(file, EmployeeCsvMapper.header(), List.<String[]>of(row));
        List<Employee> employees = EmployeeCsvMapper.toEmployees(CsvLines.getLines(file));

        assertEquals(1, employees.size());
        assertArrayEquals(row, EmployeeCsvMapper.toRow(employees.get(0)));
    }

    @Test
    void readsSampleFile() {
        List<Employee> employees = EmployeeCsvMapper.toEmployees(
                CsvLines.getLines(Path.of("src/main/resources/19-10-2026.csv")));

        assertEquals(30, employees.size());
        assertEquals("Alice", employees.get(0).getName());
    }
}
