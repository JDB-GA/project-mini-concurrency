# Concurrent CSV Data Processor

A Spring Boot application that reads employee records from a CSV file, calculates each employee's salary increase, and applies the increases concurrently using a thread pool. A web page shows every salary before and after the raise, with adjustable raise percentages.

## Quick start

Requires Java 17 or newer. Maven is not needed; the project includes the Maven wrapper.

```bash
./mvnw spring-boot:run
```

Open http://localhost:8080, then click **Show before & after**.

```bash
./mvnw test
```

## Raise rules

| Rule | Effect |
|---|---|
| Completion below 60% | No raise |
| Role | Director +5%, Manager +2%, Employee +1% |
| Tenure | +2% per full year worked, after the first full year |
| Completion above 80% | Role percentage × 1.5 |

## Documentation

| Page | Contents |
|---|---|
| [Setup](docs/setup.md) | Running, building, configuration and the CSV format |
| [Usage](docs/usage.md) | Raise rules in detail, the web page and the REST API |
| [Architecture](docs/architecture.md) | Project structure and a reference for every class |
| [Concurrency design](docs/concurrency.md) | How the thread pool, semaphore, locks, atomics and `synchronized` are used, and why |
| [Project report](docs/report.md) | Implementation summary, tests, outcomes and lessons learned |

## Tech stack

- Java 17, Spring Boot 4 (Web MVC)
- `java.util.concurrent`: `ExecutorService`, `Semaphore`, `ReentrantLock`, atomics
- Plain HTML, CSS and JavaScript modules for the frontend
- JUnit 5 and MockMvc for tests
