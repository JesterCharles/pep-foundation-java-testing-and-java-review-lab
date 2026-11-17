package legacy;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Public tests for the Testing and Java Review lab. These tests came first:
 * they describe what the old service SHOULD do. Read a failing test, then
 * change the service until it passes. Every test follows arrange-act-assert.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class OrderServiceTest {

    @Test
    void task1FakePriceFeed() {
        // Arrange: a fake is a tiny real implementation the tests control.
        FakePriceFeed feed = new FakePriceFeed();
        feed.setPrice("tea", 2.5);
        // Act + assert: it answers what was set and honors the contract for the rest.
        assertAll(
                () -> assertEquals(2.5, feed.priceOf("tea"), "Task 1: priceOf a sku that was set"),
                () -> assertTrue(feed.priceOf("soap") < 0,
                        "Task 1: priceOf an unknown sku must be negative, as the interface promises"));
    }

    @Test
    void task2SubtotalBug() {
        // Arrange
        FakePriceFeed feed = new FakePriceFeed();
        feed.setPrice("tea", 2.5);
        OrderService service = new OrderService(feed);
        // Act + assert: 4 units at 2.5 are 10.0 - the old code billed one unit short.
        assertAll(
                () -> assertEquals(10.0, service.subtotal("tea", 4), "Task 2: subtotal(\"tea\", 4)"),
                () -> assertEquals(2.5, service.subtotal("tea", 1),
                        "Task 2: subtotal(\"tea\", 1), a single unit must not be free"),
                () -> assertEquals(0.0, service.subtotal("tea", 0), "Task 2: subtotal(\"tea\", 0)"));
    }

    @Test
    void task3UnitPriceWithMock() {
        // Arrange: a Mockito mock records every call; when(...) stubs its answers.
        PriceFeed feed = mock(PriceFeed.class);
        when(feed.priceOf("tea")).thenReturn(2.5);
        when(feed.priceOf("soap")).thenReturn(-1.0);
        OrderService service = new OrderService(feed);
        // Act
        double first = service.unitPrice("tea");
        double second = service.unitPrice("tea");
        // Assert: right value, and the expensive feed was asked only once.
        assertAll(
                () -> assertEquals(2.5, first, "Task 3: unitPrice(\"tea\")"),
                () -> assertEquals(2.5, second, "Task 3: unitPrice(\"tea\") again, from the cache"),
                () -> verify(feed, times(1)).priceOf("tea"));
        IllegalArgumentException unknown = assertThrows(IllegalArgumentException.class,
                () -> service.unitPrice("soap"),
                "Task 3: a negative feed answer means the sku is unknown and must be rejected");
        assertEquals("unknown sku: soap", unknown.getMessage(), "Task 3: the rejection names the sku");
    }

    @Test
    void task4ShippingLabel() {
        // Arrange
        OrderService service = new OrderService(new FakePriceFeed());
        // Act + assert: zone 1 exposes the old fall-through.
        assertAll(
                () -> assertEquals("local", service.shippingLabel(1), "Task 4: shippingLabel(1)"),
                () -> assertEquals("regional", service.shippingLabel(2), "Task 4: shippingLabel(2)"),
                () -> assertEquals("overseas", service.shippingLabel(3), "Task 4: shippingLabel(3)"),
                () -> assertEquals("unknown", service.shippingLabel(0), "Task 4: shippingLabel(0)"),
                () -> assertEquals("unknown", service.shippingLabel(9), "Task 4: shippingLabel(9)"));
    }

    @Test
    void task5CheapestFirst() {
        // Arrange
        FakePriceFeed feed = new FakePriceFeed();
        feed.setPrice("tea", 2.5);
        feed.setPrice("mug", 8.0);
        feed.setPrice("pen", 1.0);
        OrderService service = new OrderService(feed);
        List<String> shelf = List.of("tea", "mug", "pen");
        // Act
        List<String> sorted = service.cheapestFirst(shelf);
        // Assert: ascending by price, and the input list is untouched.
        assertAll(
                () -> assertEquals(List.of("pen", "tea", "mug"), sorted,
                        "Task 5: cheapest first means ascending by unit price"),
                () -> assertEquals(List.of("tea", "mug", "pen"), shelf,
                        "Task 5: the method returns a new list and leaves the input alone"));
    }
}
