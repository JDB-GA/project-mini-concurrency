# Setup

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

