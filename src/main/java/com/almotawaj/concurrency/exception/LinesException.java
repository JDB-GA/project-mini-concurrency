package com.almotawaj.concurrency.exception;

public class LinesException extends RuntimeException {
    public LinesException(String message) {
        super(message);
    }

    public LinesException(String message, Throwable cause) {
        super(message, cause);
    }
}
