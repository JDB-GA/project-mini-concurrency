package com.almotawaj.concurrency.controller;

import com.almotawaj.concurrency.dto.RaiseRequest;
import com.almotawaj.concurrency.model.ApplyResult;
import com.almotawaj.concurrency.model.Employee;
import com.almotawaj.concurrency.model.ProcessingReport;
import com.almotawaj.concurrency.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SalaryController {

    private final EmployeeService employeeService;

    public SalaryController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/employees")
    public ResponseEntity<List<Employee>> getEmployees() {
        return ResponseEntity.ok(employeeService.loadEmployees());
    }

    @GetMapping("/salaries/preview")
    public ResponseEntity<ProcessingReport> preview(@ModelAttribute RaiseRequest request) {
        return ResponseEntity.ok(employeeService.preview(request.toRules()));
    }

    @PostMapping("/salaries/apply")
    public ResponseEntity<ApplyResult> apply(@ModelAttribute RaiseRequest request) {
        return ResponseEntity.ok(employeeService.apply(request.toRules()));
    }
}
