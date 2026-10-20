package com.almotawaj.concurrency.model;

import java.math.BigDecimal;

public record SalaryUpdate(BigDecimal before, BigDecimal after) {
}
