package com.almotawaj.concurrency.service;

import com.almotawaj.concurrency.model.RaiseRules;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class EmployeeServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);
    private static final int APPLIES = 5;

    private final ExecutorService workers = Executors.newFixedThreadPool(4);
    private final ExecutorService callers = Executors.newFixedThreadPool(APPLIES);
    private final RaiseCalculator calculator = new RaiseCalculator(
            Clock.fixed(TODAY.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()));

    @TempDir
    Path outputDir;

    @AfterEach
    void shutdown() {
        workers.shutdownNow();
        callers.shutdownNow();
    }

    @Test
    void concurrentAppliesStackWithoutLosingAny() throws Exception {
        EmployeeService service = new EmployeeService(new SalaryProcessor(workers, calculator, 2),
                "src/main/resources/19-10-2026.csv", outputDir.toString());
        List<Callable<Object>> applies = IntStream.range(0, APPLIES)
                .mapToObj(i -> (Callable<Object>) () -> service.apply(RaiseRules.defaults()))
                .toList();

        for (Future<Object> result : callers.invokeAll(applies)) {
            result.get();
        }

        BigDecimal expected = new BigDecimal("52000.0");
        for (int i = 0; i < APPLIES; i++) {
            expected = expected.multiply(new BigDecimal("1.15")).setScale(2, RoundingMode.HALF_UP);
        }
        assertEquals(expected, service.loadEmployees().get(0).getSalary());
        assertFalse(service.status().original());
    }
}
