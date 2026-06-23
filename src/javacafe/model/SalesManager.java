package javacafe.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalesManager {
    private String salesFilePath;

    public enum ReportPeriod {
        TODAY, WEEK, MONTH
    }

    public SalesManager(String salesFilePath) {
        this.salesFilePath = salesFilePath;
    }

    // Save a completed order to the CSV file
    public void recordSale(Order order) {
        // "true" means appending to the file, not overwriting it
        try (PrintWriter pw = new PrintWriter(new FileWriter(salesFilePath, true))) {
            
            // Get the current date and time
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            double totalRevenue = order.calculateTotal();
            
            // Build a text string showing exactly which items were sold and their quantities using IDs
            StringBuilder itemsSold = new StringBuilder();
            for (java.util.Map.Entry<Product, Integer> entry : order.getItems().entrySet()) {
                if (itemsSold.length() > 0) {
                    itemsSold.append(";"); // Separate multiple items with a semicolon
                }
                itemsSold.append(entry.getKey().getId()).append(":").append(entry.getValue());
            }
            
            // Write the line: Date, TotalRevenue, ItemsSold
            // Using replace to ensure standard dot notation for decimals in the CSV
            pw.println(timestamp + "," + String.format("%.2f", totalRevenue).replace(",", ".") + "," + itemsSold.toString());
            
        } catch (IOException e) {
            System.err.println("Error saving sale: " + e.getMessage());
        }
    }

    // Generate a formatted report string reading from the CSV based on a period
    public String generateReportText(Inventory inventory, ReportPeriod period) {
        File f = new File(salesFilePath);
        if (!f.exists()) return "Nenhuma venda registrada ainda.";

        int numTransactions = 0;
        double totalRevenue = 0.0;
        Map<String, Integer> itemsSoldMap = new HashMap<>();
        
        LocalDateTime now = LocalDateTime.now();

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    LocalDateTime saleDate = LocalDateTime.parse(parts[0].trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    
                    boolean include = false;
                    switch (period) {
                        case TODAY:
                            include = saleDate.toLocalDate().equals(now.toLocalDate());
                            break;
                        case WEEK:
                            // Considering the last 7 days as 'current week' for simplicity
                            include = saleDate.isAfter(now.minusDays(7));
                            break;
                        case MONTH:
                            include = saleDate.getYear() == now.getYear() && saleDate.getMonth() == now.getMonth();
                            break;
                    }
                    
                    if (!include) {
                        continue; // Skip this line if it's outside the timeframe
                    }

                    numTransactions++;
                    totalRevenue += Double.parseDouble(parts[1].trim());

                    // Parse items format: "P001:2;P006:1"
                    String[] items = parts[2].split(";");
                    for (String itemData : items) {
                        String[] itemParts = itemData.split(":");
                        if (itemParts.length == 2) {
                            String pId = itemParts[0].trim();
                            int qty = Integer.parseInt(itemParts[1].trim());
                            itemsSoldMap.put(pId, itemsSoldMap.getOrDefault(pId, 0) + qty);
                        }
                    }
                }
            }
        } catch (Exception e) {
            return "Erro ao ler as vendas: " + e.getMessage();
        }

        // Sort items by quantity sold to find Top 3 (Descending order)
        List<Map.Entry<String, Integer>> sortedItems = new ArrayList<>(itemsSoldMap.entrySet());
        sortedItems.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        StringBuilder report = new StringBuilder();
        
        String title = "=== RELATÓRIO DE VENDAS ===\n\n";
        if (period == ReportPeriod.TODAY) title = "=== VENDAS DE HOJE ===\n\n";
        else if (period == ReportPeriod.WEEK) title = "=== VENDAS DA SEMANA (Últimos 7 Dias) ===\n\n";
        else if (period == ReportPeriod.MONTH) title = "=== VENDAS DO MÊS ===\n\n";
        
        report.append(title);
        report.append("Total de Transações: ").append(numTransactions).append("\n");
        report.append("Receita Total: R$").append(String.format("%.2f", totalRevenue)).append("\n\n");
        
        report.append("--- TOP 3 PRODUTOS MAIS VENDIDOS ---\n");
        int count = 0;
        for (Map.Entry<String, Integer> entry : sortedItems) {
            if (count >= 3) break; // Only show top 3
            
            String pId = entry.getKey();
            int qty = entry.getValue();
            
            // Look up product name from inventory using ID
            String pName = "Desconhecido (" + pId + ")";
            for (Product p : inventory.getProducts()) {
                if (p.getId().equals(pId)) {
                    pName = p.getName();
                    break;
                }
            }
            
            report.append(count + 1).append(". ").append(pName).append(" - ").append(qty).append(" unidades vendidas\n");
            count++;
        }

        return report.toString();
    }
}
