package javacafe.model;

import java.util.HashMap;
import java.util.Map;

public class Order {
    // Maps a Product to the quantity ordered
    private Map<Product, Integer> items;
    private static final double TAX_RATE = 0.10; // 10% tax for example

    public Order() {
        this.items = new HashMap<>();
    }

    public void addItem(Product product, int quantity) {
        items.put(product, items.getOrDefault(product, 0) + quantity);
    }

    public double calculateSubtotal() {
        double subtotal = 0;
        for (Map.Entry<Product, Integer> entry : items.entrySet()) {
            subtotal += entry.getKey().getPrice() * entry.getValue();
        }
        return subtotal;
    }

    public double calculateTax() {
        return calculateSubtotal() * TAX_RATE;
    }

    public double calculateTotal() {
        return calculateSubtotal() + calculateTax();
    }

    // Remove an item entirely from the cart
    public void removeItem(Product product) {
        items.remove(product);
    }

    public Map<Product, Integer> getItems() {
        return items;
    }
}
