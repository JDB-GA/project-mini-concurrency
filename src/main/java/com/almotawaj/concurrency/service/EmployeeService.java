package com.almotawaj.concurrency.service;

import com.almotawaj.concurrency.constant.FileConstants;
import com.almotawaj.concurrency.dto.DataStatus;
import com.almotawaj.concurrency.model.ApplyResult;
import com.almotawaj.concurrency.model.Employee;
import com.almotawaj.concurrency.model.ProcessingReport;
import com.almotawaj.concurrency.model.RaiseRules;
import com.almotawaj.concurrency.tools.CsvLines;
import com.almotawaj.concurrency.tools.EmployeeCsvMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

@Service
public class EmployeeService {

    private final SalaryProcessor processor;
    private final Path inputFile;
    private final Path workingFile;
    private final ReentrantReadWriteLock dataLock = new ReentrantReadWriteLock();

    public EmployeeService(SalaryProcessor processor,
                           @Value(FileConstants.INPUT_FILE_PROPERTY) String inputFile,
                           @Value(FileConstants.OUTPUT_DIR_PROPERTY) String outputDir) {
        this.processor = processor;
        this.inputFile = Path.of(inputFile);
        this.workingFile = Path.of(outputDir).resolve(workingFileName(this.inputFile));
    }

    public List<Employee> loadEmployees() {
        return withLock(dataLock.readLock(), this::readActiveFile);
    }

    public DataStatus status() {
        return withLock(dataLock.readLock(), this::currentStatus);
    }

    public ProcessingReport preview(RaiseRules rules) {
        return processor.process(loadEmployees(), rules);
    }

    public ApplyResult apply(RaiseRules rules) {
        return withLock(dataLock.writeLock(), () -> {
            List<Employee> employees = readActiveFile();
            ProcessingReport report = processor.process(employees, rules);
            CsvLines.writeLines(workingFile, EmployeeCsvMapper.header(),
                    employees.stream().map(EmployeeCsvMapper::toRow).toList());
            return new ApplyResult(report, workingFile.toString());
        });
    }

    public DataStatus reset() {
        return withLock(dataLock.writeLock(), () -> {
            CsvLines.deleteIfExists(workingFile);
            return currentStatus();
        });
    }

    private List<Employee> readActiveFile() {
        return EmployeeCsvMapper.toEmployees(CsvLines.getLines(activeFile()));
    }

    private DataStatus currentStatus() {
        Path active = activeFile();
        return new DataStatus(active.toString(), active.equals(inputFile));
    }

    private Path activeFile() {
        return Files.exists(workingFile) ? workingFile : inputFile;
    }

    private static String workingFileName(Path inputFile) {
        String fileName = inputFile.getFileName().toString();
        String baseName = fileName.endsWith(FileConstants.CSV_EXTENSION)
                ? fileName.substring(0, fileName.length() - FileConstants.CSV_EXTENSION.length())
                : fileName;

        return baseName + FileConstants.OUTPUT_SUFFIX + FileConstants.CSV_EXTENSION;
    }

    private static <T> T withLock(Lock lock, Supplier<T> action) {
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }
}
