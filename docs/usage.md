# Usage

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

