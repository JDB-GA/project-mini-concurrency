# Concurrency design

`SalaryProcessor.process` submits one `SalaryTask` per employee to the thread pool. Each technique from the brief has a specific job:

| Technique | Where | Why |
|---|---|---|
| Thread pool (`ExecutorService`) | `ExecutorConfig`, `SalaryProcessor` | A fixed pool of reusable worker threads. Each employee becomes one task, so the work is spread across the pool instead of creating a thread per employee. |
| `Future` | `SalaryProcessor` | The caller waits for every task. If one fails or the caller is interrupted, the remaining tasks are cancelled and the error is reported. |
| `Semaphore` | `SalaryTask` | Limits how many salary updates run at the same moment (`processing.max-concurrent-updates`), the way access to a limited resource such as a database connection would be throttled. Permits are always released in a `finally` block. |
| `ReentrantLock` | `Employee` | Protects the only mutable field, `salary`. Reading the old value and writing the new one happen in one locked section, so two raises on the same employee can never lose an update. |
| `AtomicInteger` | `ProcessingStats` | Counts raised and skipped employees from many threads without locking. |
| `AtomicReference<BigDecimal>` | `ProcessingStats` | Sums the total increase with `accumulateAndGet`, a lock-free compare-and-set loop. |
| `synchronized` block | `ProcessingStats` | Guards the shared `ArrayList` of results, which is not thread-safe by itself. |
| `ReentrantReadWriteLock` | `EmployeeService` | Guards the current data file. Any number of requests can read it at once, but apply and reset take the write lock, so a reader never sees a half-written file and two applies can never overwrite each other. |

## Supporting decisions

- `RaiseCalculator` has no shared mutable state, so every thread can call it without synchronization.
- All `Employee` fields except `salary` are `final`, so they are safely visible to every thread without locks.
- `BigDecimal` is immutable, so values passed between threads cannot change underneath them.
- Results arrive in whatever order the threads finish, so the report sorts them by id.
- `InterruptedException` is never swallowed: the interrupt flag is restored and the error is reported.
- The pool is a Spring bean created once and shut down with the application, rather than a new pool per request.
