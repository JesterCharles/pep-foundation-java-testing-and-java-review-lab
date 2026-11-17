# Testing and Java Review lab: rescue an older service

You take over `OrderService.java`, an order service written years ago, in five linked tasks in the package `legacy`.
The tests came first. They describe what the service SHOULD do, and the old code fails them:
- a billing bug;
- a price feed that gets called on every request instead of cached;
- a switch that falls through;
- a comparator that sorts backwards.

You work the way maintainers work: read a failing test, fix or modernize the code under it, keep everything that already passes, and read the coverage report at the end.
You practise JUnit, hand-written fakes, Mockito mocks with `when`/`verify`, `assertThrows`, modernizing old idioms and JaCoCo coverage.

## Quick start

1. Open the lab project. The files you edit are in `src/main/java/legacy/`; the checklist below names them.
2. Select **Run Tests** before you change anything.

   **All 5 tests fail at first. That is expected: the old code does not meet the tests yet.**
   Every failure message starts with its task number, such as `Task 1:`.
3. Work through the checklist below in order. After each task, select **Run Tests** again and read the remaining `Task N:` messages.
4. **You are finished with Tasks 1-5 when all 5 tests pass.** Then do Task 6, which has no test, and select **Submit**.

```
OPEN project -> RUN TESTS: 5 fail (expected)
  -> READ the "Task N:" messages -> EDIT the code under the next task -> RUN TESTS again
  -> repeat for Tasks 1-5 -> all 5 pass -> Task 6: coverage report + one test of your own -> SUBMIT
```

In words: open the project, run the tests, read which tasks fail, fix one task, run the tests again, and when they all pass, read the coverage report, add one test and submit.

## Checklist

The files are in `src/main/java/legacy/`. Tasks 2, 4 and 5 are marked by `// Task N` comments above the old code you fix:

- [ ] Task 1: `FakePriceFeed.java`, `setPrice` and `priceOf` (a hand-written fake, marked `TODO`)
- [ ] Task 2: `OrderService.java`, `subtotal` (fix a bug, test-first)
- [ ] Task 3: `OrderService.java`, `unitPrice` (code driven by a Mockito mock, marked `TODO Task 3`)
- [ ] Task 4: `OrderService.java`, `shippingLabel` (modernize a switch)
- [ ] Task 5: `OrderService.java`, `cheapestFirst` (modernize a comparator)
- [ ] Task 6: read the coverage report, and add one test class of your own in `src/test/java/legacy/` (no test checks this task)

A task is done when no failure message starts with its number.
Edit only `FakePriceFeed.java` and `OrderService.java`, plus your one new test file for Task 6. Do not change anything else:
- `src/test/java/legacy/OrderServiceTest.java` is the specification: make it pass, and never edit it. The tests in `src/test/java/` are restored to their original state before your work is graded, so changes to them do not count, and your own Task 6 test is not part of the grade.
- `PriceFeed.java` is the complete dependency boundary.
- The build file `pom.xml` already sets up Mockito and the JaCoCo report.
- `expected/test-summary.txt` is the finished-state reference.

## Task steps

Read the code in this order:
1. `PriceFeed.java`, the complete dependency boundary;
2. the test class `src/test/java/legacy/OrderServiceTest.java`, the specification;
3. `OrderService.java`, the legacy code.

`OrderService` depends only on the `PriceFeed` interface, so the tests can hand it a fake or a mock. That design is what makes everything below possible.

### Task 1: the hand-written fake

1. Open `FakePriceFeed.java`. A fake is a tiny, real, in-memory implementation that the tests control.
2. Implement both methods:
   - `setPrice`: store the price in the map;
   - `priceOf`: return the stored price, or `-1.0` via `getOrDefault`. The interface promises "negative means unknown", and a fake must honor the contract it fakes.
3. While it is wrong, you see messages such as `Task 1: priceOf a sku that was set`.

### Task 2: fix the billing bug, test-first

1. Run the tests and read the task 2 failure: `subtotal("tea", 4)` must be `10.0`, but the old code answers `7.5`. The test is the specification; the fix comes second.
2. Find the off-by-one in `subtotal` and fix it. The edge assertions pin you down: one unit is not free, and zero units cost nothing.
3. Notice the test's shape, called arrange-act-assert:
   - arrange: a fake with one price;
   - act: one call;
   - assert: three expectations.

   Every test in this lab follows this shape; reuse it when you write your own.

### Task 3: drive the cache with a mock

1. Read `task3UnitPriceWithMock` in the test class first. It builds a Mockito mock with `mock(PriceFeed.class)`, and stubs it with `when(feed.priceOf("tea")).thenReturn(2.5)`.
   After two `unitPrice` calls, it demands `verify(feed, times(1)).priceOf("tea")`: the expensive feed may be asked only once per sku.
