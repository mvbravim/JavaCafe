package javacafe.model;

import javacafe.exceptions.OutOfStockException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private List<Product> products;
    private String dataFilePath;

    public Inventory(String dataFilePath) {
        this.dataFilePath = dataFilePath;
        this.products = new ArrayList<>();
        loadInventory();
    }

    private void loadInventory() {
        try (BufferedReader br = new BufferedReader(new FileReader(dataFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                // Now expecting 5 columns because of the new 'category' column
                if (values.length >= 5) {
                    String id = values[0].trim();
                    String name = values[1].trim();
                    double price = Double.parseDouble(values[2].trim());
                    int quantity = Integer.parseInt(values[3].trim());
                    String category = values[4].trim(); // NEW COLUMN
                    products.add(new Product(id, name, price, quantity, category));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading inventory: " + e.getMessage());
        }
    }

    public List<Product> getProducts() {
        return products;
    }

    public void reduceStock(Product product, int quantityToReduce) throws OutOfStockException {
        if (product.getStockQuantity() < quantityToReduce) {
            throw new OutOfStockException("Estoque insuficiente para o produto: " + product.getName() + "!\nQuantidade disponível: " + product.getStockQuantity());
        }
        product.setStockQuantity(product.getStockQuantity() - quantityToReduce);
        saveInventory(); // Save changes automatically
    }

    // Save current inventory state back to the CSV file
    public void saveInventory() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(dataFilePath))) {
            for (Product p : products) {
                pw.println(p.getId() + "," + p.getName() + "," + p.getPrice() + "," + p.getStockQuantity() + "," + p.getCategory());
            }
        } catch (IOException e) {
            System.err.println("Error saving inventory: " + e.getMessage());
        }
    }

    public void addProduct(Product p) {
        products.add(p);
        saveInventory();
    }

    public void removeProduct(String id) {
        products.removeIf(p -> p.getId().equals(id));
        saveInventory();
    }
}
