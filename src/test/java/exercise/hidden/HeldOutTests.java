package exercise.hidden;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import exercise.FakePriceFeed;
import exercise.OrderService;
import exercise.PriceFeed;
import org.junit.jupiter.api.Test;

/** Held-out tests: graded on submit, never shown to the learner. Edge cases for Tasks 2 and 3. */
class HeldOutTests {

    @Test
    void task2SubtotalEdgeQuantities() {
        // Arrange
        FakePriceFeed feed = new FakePriceFeed();
        feed.setPrice("tea", 2.5);
        OrderService service = new OrderService(feed);
        // Act + assert: one unit is not free, and zero units cost nothing.
        assertAll(
                () -> assertEquals(2.5, service.subtotal("tea", 1),
                        "Task 2: subtotal(\"tea\", 1), a single unit must not be free"),
                () -> assertEquals(0.0, service.subtotal("tea", 0), "Task 2: subtotal(\"tea\", 0)"));
    }

    @Test
    void task3UnknownSkuMessage() {
        // Arrange
        PriceFeed feed = mock(PriceFeed.class);
        when(feed.priceOf("soap")).thenReturn(-1.0);
        OrderService service = new OrderService(feed);
        // Act
        IllegalArgumentException unknown = assertThrows(IllegalArgumentException.class,
                () -> service.unitPrice("soap"),
                "Task 3: a negative feed answer means the sku is unknown and must be rejected");
        // Assert
        assertEquals("unknown sku: soap", unknown.getMessage(), "Task 3: the rejection names the sku");
    }
}
