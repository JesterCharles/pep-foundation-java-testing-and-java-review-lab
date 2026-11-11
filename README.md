# Testing and Java Review lab: rescue an older service

## Goal

Take over `OrderService.java`, an order service written years ago, in five linked tasks in the package `legacy`.
The tests came first: they describe what the service SHOULD do, and the old code fails them - a billing bug, a feed that gets hammered instead of cached, a switch that falls through, a backwards comparator.
You work the way maintainers work: read a failing test, fix or modernize the code under it, keep everything already green, and read the coverage report at the end.
You practise JUnit, hand-written fakes, Mockito mocks with `when`/`verify`, `assertThrows`, modernizing old idioms and JaCoCo coverage on one real-feeling service.
You build and test it with the real JDK and Maven on your own computer.

## Files

**Edit exactly two files.** The tests came first; their failure lines carry the task numbers:

- `starter/src/main/java/legacy/FakePriceFeed.java` - Task 1: the hand-written fake
- `starter/src/main/java/legacy/OrderService.java` - Tasks 2-5: fix and modernize the legacy service under the tests

**Task 6 adds one file of your own:** a new test class in `tests/legacy/` - that is the only change you make under `tests/`.

**Do not change anything else.** In particular:

- `tests/legacy/OrderServiceTest.java` - the shipped specification; make it pass, never edit it
- `starter/src/main/java/legacy/PriceFeed.java` - the complete dependency boundary
- `starter/pom.xml` - the build, Mockito agent and JaCoCo report are prewired
- `expected/test-summary.txt` - the finished-state reference

## Prerequisites

You need JDK 25 and Apache Maven 3.9 or later, the same setup as the earlier Java labs.
Check both in a new terminal:

```
java -version
mvn -v
```

- `java -version` must report version 25.
- `mvn -v` must report Apache Maven 3.9 or later, and its `Java version:` line must also say 25.

This lab works the same on macOS, Linux and Windows.
On Windows, use PowerShell or Command Prompt; the commands are the same.
No Docker or other container tool is needed.

## Setup (once, needs the internet)

Open a terminal in the lab's root folder - the one that holds this README, `starter/` and `tests/` - and change into `starter` with `cd starter`; every command in this lab runs there. Then run:

```
mvn -q dependency:go-offline
```

It prints nothing when it succeeds.
This lab additionally downloads Mockito 5.24.0 (a test-scoped dependency) and the JaCoCo coverage plugin (a build plugin) - both pinned in the POM. Look at the POM and notice that the shipped program depends on neither: test-scope dependencies and build plugins never ship with the service.
After this step, everything else in the lab runs offline.

## Tasks

Open the lab's root folder in your IDE or editor; it holds one Maven project, `starter/pom.xml`, and if your editor asks, open or import that file as a Maven project. Whatever your editor shows, the test command below, run in a terminal in `starter`, is the result that counts.
Read in this order: `PriceFeed.java` (the dependency boundary - complete), the test class in `tests/legacy/OrderServiceTest.java` (the specification), then `OrderService.java` (the legacy code).
Because `OrderService` depends only on the `PriceFeed` interface, the tests can hand it a fake or a mock - that design is what makes everything below possible.

After each task, run the one test command from the `starter` folder:

```
mvn -q -o test
```

Every failure line starts with its task number, so you can work through the tasks in order.

### Task 1: the hand-written fake

1. Open `FakePriceFeed.java`: a fake is a tiny, real, in-memory implementation the tests control.
2. Implement `setPrice` (store in the map) and `priceOf` (return the stored price, or `-1.0` via `getOrDefault` - the interface promises "negative means unknown", and a fake must honor the contract it fakes).
3. While it is wrong, you see lines such as `Task 1: priceOf a sku that was set`.
4. Done when no line starting with `Task 1` appears.

### Task 2: fix the billing bug, test-first

1. Run the tests and read the task 2 failure: `subtotal("tea", 4)` must be `10.0`, the old code answers `7.5`. The test is the specification; the fix comes second.
2. Find the off-by-one in `subtotal` and fix it. The edge assertions pin you down: one unit is not free, zero units cost nothing.
3. Notice the test's shape - arrange (a fake with one price), act (one call), assert (three expectations). Every test in this lab follows arrange-act-assert; reuse the shape when you write your own.
4. Done when no line starting with `Task 2` appears.

### Task 3: drive the cache with a mock

1. Read `task3UnitPriceWithMock` in the test class first. It builds a Mockito mock - `mock(PriceFeed.class)` - stubs it with `when(feed.priceOf("tea")).thenReturn(2.5)`, and after two `unitPrice` calls demands `verify(feed, times(1)).priceOf("tea")`: the expensive feed may be asked only once per sku.
2. Implement `unitPrice`: answer from the `cache` map when the sku is there; otherwise ask the feed once, reject a negative answer with `new IllegalArgumentException("unknown sku: " + sku)`, store the price, return it.
3. The same test stubs an unknown sku and expects your exception via `assertThrows` - the test knows the exception is coming and catches it as the result.
4. Compare the two doubles you now know: the fake (Task 1) is a small real object, clearest for canned data; the mock records interactions, which is the only way to test "called at most once". Choosing between them is a design decision, not a habit.
5. Done when no line starting with `Task 3` appears.

