package legacy;

import java.util.HashMap;

/**
 * Task 1: a hand-written fake price feed.
 * A fake is a real, working implementation that lives in memory: the tests
 * set a few prices and the service under test never notices the difference.
 * Compare it with the Mockito mock in the task 3 test - for simple canned
 * data like this, the fake is often the clearer tool.
 */
public class FakePriceFeed implements PriceFeed {

    private final HashMap<String, Double> prices = new HashMap<>();

    // Task 1: remember the price for the sku.
    public void setPrice(String sku, double price) {
        // TODO: store it in the map
    }

    // Task 1: the stored price, or -1.0 for a sku nobody set - the interface
    // says "negative means unknown", and the fake must honor the contract.
    @Override
    public double priceOf(String sku) {
        return 0; // TODO: read the map with getOrDefault
    }
}
