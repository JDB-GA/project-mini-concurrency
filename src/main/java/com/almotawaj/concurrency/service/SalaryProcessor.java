package com.almotawaj.concurrency.service;

import com.almotawaj.concurrency.constant.ErrorConstants;
import com.almotawaj.concurrency.constant.ProcessingConstants;
import com.almotawaj.concurrency.exception.ProcessingException;
import com.almotawaj.concurrency.model.Employee;
import com.almotawaj.concurrency.model.ProcessingReport;
import com.almotawaj.concurrency.model.RaiseRules;
import com.almotawaj.concurrency.model.SalaryChange;
import com.almotawaj.concurrency.model.SalaryUpdate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class SalaryProcessor {

    private final ExecutorService executor;
    private final RaiseCalculator calculator;
    private final int maxConcurrentUpdates;

    public SalaryProcessor(ExecutorService executor,
                           RaiseCalculator calculator,
                           @Value(ProcessingConstants.MAX_CONCURRENT_UPDATES_PROPERTY) int maxConcurrentUpdates) {
        this.executor = executor;
        this.calculator = calculator;
        this.maxConcurrentUpdates = maxConcurrentUpdates;
    }

    public ProcessingReport process(List<Employee> employees, RaiseRules rules) {
        long start = System.nanoTime();
        Semaphore permits = new Semaphore(maxConcurrentUpdates);
        AtomicInteger raised = new AtomicInteger();
        AtomicInteger skipped = new AtomicInteger();
        AtomicReference<BigDecimal> totalIncrease = new AtomicReference<>(BigDecimal.ZERO);
        List<SalaryChange> changes = new ArrayList<>(employees.size());

        List<Future<?>> tasks = employees.stream()
                .<Future<?>>map(employee -> executor.submit(() -> {
                    SalaryChange change = processEmployee(employee, rules, permits);

                    if (change.rate().signum() > 0) {
                        raised.incrementAndGet();
                        totalIncrease.accumulateAndGet(change.increase(), BigDecimal::add);
                    } else {
                        skipped.incrementAndGet();
                    }

                    synchronized (changes) {
                        changes.add(change);
                    }
                }))
                .toList();

        awaitAll(tasks);

        List<SalaryChange> sorted;
        synchronized (changes) {
            sorted = changes.stream().sorted(Comparator.comparingInt(SalaryChange::id)).toList();
        }

        return new ProcessingReport(
                sorted,
                sorted.size(),
                raised.get(),
                skipped.get(),
                totalIncrease.get(),
                (System.nanoTime() - start) / 1_000_000);
    }

    private SalaryChange processEmployee(Employee employee, RaiseRules rules, Semaphore permits) {
        BigDecimal rate = calculator.rateFor(employee, rules);

        try {
            permits.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProcessingException(ErrorConstants.PROCESSING_INTERRUPTED, e);
        }

        try {
            SalaryUpdate update = employee.applyRaise(rate);
            return new SalaryChange(
                    employee.getId(),
                    employee.getName(),
                    employee.getRole(),
                    calculator.yearsWorked(employee),
                    employee.getCompletion(),
                    update.before(),
                    update.after(),
                    rate,
                    Thread.currentThread().getName());
        } finally {
            permits.release();
        }
    }

    private void awaitAll(List<Future<?>> tasks) {
        for (Future<?> task : tasks) {
            try {
                task.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                tasks.forEach(pending -> pending.cancel(true));
                throw new ProcessingException(ErrorConstants.PROCESSING_INTERRUPTED, e);
            } catch (ExecutionException e) {
                tasks.forEach(pending -> pending.cancel(true));
                throw new ProcessingException(ErrorConstants.PROCESSING_FAILED, e.getCause());
            }
        }
    }
}
