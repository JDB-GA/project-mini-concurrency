package com.almotawaj.concurrency.service;

import com.almotawaj.concurrency.model.Employee;
import com.almotawaj.concurrency.model.ProcessingReport;
import com.almotawaj.concurrency.model.RaiseRules;
import com.almotawaj.concurrency.model.Role;
import com.almotawaj.concurrency.model.SalaryChange;
import com.almotawaj.concurrency.model.SalaryUpdate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

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
                .mapToObj(i -> new Employee(i, "E" + i, 1000.0, TODAY.minusYears(i % 5), Role.values()[i % 3], (i % 10) / 10.0))
                .toList();

        ProcessingReport report = processor.process(employees, RaiseRules.defaults());

        assertEquals(1000, report.processed());
        assertEquals(report.processed(), report.raised() + report.skipped());
        assertEquals(employees.stream().filter(e -> e.getCompletion() >= 0.6).count(), report.raised());
        assertEquals(IntStream.rangeClosed(1, 1000).boxed().toList(),
                report.changes().stream().map(SalaryChange::id).toList());
        assertEquals(report.changes().stream().mapToDouble(SalaryChange::increase).sum(), report.totalIncrease(), 0.01);
    }

    @Test
    void appliesExpectedRaise() {
        Employee alice = new Employee(1, "Alice", 52000.0, LocalDate.of(2019, 5, 12), Role.EMPLOYEE, 0.8);

        SalaryChange change = processor.process(List.of(alice), RaiseRules.defaults()).changes().get(0);

        assertEquals(0.01 + 7 * 0.02, change.rate(), 1e-9);
        assertEquals(52000.0, change.oldSalary());
        assertEquals(59800.0, change.newSalary());
        assertEquals(59800.0, alice.getSalary());
    }

    @Test
    void usesMultipleWorkerThreads() {
        List<Employee> employees = IntStream.rangeClosed(1, 200)
                .<Employee>mapToObj(i -> new SlowEmployee(i, new AtomicInteger(), new AtomicInteger()))
                .toList();

        ProcessingReport report = processor.process(employees, RaiseRules.defaults());

        assertTrue(report.changes().stream().map(SalaryChange::thread).distinct().count() > 1);
    }

    @Test
    void semaphoreLimitsConcurrentSalaryUpdates() {
        AtomicInteger active = new AtomicInteger();
        AtomicInteger peak = new AtomicInteger();
        List<Employee> employees = IntStream.rangeClosed(1, 40)
                .<Employee>mapToObj(i -> new SlowEmployee(i, active, peak))
                .toList();

        processor.process(employees, RaiseRules.defaults());

        assertTrue(peak.get() <= MAX_CONCURRENT, "peak was " + peak.get());
        assertTrue(peak.get() >= 1);
    }

    @Test
    void lockPreventsLostUpdatesOnSameEmployee() throws InterruptedException {
        Employee shared = new Employee(1, "Shared", 1000.0, TODAY, Role.EMPLOYEE, 0.7);
        int raises = 500;
        double expected = 1000.0;
        for (int i = 0; i < raises; i++) {
            expected = Math.round(expected * 1.001 * 100.0) / 100.0;
        }

        for (int i = 0; i < raises; i++) {
            executor.submit(() -> shared.applyRaise(0.001));
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        assertEquals(expected, shared.getSalary());
    }

    private static class SlowEmployee extends Employee {

        private final AtomicInteger active;
        private final AtomicInteger peak;

        SlowEmployee(int id, AtomicInteger active, AtomicInteger peak) {
            super(id, "Slow" + id, 1000.0, TODAY.minusYears(1), Role.EMPLOYEE, 0.7);
            this.active = active;
            this.peak = peak;
        }

        @Override
        public SalaryUpdate applyRaise(double rate) {
            peak.accumulateAndGet(active.incrementAndGet(), Math::max);
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            active.decrementAndGet();
            return super.applyRaise(rate);
        }
    }
}
