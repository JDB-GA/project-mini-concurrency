package com.almotawaj.concurrency.service;

import com.almotawaj.concurrency.constant.FileConstants;
import com.almotawaj.concurrency.model.ApplyResult;
import com.almotawaj.concurrency.model.Employee;
import com.almotawaj.concurrency.model.ProcessingReport;
import com.almotawaj.concurrency.model.RaiseRules;
import com.almotawaj.concurrency.tools.CsvLines;
import com.almotawaj.concurrency.tools.EmployeeCsvMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

@Service
public class EmployeeService {

    private final SalaryProcessor processor;
    private final Path inputFile;
    private final Path outputDir;

    public EmployeeService(SalaryProcessor processor,
                           @Value(FileConstants.INPUT_FILE_PROPERTY) String inputFile,
                           @Value(FileConstants.OUTPUT_DIR_PROPERTY) String outputDir) {
        this.processor = processor;
        this.inputFile = Path.of(inputFile);
        this.outputDir = Path.of(outputDir);
    }

    public List<Employee> loadEmployees() {
        return EmployeeCsvMapper.toEmployees(CsvLines.getLines(inputFile));
    }

    public ProcessingReport preview(RaiseRules rules) {
        return processor.process(loadEmployees(), rules);
    }

    public ApplyResult apply(RaiseRules rules) {
        List<Employee> employees = loadEmployees();
        ProcessingReport report = processor.process(employees, rules);
        Path outputFile = outputFile();

        CsvLines.writeLines(outputFile, EmployeeCsvMapper.header(),
                employees.stream().map(EmployeeCsvMapper::toRow).toList());

        return new ApplyResult(report, outputFile.toString());
    }

    private Path outputFile() {
        String fileName = inputFile.getFileName().toString();
        String baseName = fileName.endsWith(FileConstants.CSV_EXTENSION)
                ? fileName.substring(0, fileName.length() - FileConstants.CSV_EXTENSION.length())
                : fileName;

        return outputDir.resolve(baseName + FileConstants.OUTPUT_SUFFIX + FileConstants.CSV_EXTENSION);
    }
}
