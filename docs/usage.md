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

- The table first shows the current employee data. The badge in the corner says whether that is the **original data** or the **updated data**.
- **Show before & after** calculates every raise and shows the old salary, new salary, raise percentage, increase and the worker thread that processed each employee. Employees without a raise are greyed out. Nothing is saved.
- **Apply & save CSV** does the same calculation and saves the result to `output/<input-name>-updated.csv`, which becomes the current data. Applying again stacks another round of raises on top.
- The four percentage inputs change the role and per-year raises for both buttons. Values must be between 0 and 100.
- **Reset to original data** deletes the updated file, restores the default percentages and reloads the original data, so you can simulate again from the start.
- The original input file is never modified.

## REST API

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/employees` | The current employee data |
| `GET` | `/api/status` | Which file is current: `{"activeFile": "...", "original": true}` |
| `GET` | `/api/salaries/preview` | Calculates raises on the current data and returns a report without saving |
| `POST` | `/api/salaries/apply` | Calculates raises on the current data, saves the updated CSV and returns the report with its path |
| `POST` | `/api/salaries/reset` | Deletes the updated CSV so the original data is current again; returns the status |

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

