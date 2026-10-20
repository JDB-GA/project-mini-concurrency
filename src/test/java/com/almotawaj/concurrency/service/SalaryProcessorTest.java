package com.almotawaj.concurrency.service;

import com.almotawaj.concurrency.model.Employee;
import com.almotawaj.concurrency.model.ProcessingReport;
import com.almotawaj.concurrency.model.RaiseRules;
import com.almotawaj.concurrency.model.Role;
import com.almotawaj.concurrency.model.SalaryChange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SalaryProcessorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);
    private static final int POOL_SIZE = 8;
    private static final int MAX_CONCURRENT = 2;
    private static final BigDecimal SALARY = new BigDecimal("1000.00");

    private final ExecutorService executor = Executors.newFixedThreadPool(POOL_SIZE);
    private final RaiseCalculator calculator = new RaiseCalculator(
            Clock.fixed(TODAY.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()));
    private final SalaryProcessor processor = new SalaryProcessor(executor, calculator, MAX_CONCURRENT);

    @AfterEach
    void shutdown() {
        executor.shutdownNow();
    }

    @Test
    void processesEveryEmployeeExactlyOnce() {
        List<Employee> employees = IntStream.rangeClosed(1, 1000)
                .mapToObj(i -> new Employee(i, "E" + i, SALARY, TODAY.minusYears(i % 5), Role.values()[i % 3], (i % 10) / 10.0))
                .toList();

        ProcessingReport report = processor.process(employees, RaiseRules.defaults());

        assertEquals(1000, report.processed());
        assertEquals(report.processed(), report.raised() + report.skipped());
        assertEquals(employees.stream().filter(e -> e.getCompletion() >= 0.6).count(), report.raised());
        assertEquals(IntStream.rangeClosed(1, 1000).boxed().toList(),
                report.changes().stream().map(SalaryChange::id).toList());
        assertEquals(report.changes().stream().map(SalaryChange::increase).reduce(BigDecimal.ZERO, BigDecimal::add),
                report.totalIncrease());
    }

    @Test
    void appliesExpectedRaise() {
        Employee alice = new Employee(1, "Alice", new BigDecimal("52000.00"), LocalDate.of(2019, 5, 12), Role.EMPLOYEE, 0.8);

        SalaryChange change = processor.process(List.of(alice), RaiseRules.defaults()).changes().get(0);

        assertEquals(new BigDecimal("0.1500"), change.rate());
        assertEquals(new BigDecimal("52000.00"), change.oldSalary());
        assertEquals(new BigDecimal("59800.00"), change.newSalary());
        assertEquals(new BigDecimal("59800.00"), alice.getSalary());
    }

    @Test
    void usesMultipleWorkerThreads() {
        List<Employee> employees = IntStream.rangeClosed(1, 200)
                .<Employee>mapToObj(i -> new SlowEmployee(i, TODAY.minusYears(1), new AtomicInteger(), new AtomicInteger()))
                .toList();

        ProcessingReport report = processor.process(employees, RaiseRules.defaults());

        assertTrue(report.changes().stream().map(SalaryChange::thread).distinct().count() > 1);
    }

    @Test
    void semaphoreLimitsConcurrentSalaryUpdates() {
        AtomicInteger active = new AtomicInteger();
        AtomicInteger peak = new AtomicInteger();
        List<Employee> employees = IntStream.rangeClosed(1, 40)
                .<Employee>mapToObj(i -> new SlowEmployee(i, TODAY.minusYears(1), active, peak))
                .toList();

        processor.process(employees, RaiseRules.defaults());

        assertTrue(peak.get() <= MAX_CONCURRENT, "peak was " + peak.get());
        assertTrue(peak.get() >= 1);
    }

    @Test
    void lockPreventsLostUpdatesOnSameEmployee() throws InterruptedException {
        Employee shared = new Employee(1, "Shared", SALARY, TODAY, Role.EMPLOYEE, 0.7);
        int raises = 500;
        BigDecimal rate = new BigDecimal("0.001");
        BigDecimal expected = SALARY;
        for (int i = 0; i < raises; i++) {
            expected = expected.multiply(BigDecimal.ONE.add(rate)).setScale(2, RoundingMode.HALF_UP);
        }

        for (int i = 0; i < raises; i++) {
            executor.submit(() -> shared.applyRaise(rate));
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        assertEquals(expected, shared.getSalary());
    }
}