2. Implement `unitPrice`:
   - answer from the `cache` map when the sku is there;
   - otherwise ask the feed once;
   - reject a negative answer with `new IllegalArgumentException("unknown sku: " + sku)`;
   - store the price, and return it.
3. The same test stubs an unknown sku and expects your exception via `assertThrows`. The test knows the exception is coming, and treats catching it as the result.
4. Compare the two test doubles you now know:
   - the fake (Task 1) is a small real object, clearest for canned data;
   - the mock records interactions, which is the only way to test "called at most once".

   Choosing between them is a design decision, not a habit.

<details><summary>Hint</summary>

If Mockito reports `Wanted 1 time ... but was 2 times`, `unitPrice` still asks the feed on every call. Answer from the cache first.

</details>

### Task 4: modernize the fall-through switch

1. The task 4 test shows zone 1 answering `"regional"`. The old colon-style switch is missing a `break`, so case 1 falls through into case 2. This kind of bug is why the colon form counts as a legacy idiom.
2. Rewrite `shippingLabel` as an arrow-label switch expression (`case 1 -> "local"; ... default -> "unknown"`). Arrows cannot fall through, so the rewrite removes this bug and the whole kind of bug.

<details><summary>Hint</summary>

A switch expression ends with a semicolon after its closing brace: `return switch (zone) { ... };`.

</details>

### Task 5: modernize the comparator

1. The task 5 test wants `cheapestFirst` in ascending order. The old anonymous `Comparator` class sorts in descending order, and takes seven lines to do it.
2. Rewrite it as a lambda, `(a, b) -> Double.compare(unitPrice(a), unitPrice(b))`, or as `Comparator.comparing(this::unitPrice)`.
3. Keep returning a new list: the test checks that the input list is untouched.

### Task 6: read the coverage report and close one gap

No test checks this task; the coverage report itself is your result.

1. After all 5 tests pass, run the tests with coverage. In a terminal, from the project folder:

   ```
   mvn -q test -Pcoverage
   ```

   The first coverage run downloads the JaCoCo plugin, so it needs internet access. Then open `target/site/jacoco/index.html` in a web browser. JaCoCo wrote it during the test run.
2. Find the `legacy` package. Line coverage is high, but look at the branch column of `OrderService`:
   - `main` is red, because the tests never run it;
   - inside `unitPrice`, the cache-hit branch is covered only because task 3 calls it twice.
3. Think about what that means. Without the second `unitPrice` call, coverage would stay high while the cache went untested.
   High coverage proves the lines ran, never that they are right. The `verify(times(1))` is what actually pins down the cache.
4. Now close one gap. Add one test class of your own to `src/test/java/legacy/`, for example `subtotal` with a large quantity, or `shippingLabel` with a negative zone.
   Run the coverage command again, and watch the report change. Write the test before any code change it motivates, and keep it in your submission.

### About the test results

A passing run shows the service behaves as specified.
It does not check which Java constructs your code uses, so compare your code with each task's steps yourself.

## Using an AI assistant

If you use an AI assistant, ask it to explain, not to answer.
"Explain what verify(times(1)) checks" teaches you mocks; "fix the service for me" does not.
Check any explanation with a test run before you trust it.

## Run the program (optional)

The service runs against the fake feed. In a terminal, from the project folder:

```
mvn -q compile
java -cp target/classes legacy.OrderService
```

`expected/test-summary.txt` shows the finished result, including the five lines the program prints and where the coverage report is.

## Troubleshooting

- **A warning mentioning "Sharing is only supported for boot loader classes".** This is harmless: the test JVM runs under the JaCoCo agent.
- **The coverage run's test JVM fails to start, and the message mentions the Maven repository path.** A space in the local repository path, which is common in a Windows user folder, can break the agent's arguments.
  Point Maven at a repository path with no spaces, using `<localRepository>` in `~/.m2/settings.xml`, and run the coverage command again.
- **`COMPILATION ERROR`.** Your code does not compile yet. Maven names the file, then `[line,column]`, then the problem. Fix that line and run the tests again.
- **`Cannot access central in offline mode`, or a missing artifact, on the coverage run.** The JaCoCo plugin is not downloaded yet. Run `mvn -q test -Pcoverage` once while online. **Run Tests** does not need it.

## Cleanup (optional)

Maven writes only the `target` folder (compiled classes, test reports and the coverage report), and Git already ignores it.
To delete it, run `rm -rf target` from the project folder. In Windows PowerShell, run `Remove-Item -Recurse -Force target`.
