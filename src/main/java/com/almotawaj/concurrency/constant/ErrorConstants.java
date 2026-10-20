package com.almotawaj.concurrency.constant;

public final class ErrorConstants {
    public static final String LINES_READ_ERROR = "An error occurred while reading lines";
    public static final String LINES_WRITE_ERROR = "An error occurred while writing lines";
    public static final String FILES_LIST_ERROR = "An error occurred while listing files";
    public static final String CELL_NOT_FOUND = "Cell (%d, %d) does not exist";
    public static final String INVALID_EMPLOYEE_ROW = "Invalid employee data at line %d";
    public static final String PROCESSING_INTERRUPTED = "Salary processing was interrupted";
    public static final String PROCESSING_FAILED = "Salary processing failed";
    public static final String INVALID_RATE = "Raise percentages must be between 0 and 100";
    public static final String INVALID_RATE_FORMAT = "Raise percentages must be numbers";
    public static final String RESOURCE_NOT_FOUND = "Resource not found";
    public static final String UNEXPECTED_ERROR = "Unexpected error";
    public static final String GENERIC_RESPONSE_ERROR = "Something went wrong";

    private ErrorConstants() {
    }
}
