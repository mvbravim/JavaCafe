package javacafe.model;

public class Product {
    private String id;
    private String name;
    private double price;
    private int stockQuantity;
    private String category; // NEW FIELD

    public Product(String id, String name, double price, int stockQuantity, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
    public String getCategory() { return category; } // NEW GETTER

    // Setters
    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return name + " ($" + String.format("%.2f", price) + ")";
    }
}
