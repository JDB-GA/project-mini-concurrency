package com.almotawaj.concurrency.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AppLogger {

    private static final Logger log =
            LoggerFactory.getLogger(AppLogger.class);

    private AppLogger() {
    }

    public static void error(String message, Throwable e) {
        log.error(message, e);
    }

    public static void warn(String message) {
        log.warn(message);
    }
}
