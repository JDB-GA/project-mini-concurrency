package com.almotawaj.concurrency.config;

import com.almotawaj.concurrency.constant.ProcessingConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class ExecutorConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService salaryExecutor(@Value(ProcessingConstants.POOL_SIZE_PROPERTY) int poolSize) {
        return Executors.newFixedThreadPool(poolSize, workerThreadFactory());
    }

    private ThreadFactory workerThreadFactory() {
        AtomicInteger counter = new AtomicInteger(1);
        return task -> {
            Thread thread = new Thread(task, ProcessingConstants.WORKER_NAME_PREFIX + counter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
    }
}
