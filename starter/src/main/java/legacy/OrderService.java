package legacy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Testing and Java Review lab: an older order service, inherited as-is.
 * This class was written years ago: it has a known billing bug, it hammers
 * the price feed instead of caching, its switch falls through, and its
 * comparator is an anonymous class that sorts the wrong way. Your job is the
 * daily job of a developer: read the failing tests first, then fix and
 * modernize the code underneath them, task by task. The public tests describe
 * the intended behavior; make them pass without breaking the ones already
 * green. The README lists the steps for every task.
 */
public class OrderService {

    private final PriceFeed feed;
    private final HashMap<String, Double> cache = new HashMap<>();

    public OrderService(PriceFeed feed) {
        this.feed = feed;
    }

    // Task 2 (fix a bug test-first): the price of quantity units of the sku.
    // The task 2 test shows the intended behavior; run it, read the failure,
    // then fix the line below. The original author shipped an off-by-one.
    public double subtotal(String sku, int quantity) {
        return unitPrice(sku) * (quantity - 1);
    }

    // Task 3 (drive code with a mock): one unit price, asked of the feed AT
    // MOST ONCE per sku - later calls answer from the cache - and an unknown
    // sku (negative feed answer) rejected with
    //   new IllegalArgumentException("unknown sku: " + sku)
    // The task 3 test uses a Mockito mock to count the feed calls and to
    // stub the unknown sku; make its when/verify story true.
    public double unitPrice(String sku) {
        return feed.priceOf(sku); // TODO Task 3: cache, and reject unknown skus
    }

    // Task 4 (modernize the switch): the shipping label for a zone:
    // 1 is "local", 2 is "regional", 3 is "overseas", anything else "unknown".
    // This colon switch has a fall-through bug - zone 1 runs into case 2.
    // Rewrite it as an arrow-label switch expression; arrows cannot fall
    // through, so the bug class disappears with the rewrite.
    public String shippingLabel(int zone) {
        String label = "unknown";
        switch (zone) {
            case 1:
                label = "local";
            case 2:
                label = "regional";
                break;
            case 3:
                label = "overseas";
                break;
        }
        return label;
    }

    // Task 5 (modernize the comparator): the skus sorted by unit price,
    // cheapest first, as a new list. The anonymous class below is the old
    // idiom and it also sorts the wrong way around. Rewrite the comparator as
    // a lambda (or Comparator.comparing) that sorts ascending.
    public List<String> cheapestFirst(List<String> skus) {
        ArrayList<String> sorted = new ArrayList<>(skus);
        Collections.sort(sorted, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return Double.compare(unitPrice(b), unitPrice(a));
            }
        });
        return sorted;
    }

    public static void main(String[] args) {
        FakePriceFeed feed = new FakePriceFeed();
        feed.setPrice("tea", 2.5);
        feed.setPrice("mug", 8.0);
        feed.setPrice("pen", 1.0);
        OrderService service = new OrderService(feed);
        System.out.println("Subtotal: " + service.subtotal("tea", 4));
        System.out.println("Unit: " + service.unitPrice("mug"));
        System.out.println("Zone 1: " + service.shippingLabel(1));
        System.out.println("Zone 3: " + service.shippingLabel(3));
        System.out.println("Cheapest first: " + service.cheapestFirst(List.of("tea", "mug", "pen")));
    }
}
