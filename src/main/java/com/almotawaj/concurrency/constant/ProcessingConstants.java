package com.almotawaj.concurrency.constant;

public final class ProcessingConstants {
    public static final String POOL_SIZE_PROPERTY = "${processing.pool-size}";
    public static final String MAX_CONCURRENT_UPDATES_PROPERTY = "${processing.max-concurrent-updates}";
    public static final String WORKER_NAME_PREFIX = "salary-worker-";
    public static final long SHUTDOWN_TIMEOUT_SECONDS = 10;

    private ProcessingConstants() {
    }
}
