package com.almotawaj.concurrency.exception;

import com.almotawaj.concurrency.constant.ErrorConstants;
import com.almotawaj.concurrency.dto.ErrorResponse;
import com.almotawaj.concurrency.tools.AppLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidRaiseRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidRaiseRequestException e) {
        AppLogger.warn(e.getMessage());
        return respond(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBinding(BindException e) {
        AppLogger.warn(ErrorConstants.INVALID_RATE_FORMAT);
        return respond(HttpStatus.BAD_REQUEST, ErrorConstants.INVALID_RATE_FORMAT);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException e) {
        return respond(HttpStatus.NOT_FOUND, ErrorConstants.RESOURCE_NOT_FOUND);
    }

    @ExceptionHandler(InvalidEmployeeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEmployee(InvalidEmployeeException e) {
        AppLogger.warn(e.getMessage());
        return respond(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
    }

    @ExceptionHandler({LinesException.class, ProcessingException.class})
    public ResponseEntity<ErrorResponse> handleProcessing(RuntimeException e) {
        AppLogger.error(e.getMessage(), e);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception e) {
        AppLogger.error(ErrorConstants.UNEXPECTED_ERROR, e);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, ErrorConstants.GENERIC_RESPONSE_ERROR);
    }

    private ResponseEntity<ErrorResponse> respond(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(message));
    }
}
