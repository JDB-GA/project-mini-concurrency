package com.almotawaj.concurrency.service;

import com.almotawaj.concurrency.model.ProcessingReport;
import com.almotawaj.concurrency.model.SalaryChange;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ProcessingStats {

    private final AtomicInteger raised = new AtomicInteger();
    private final AtomicInteger skipped = new AtomicInteger();
    private final AtomicReference<BigDecimal> totalIncrease = new AtomicReference<>(BigDecimal.ZERO);
    private final List<SalaryChange> changes;

    public ProcessingStats(int expectedSize) {
        this.changes = new ArrayList<>(expectedSize);
    }

    public void record(SalaryChange change) {
        if (change.rate().signum() > 0) {
            raised.incrementAndGet();
            totalIncrease.accumulateAndGet(change.increase(), BigDecimal::add);
        } else {
            skipped.incrementAndGet();
        }

        synchronized (changes) {
            changes.add(change);
        }
    }

    public ProcessingReport toReport(long durationMillis) {
        List<SalaryChange> sorted;

        synchronized (changes) {
            sorted = changes.stream().sorted(Comparator.comparingInt(SalaryChange::id)).toList();
        }

        return new ProcessingReport(sorted, sorted.size(), raised.get(), skipped.get(), totalIncrease.get(), durationMillis);
    }
}
