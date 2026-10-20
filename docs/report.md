# Project report

## Implementation

The application is split into layers that each do one thing:

1. **Reading**: `CsvLines` reads the file and skips the header; `EmployeeCsvMapper` validates each row and builds `Employee` objects, reporting the exact line of any bad row.
2. **Calculating**: `RaiseCalculator` holds the business rules with no threading concerns, so the rules can be tested on their own with a fixed clock.
3. **Processing**: `SalaryProcessor` hands each employee to a fixed thread pool as a `SalaryTask`. A semaphore limits simultaneous updates, a per-employee lock protects each salary, and `ProcessingStats` collects the results with atomics and a `synchronized` block.
4. **Delivering**: `EmployeeService` either previews the result or saves it as the current data, which can be reset to the untouched original. `SalaryController` exposes this over REST, and the web page uses that API.

Raise percentages are passed in as `RaiseRules` instead of being hard-coded, which lets the web page change them per request.

## Testing

`./mvnw test` runs 35 tests:

| Test class | Covers |
|---|---|
| `EmployeeCsvMapperTest` | Valid rows, missing columns, unknown roles, bad dates, completion out of range, a write-then-read round trip, and the sample file |
| `RaiseCalculatorTest` | Every rule and boundary (exactly 60%, exactly 80%, partial years, bonus, custom rates, future join dates) against a fixed date |
| `SalaryProcessorTest` | 1,000 employees processed exactly once with correct totals, work spread over several threads, the semaphore never exceeding its permits, and 500 concurrent raises on one employee with no lost update |
| `SalaryControllerTest` | All endpoints, the output file, custom percentages and every error status |
| `DataResetControllerTest` | Applied data becomes current, a second apply stacks on the first, preview saves nothing, and reset restores the original without touching the input file |
| `EmployeeServiceTest` | Five applies fired at once all stack, proving the write lock serializes them |
| `ConcurrencyApplicationTests` | The Spring context starts |

## Outcomes

With the default rules on the 30-employee sample file (run on 2026-10-20):

| | |
|---|---|
| Employees processed | 30 |
| Received a raise | 21 |
| No raise (completion below 60%) | 9 |
| Total increase | $222,970.00 |
| Worker threads used | 4 (`salary-worker-1` to `salary-worker-4`) |

Example: Eve is an Employee who joined on 2019-02-14 with 90% completion. She gets 1% × 1.5 = 1.5% for her role and 7 × 2% = 14% for tenure, 15.5% in total, taking her salary from $54,000.00 to $62,370.00.

The totals depend on the current date, because years worked are counted up to today.

## Lessons learned

- Keeping the business rules free of threading code made both the rules and the concurrency easier to test.
- Reading and writing the salary must happen under the same lock. Reading it first and writing it later leaves a gap where another thread can change it.
- Concurrency tests need to measure something, such as the peak number of simultaneous updates or the final salary after many parallel raises, rather than only checking that nothing crashed.
- `double` produces values like `0.15000000000000002`, so money and rates use `BigDecimal` with explicit rounding.

## Possible improvements

- Allow uploading a CSV file from the web page instead of using the configured file.
- Make the 60% and 80% thresholds and the 1.5 multiplier configurable from the page.
- Stream very large files instead of loading every row into memory.