### Task 4: modernize the fall-through switch

1. The task 4 test shows zone 1 answering `"regional"` - the old colon switch is missing a `break`, so case 1 falls into case 2. This bug class is why the colon form is a legacy idiom.
2. Rewrite `shippingLabel` as an arrow-label switch expression (`case 1 -> "local"; ... default -> "unknown"`). Arrows cannot fall through: the rewrite removes the bug and the bug class at once.
3. Done when no line starting with `Task 4` appears.

### Task 5: modernize the comparator

1. The task 5 test wants `cheapestFirst` ascending; the old anonymous `Comparator` class sorts descending - and takes seven lines to do it.
2. Rewrite it as a lambda - `(a, b) -> Double.compare(unitPrice(a), unitPrice(b))` - or `Comparator.comparing(this::unitPrice)`. Keep returning a new list; the test checks the input is untouched.
3. Done when no line starting with `Task 5` appears and the test run exits with status 0.

### Task 6: read the coverage report and close one gap

No test checks this task - the coverage report itself is your result.

1. After a full green run, open `starter/target/site/jacoco/index.html` in a browser (JaCoCo wrote it during the test run).
2. Find the `legacy` package: line coverage is high, but look at the branch column of `OrderService` - `main` is red (tests never run it), and inside `unitPrice` the cache-hit branch is only covered because task 3 calls twice.
3. Try it: comment out the second `unitPrice` call idea in your head - coverage would stay high while the cache went untested. High coverage proves the lines ran, never that they are right; the `verify(times(1))` is what actually pins the cache.
4. Now close one gap: add one test of your own to `tests/legacy/` - for example `subtotal` with a large quantity, or `shippingLabel` with a negative zone - run the tests, and watch the report change. Write the test before any code change it motivates, and keep it in your submission.

## Using an AI assistant

If you use an AI assistant in this lab, ask it to explain, not to answer: "explain what verify(times(1)) checks" teaches you mocks; "fix the service for me" does not.
Verify any explanation against a test run before you trust it.

## Test

The one test command, run from the `starter` folder, is:

```
mvn -q -o test
```

- Before you start, all five tests fail, and Maven ends with `Tests run: 5, Failures: 5`.
- Each test lists what behavior was wrong. A passing run shows the service behaves as specified; it does not check which Java constructs your code uses, so compare your code with each task's steps yourself.
- If your code does not compile, Maven prints `COMPILATION ERROR` with the file, then `[line,column]`, then the compiler's message. Fix that line and run the test again.
- The JVM may print one harmless warning about class data sharing; it comes from the Mockito and JaCoCo agents and is not a failure.

You can also run the service itself against the fake feed:

```
mvn -q -o compile
java -cp target/classes legacy.OrderService
```

## Expected output

When every task is complete, `mvn -q -o test` exits with status 0.
`expected/test-summary.txt` shows the full expected result, including the five lines the program prints and the coverage report location.

## Offline note

Only the setup step uses the network.
The test command uses `-o` (offline), so it never downloads anything.
If it reports that an artifact is missing from the local repository, run the setup step once more while online.

## Cleanup

The lab writes only the `starter/target` folder (compiled classes, test reports and the JaCoCo report). Delete it from the `starter` folder:

- macOS and Linux: `rm -rf target`
- Windows PowerShell: `Remove-Item -Recurse -Force target`

The JUnit, Mockito, JaCoCo and plugin downloads stay in your local Maven repository (`~/.m2/repository` by default) so later Java labs can reuse them.

`starter/.gitignore` already keeps Maven's `starter/target` folder out of Git. Your editor and operating system may also create local files in the lab's root folder, such as `.idea/`, `.vscode/` or `.DS_Store`; they are not part of the lab, so if `git status` lists any of them, add them to a `.gitignore` file in the lab's root folder instead of committing them.

## Troubleshooting

- **`mvn: command not found`, or `'mvn' is not recognized`.** Maven's `bin` folder is not on your `PATH`. Add it, then open a new terminal.
- **`mvn -v` shows a Java version other than 25.** Maven uses the JDK named by `JAVA_HOME` when it is set, otherwise the `java` on your `PATH`. Set `JAVA_HOME` to your JDK 25 folder, or unset it, then open a new terminal.
- **`release version 25 not supported`.** Maven is running on an older JDK. Fix it as in the previous item.
- **`Cannot access central in offline mode`, or a missing artifact.** The setup step has not completed. Run `mvn -q dependency:go-offline` once while online.
- **`Wanted 1 time ... but was 2 times` from Mockito.** `unitPrice` still asks the feed on every call; answer from the cache first.
- **A warning mentioning "Sharing is only supported for boot loader classes".** Harmless: the test JVM runs under the Mockito and JaCoCo agents.
- **The test JVM fails to start and the message mentions the Maven repository path.** A space in the local repository path (common in a Windows user folder) can break agent arguments; point Maven at a space-free repository via `<localRepository>` in `~/.m2/settings.xml`, rerun setup, then test again.
- **`unreachable code` after rewriting the switch.** A switch expression ends with a semicolon after the closing brace: `return switch (zone) { ... };`.
