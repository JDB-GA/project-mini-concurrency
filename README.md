# Concurrent CSV Data Processor

A Spring Boot application that reads employee records from a CSV file, calculates each employee's salary increase, and applies the increases concurrently using a thread pool. It includes a web page that shows every employee's salary before and after the raise, with adjustable raise percentages.

## Requirements

- Java 17 or newer
- No Maven installation needed: the project ships with the Maven wrapper (`./mvnw`)

## Setup and running

Clone the repository and start the application from the project root:

```bash
./mvnw spring-boot:run
```

Then open http://localhost:8080 in a browser.

Run the test suite:

```bash
./mvnw test
```

Build a runnable jar:

```bash
./mvnw package
java -jar target/concurrency-0.0.1-SNAPSHOT.jar
```

The jar reads the CSV path relative to the folder it is started from, so start it from the project root or override the paths (see Configuration).

## Configuration

All settings live in `src/main/resources/application.properties` and can be overridden on the command line, for example `--processing.pool-size=8`.

| Property | Default | Meaning |
|---|---|---|
| `employees.input-file` | `src/main/resources/19-10-2026.csv` | CSV file the employees are read from |
| `employees.output-dir` | `output` | Folder the updated CSV is written to |
| `processing.pool-size` | `4` | Number of worker threads in the pool |
| `processing.max-concurrent-updates` | `2` | Semaphore permits: how many salary updates may run at the same moment |

## CSV format

The first line is a header and is skipped when reading.

```
id,name,salary,joinedDate,role,completion
1,Alice,52000.0,2019-05-12,Employee,0.8
```

| Column | Type | Notes |
|---|---|---|
| `id` | integer | |
| `name` | text | |
| `salary` | decimal | current salary |
| `joinedDate` | date | ISO format `yyyy-MM-dd` |
| `role` | `Director`, `Manager` or `Employee` | case-insensitive |
| `completion` | decimal between 0 and 1 | fraction of projects completed, `0.8` = 80% |

Rows with a missing column, an unknown role, a malformed date or number, or a completion outside 0 to 1 are rejected with the line number in the error message.

## Raise rules

1. **Completion below 60%**: no raise at all; the other rules are skipped.
2. **Role**: Director +5%, Manager +2%, Employee +1%.
3. **Tenure**: +2% for each full year worked, only once at least one full year has been completed. Partial years are not counted.
4. **Bonus**: completion above 80% multiplies the role percentage by 1.5 (for example Employee 1% becomes 1.5%).

The final rate is `role rate (with bonus) + tenure rate`, and the new salary is `salary × (1 + rate)`, rounded to cents.

Boundary decisions:

- Exactly 60% completion **does** receive a raise ("less than 60%" is excluded).
- Exactly 80% completion does **not** receive the bonus ("more than 80%" is required).
- The 1.5 multiplier applies to the role percentage only, not to the tenure percentage, following the example in the brief.
- Years worked are counted against today's date.

## Using the web page

- The table first shows the employees exactly as they are in the CSV.
- **Show before & after** calculates every raise and shows the old salary, new salary, raise percentage, increase and the worker thread that processed each employee. Employees without a raise are greyed out. The CSV file is not changed.
- **Apply & save CSV** does the same calculation and writes the updated salaries to `output/<input-name>-updated.csv`. The original file is never modified.
- The four percentage inputs change the role and per-year raises for both buttons. Values must be between 0 and 100.
- **Reset** restores the default percentages and reloads the original data.

## REST API

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/employees` | Employees as read from the CSV |
| `GET` | `/api/salaries/preview` | Calculates raises and returns a report without writing anything |
| `POST` | `/api/salaries/apply` | Calculates raises, writes the output CSV and returns the report with the output path |

Both salary endpoints accept optional query parameters in percent: `director`, `manager`, `employee`, `yearly`. Missing parameters fall back to the default rules.

```bash
curl "http://localhost:8080/api/salaries/preview?employee=3&yearly=1"
```

Errors are returned as JSON, `{"message": "..."}`:

| Status | When |
|---|---|
| 400 | A percentage is not a number or is outside 0 to 100 |
| 404 | Unknown path |
| 422 | The CSV contains an invalid row |
| 500 | The file cannot be read or written, or processing fails |
