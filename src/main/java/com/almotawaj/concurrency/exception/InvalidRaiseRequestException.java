package com.almotawaj.concurrency.exception;

public class InvalidRaiseRequestException extends RuntimeException {
    public InvalidRaiseRequestException(String message) {
        super(message);
    }
}
