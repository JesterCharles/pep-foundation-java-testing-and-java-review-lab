package legacy;

/**
 * The price source the order service depends on. This interface is complete.
 * It is the service's dependency boundary: because OrderService only knows
 * this interface, a test can hand in a hand-written fake or a Mockito mock
 * instead of a real feed - that is what makes the service testable.
 */
public interface PriceFeed {

    /** The unit price of the sku, or a negative value when the sku is unknown. */
    double priceOf(String sku);
}
