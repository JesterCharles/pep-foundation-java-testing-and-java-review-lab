# Testing and Java Review lab: rescue an older service

You take over `OrderService.java`, an order service written years ago, in five linked tasks in the package `legacy`.
The tests came first. They describe what the service SHOULD do, and the old code fails them:
- a billing bug;
- a price feed that gets called on every request instead of cached;
- a switch that falls through;
- a comparator that sorts backwards.

You practise JUnit, hand-written fakes, Mockito mocks with `when`/`verify`, `assertThrows`, modernizing old idioms and JaCoCo coverage.

The full learner instructions are in [`.evolv/instructions.md`](.evolv/instructions.md), which the lab platform shows as the lab's instructions.

## Layout

This repository follows the Exa coding-lab scaffold for Java.

| Path | What it is |
|---|---|
| `src/main/java/legacy/PriceFeed.java` | Given interface, the service's dependency boundary |
| `src/main/java/legacy/FakePriceFeed.java` | Lab source (Task 1) |
| `src/main/java/legacy/OrderService.java` | Lab source (Tasks 2-5) |
| `src/test/java/legacy/OrderServiceTest.java` | Visible tests |
| `.evolv/instructions.md` | Learner instructions |
| `.evolv/grade.yml` | Grading manifest, used by Run Tests and by the grader |
| `pom.xml` | Maven build: Java 17, JUnit 5.11.3 and Mockito; a `coverage` profile adds JaCoCo |
| `Dockerfile` | Image for local Run Tests; warms the Maven cache |
| `expected/test-summary.txt` | The result of a finished lab |

There are no held-out tests in this lab: every test is visible.

## Run the tests

On the platform, select **Run Tests**. From a terminal with JDK 17 or later and Apache Maven 3.9:

```
mvn -q test
```

All 5 tests fail until the tasks are done. That is expected.
For the coverage report (Task 6), run `mvn -q test -Pcoverage`.
