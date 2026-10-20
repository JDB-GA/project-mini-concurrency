package com.almotawaj.concurrency.service;

import com.almotawaj.concurrency.constant.ErrorConstants;
import com.almotawaj.concurrency.constant.ProcessingConstants;
import com.almotawaj.concurrency.exception.ProcessingException;
import com.almotawaj.concurrency.model.Employee;
import com.almotawaj.concurrency.model.ProcessingReport;
import com.almotawaj.concurrency.model.RaiseRules;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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
        ProcessingStats stats = new ProcessingStats(employees.size());

        List<Future<?>> tasks = employees.stream()
                .<Future<?>>map(employee -> executor.submit(new SalaryTask(employee, rules, calculator, permits, stats)))
                .toList();

        awaitAll(tasks);

        return stats.toReport(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
    }

    private void awaitAll(List<Future<?>> tasks) {
        for (Future<?> task : tasks) {
            try {
                task.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                cancelAll(tasks);
                throw new ProcessingException(ErrorConstants.PROCESSING_INTERRUPTED, e);
            } catch (ExecutionException e) {
                cancelAll(tasks);
                throw new ProcessingException(ErrorConstants.PROCESSING_FAILED, e.getCause());
            }
        }
    }

    private void cancelAll(List<Future<?>> tasks) {
        tasks.forEach(task -> task.cancel(true));
    }
}
