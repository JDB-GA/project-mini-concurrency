package com.almotawaj.concurrency.constant;

public final class FileConstants {
    public static final String COLUMN_SEPARATOR = ",";
    public static final String CSV_EXTENSION = ".csv";
    public static final String OUTPUT_SUFFIX = "-updated";
    public static final String INPUT_FILE_PROPERTY = "${employees.input-file}";
    public static final String OUTPUT_DIR_PROPERTY = "${employees.output-dir}";

    private FileConstants() {
    }
}
