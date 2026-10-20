package com.almotawaj.concurrency.model;

import java.math.BigDecimal;
import java.util.List;

public record ProcessingReport(
        List<SalaryChange> changes,
        int processed,
        int raised,
        int skipped,
        BigDecimal totalIncrease,
        long durationMillis) {
}
