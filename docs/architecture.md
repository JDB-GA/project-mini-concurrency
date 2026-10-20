# Architecture

## Project structure

```
src/main/java/com/almotawaj/concurrency
├── config        ExecutorConfig, TimeConfig
├── constant      ErrorConstants, FileConstants, ProcessingConstants, RaiseConstants
├── controller    SalaryController
├── dto           RaiseRequest, EmployeeResponse, ErrorResponse
├── exception     GlobalExceptionHandler and the custom exceptions
├── model         Employee, Role, RaiseRules, SalaryUpdate, SalaryChange, ProcessingReport, ApplyResult
├── service       EmployeeService, RaiseCalculator, SalaryProcessor, SalaryTask, ProcessingStats
└── tools         CsvLines, EmployeeCsvMapper, AppLogger
src/main/resources
├── 19-10-2026.csv   sample data (30 employees)
└── static           index.html, css/, js/
```

Money and rates use `BigDecimal`. Salaries are rounded to 2 decimal places and rates to 4, both with `HALF_UP`.

## model

- **`Role`**: enum of `DIRECTOR`, `MANAGER`, `EMPLOYEE`, each holding its default raise rate. `fromCsv` and `toCsv` convert to and from the CSV spelling.
- **`Employee`**: one CSV row. Every field is `final` except `salary`, which is guarded by a `ReentrantLock`.
  - `getSalary()` reads the salary under the lock.
  - `applyRaise(rate)` reads the old salary, applies the raise and returns both as a `SalaryUpdate`, all in one locked section.
- **`RaiseRules`**: the role rates and yearly rate for one run. `defaults()` returns the rates from the brief.
- **`SalaryUpdate`**: the salary before and after one raise.
- **`SalaryChange`**: one employee's result, including the worker thread name. `increase()` returns the difference.
- **`ProcessingReport`**: every `SalaryChange` sorted by id, plus counts, total increase and duration.
- **`ApplyResult`**: a `ProcessingReport` plus the path of the written CSV.

## tools

- **`CsvLines`**: generic CSV file access.
  - `getLines(path)` returns the rows after the header, split into columns.
  - `writeLines(path, header, rows)` writes a header and rows, creating folders as needed.
  - `updateCell(path, row, col, value)` changes one cell in place; row 0 is the first data row.
  - `listFiles(dir)` lists the `.csv` files in a folder.
- **`EmployeeCsvMapper`**: converts between CSV rows and `Employee` objects.
  - `toEmployees(rows)` maps every row and skips blank lines.
  - `toEmployee(row, lineNumber)` validates one row and throws `InvalidEmployeeException` with the line number.
  - `toRow(employee)` maps an employee back to a CSV row; `header()` returns the header.
- **`AppLogger`**: thin wrapper around SLF4J.

## service

- **`RaiseCalculator`**: business rules only, no threading.
  - `rateFor(employee, rules)` applies the four raise rules.
  - `yearsWorked(employee)` counts full years up to today, using an injected `Clock` so tests can fix the date.
- **`SalaryProcessor`**: submits one `SalaryTask` per employee to the thread pool, waits for all of them and returns the report.
- **`SalaryTask`**: the work for one employee: calculate the rate, update the salary while holding a semaphore permit, record the result.
- **`ProcessingStats`**: thread-safe collector for results, counters and the total increase.
- **`EmployeeService`**: ties file access and processing together.
  - `loadEmployees()` reads the configured input CSV.
  - `preview(rules)` processes a fresh copy without writing anything.
  - `apply(rules)` processes the employees and writes `output/<input-name>-updated.csv`.

## web

- **`SalaryController`**: the three REST endpoints.
- **`RaiseRequest`**: binds and validates the optional percentage parameters and converts them into `RaiseRules`.
- **`EmployeeResponse`**: the JSON shape of an employee, so the API does not expose the internal model.
- **`GlobalExceptionHandler`**: turns every exception into a JSON `ErrorResponse` with the right status.

## frontend

- `js/api.js`: fetch calls and query building.
- `js/columns.js`: table column definitions and number formatting.
- `js/table.js`: renders a table from columns and rows.
- `js/app.js`: wires buttons, messages and the summary cards.
- `css/base.css`, `css/form.css`, `css/table.css`: theme and layout, with a dark mode.

## config

- **`ExecutorConfig`**: the shared fixed-size thread pool, threads named `salary-worker-N`, shut down when the application stops.
- **`TimeConfig`**: provides the system `Clock`.
