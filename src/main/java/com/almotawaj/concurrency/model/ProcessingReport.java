package com.almotawaj.concurrency.model;

import java.util.List;

public record ProcessingReport(
        List<SalaryChange> changes,
        int processed,
        int raised,
        int skipped,
        double totalIncrease,
        long durationMillis) {
}
