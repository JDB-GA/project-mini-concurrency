package com.almotawaj.concurrency.tools;

import com.almotawaj.concurrency.constant.ErrorConstants;
import com.almotawaj.concurrency.constant.FileConstants;
import com.almotawaj.concurrency.exception.LinesException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public final class CsvLines {

    private static final int HEADER_LINES = 1;

    private CsvLines() {
    }

    public static List<String[]> getLines(Path linesPath) {
        List<String[]> lines = readAll(linesPath);
        return lines.subList(Math.min(HEADER_LINES, lines.size()), lines.size());
    }

    public static void writeLines(Path linesPath, String[] header, List<String[]> lines) {
        writeAll(linesPath, Stream.concat(Stream.<String[]>of(header), lines.stream()).toList());
    }

    public static void updateCell(Path linesPath, int row, int col, String value) {
        List<String[]> lines = readAll(linesPath);
        int index = row + HEADER_LINES;

        if (row < 0 || index >= lines.size() || col < 0 || col >= lines.get(index).length) {
            throw new LinesException(String.format(ErrorConstants.CELL_NOT_FOUND, row, col));
        }

        lines.get(index)[col] = value;
        writeAll(linesPath, lines);
    }

    public static boolean deleteIfExists(Path linesPath) {
        try {
            return Files.deleteIfExists(linesPath);
        } catch (IOException e) {
            throw new LinesException(ErrorConstants.FILE_DELETE_ERROR, e);
        }
    }

    public static List<String> listFiles(Path dir) {
        if (!Files.exists(dir)) {
            return List.of();
        }

        try (Stream<Path> files = Files.list(dir)) {
            return files
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith(FileConstants.CSV_EXTENSION))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new LinesException(ErrorConstants.FILES_LIST_ERROR, e);
        }
    }

    private static List<String[]> readAll(Path linesPath) {
        try {
            return Files.readAllLines(linesPath).stream()
                    .map(line -> line.split(FileConstants.COLUMN_SEPARATOR, -1))
                    .toList();
        } catch (IOException e) {
            throw new LinesException(ErrorConstants.LINES_READ_ERROR, e);
        }
    }

    private static void writeAll(Path linesPath, List<String[]> lines) {
        try {
            Path parent = linesPath.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(linesPath, lines.stream()
                    .map(columns -> String.join(FileConstants.COLUMN_SEPARATOR, columns))
                    .toList());
        } catch (IOException e) {
            throw new LinesException(ErrorConstants.LINES_WRITE_ERROR, e);
        }
    }
}
