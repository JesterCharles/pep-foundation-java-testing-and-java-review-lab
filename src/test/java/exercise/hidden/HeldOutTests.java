package exercise.hidden;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import exercise.FakePriceFeed;
import exercise.OrderService;
import exercise.PriceFeed;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Held-out tests: graded on submit, never shown to the learner. The edge cases of each task. */
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

    @Test
    void task4UnknownZones() {
        // Arrange
        OrderService service = new OrderService(new FakePriceFeed());
        // Act + assert: every other zone is unknown.
        assertAll(
                () -> assertEquals("unknown", service.shippingLabel(0), "Task 4: shippingLabel(0)"),
                () -> assertEquals("unknown", service.shippingLabel(9), "Task 4: shippingLabel(9)"));
    }

    @Test
    void task5InputUntouched() {
        // Arrange
        FakePriceFeed feed = new FakePriceFeed();
        feed.setPrice("tea", 2.5);
        feed.setPrice("mug", 8.0);
        feed.setPrice("pen", 1.0);
        OrderService service = new OrderService(feed);
        List<String> shelf = List.of("tea", "mug", "pen");
        // Act
        service.cheapestFirst(shelf);
        // Assert: the method returns a new list and leaves the input alone.
        assertEquals(List.of("tea", "mug", "pen"), shelf,
                "Task 5: the method returns a new list and leaves the input alone");
    }
}
