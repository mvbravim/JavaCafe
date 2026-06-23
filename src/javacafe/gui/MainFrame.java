package javacafe.gui;

import javacafe.exceptions.OutOfStockException;
import javacafe.model.Inventory;
import javacafe.model.Order;
import javacafe.model.Product;
import javacafe.model.SalesManager;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends JFrame {

    private Inventory inventory;
    private Order currentOrder;
    private SalesManager salesManager;
    
    private JTabbedPane menuTabbedPane; // NEW FIELD
    private JTextArea orderTextArea;
    private JLabel totalLabel;
    
    private javax.swing.table.DefaultTableModel inventoryTableModel; // NEW FIELD

    public MainFrame() {
        setTitle("Java Café - Point of Sale");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Initialize backend models
        inventory = new Inventory("data/inventory.csv");
        currentOrder = new Order();
        salesManager = new SalesManager("data/sales.csv");
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // --- FIRST TAB: POINT OF SALE ---
        JPanel posPanel = new JPanel(new BorderLayout());
        
        // --- 1. Left Side: Menu Tabs ---
        menuTabbedPane = new JTabbedPane();
        menuTabbedPane.setBorder(BorderFactory.createTitledBorder("Opções do Menu"));
        refreshMenuTabs(); // Draw the menu based on the inventory
        
        // --- 2. Right Side: Order Summary (The "Cart") ---
        JPanel orderPanel = new JPanel();
        orderPanel.setLayout(new BorderLayout());
        orderPanel.setBorder(BorderFactory.createTitledBorder("Pedido Atual"));
        orderPanel.setPreferredSize(new Dimension(300, 0)); 
        
        orderTextArea = new JTextArea();
        orderTextArea.setEditable(false); 
        orderTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        orderPanel.add(new JScrollPane(orderTextArea), BorderLayout.CENTER);
        
        // Bottom part of the order panel
        JPanel checkoutPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        checkoutPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        totalLabel = new JLabel("Total: R$0.00", SwingConstants.CENTER);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 20));
        
        // Button to remove items from the cart
        JButton removeItmBtn = new JButton("REMOVER ITEM");
        removeItmBtn.setBackground(Color.RED);
        removeItmBtn.setForeground(Color.WHITE);
        removeItmBtn.setFont(new Font("Arial", Font.BOLD, 24));
        removeItmBtn.addActionListener(e -> {
            if (currentOrder.getItems().isEmpty()) {
                JOptionPane.showMessageDialog(this, "O carrinho está vazio!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Convert the Set of products in the cart to an Array for the Dropdown menu
            Product[] productsInCart = currentOrder.getItems().keySet().toArray(new Product[0]);
            
            // Show a popup with a dropdown menu
            Product toRemove = (Product) JOptionPane.showInputDialog(this, 
                "Selecione o produto para remover do carrinho:", 
                "Remover Item", 
                JOptionPane.QUESTION_MESSAGE, 
                null, 
                productsInCart, 
                productsInCart[0]);
            
            if (toRemove != null) {
                currentOrder.removeItem(toRemove);
                updateOrderDisplay();
            }
        });
        
        JButton checkoutButton = new JButton("FINALIZAR VENDA");
        checkoutButton.setBackground(Color.GREEN.darker()); 
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setFont(new Font("Arial", Font.BOLD, 24));
        
        checkoutButton.addActionListener(e -> finalizeSale());
        
        checkoutPanel.add(totalLabel);
        checkoutPanel.add(removeItmBtn);
        checkoutPanel.add(checkoutButton);
        
        orderPanel.add(checkoutPanel, BorderLayout.SOUTH);
        
        // --- Add everything to the POS Panel ---
        posPanel.add(menuTabbedPane, BorderLayout.CENTER);
        posPanel.add(orderPanel, BorderLayout.EAST);
        
        // --- SECOND TAB: INVENTORY MANAGEMENT ---
        JPanel inventoryPanel = new JPanel(new BorderLayout());
        
        // Define the columns for the table
        String[] columnNames = {"ID", "Nome", "Preço (R$)", "Quantidade", "Categoria"};
        
        // Create the table model (this holds the data)
        inventoryTableModel = new javax.swing.table.DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent typing directly into the table cells
            }
        };
        
        refreshInventoryTable(); // Fill the table with data
        
        // Create the actual visual Table
        JTable inventoryTable = new JTable(inventoryTableModel);
        inventoryTable.setFillsViewportHeight(true);
        inventoryTable.setFont(new Font("Arial", Font.PLAIN, 14));
        inventoryTable.setRowHeight(25);
        
        // Add a scroll pane to allow scrolling if there are too many products
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        inventoryPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add a button panel at the bottom for updating stock
        JPanel inventoryButtonPanel = new JPanel(new FlowLayout());
        
        JButton addBtn = new JButton("Adicionar Produto");
        JButton removeBtn = new JButton("Remover Selecionado");
        JButton priceBtn = new JButton("Alterar Preço");
        JButton updateStockButton = new JButton("Alterar Estoque");
        
        // --- ADD PRODUCT LOGIC ---
        addBtn.addActionListener(e -> {
            JTextField idField = new JTextField(5);
            JTextField nameField = new JTextField(15);
            JTextField priceField = new JTextField(5);
            JTextField qtyField = new JTextField(5);
            JTextField catField = new JTextField(15);

            JPanel myPanel = new JPanel(new GridLayout(5, 2, 5, 5));
            myPanel.add(new JLabel("ID:")); myPanel.add(idField);
            myPanel.add(new JLabel("Nome:")); myPanel.add(nameField);
            myPanel.add(new JLabel("Preço:")); myPanel.add(priceField);
            myPanel.add(new JLabel("Quantidade:")); myPanel.add(qtyField);
            myPanel.add(new JLabel("Categoria:")); myPanel.add(catField);

            int result = JOptionPane.showConfirmDialog(null, myPanel, "Novo Produto", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    double price = Double.parseDouble(priceField.getText().replace(",", "."));
                    int qty = Integer.parseInt(qtyField.getText());
                    Product newProd = new Product(idField.getText(), nameField.getText(), price, qty, catField.getText());
                    inventory.addProduct(newProd);
                    refreshInventoryTable();
                    refreshMenuTabs();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Valores inválidos!", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // --- REMOVE PRODUCT LOGIC ---
        removeBtn.addActionListener(e -> {
            int selectedRow = inventoryTable.getSelectedRow();
            if (selectedRow != -1) {
                String productId = (String) inventoryTableModel.getValueAt(selectedRow, 0);
                inventory.removeProduct(productId);
                refreshInventoryTable();
                refreshMenuTabs();
            }
        });

        // --- CHANGE PRICE LOGIC ---
        priceBtn.addActionListener(e -> {
            int selectedRow = inventoryTable.getSelectedRow();
            if (selectedRow != -1) {
                String productId = (String) inventoryTableModel.getValueAt(selectedRow, 0);
                Product selectedProduct = null;
                for (Product p : inventory.getProducts()) {
                    if (p.getId().equals(productId)) { selectedProduct = p; break; }
                }
                if (selectedProduct != null) {
                    String newPriceStr = JOptionPane.showInputDialog(this, "Novo preço para " + selectedProduct.getName() + ":", selectedProduct.getPrice());
                    if (newPriceStr != null && !newPriceStr.trim().isEmpty()) {
                        try {
                            double newPrice = Double.parseDouble(newPriceStr.replace(",", "."));
                            selectedProduct.setPrice(newPrice);
                            inventory.saveInventory();
                            refreshInventoryTable();
                            refreshMenuTabs();
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(this, "Preço inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
        
        // --- CHANGE STOCK LOGIC ---
        updateStockButton.addActionListener(e -> {
            int selectedRow = inventoryTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Por favor, selecione um produto na tabela primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String productId = (String) inventoryTableModel.getValueAt(selectedRow, 0);
            
            Product selectedProduct = null;
            for (Product p : inventory.getProducts()) {
                if (p.getId().equals(productId)) {
                    selectedProduct = p;
                    break;
                }
            }
            
            if (selectedProduct != null) {
                String newQtyStr = JOptionPane.showInputDialog(this, 
                    "Digite a nova quantidade para " + selectedProduct.getName() + ":", 
                    selectedProduct.getStockQuantity());
                    
                if (newQtyStr != null && !newQtyStr.trim().isEmpty()) {
                    try {
                        int newQty = Integer.parseInt(newQtyStr);
                        if (newQty < 0) throw new NumberFormatException(); 
                        
                        selectedProduct.setStockQuantity(newQty);
                        inventory.saveInventory(); 
                        refreshInventoryTable(); 
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Quantidade inválida!", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        
        inventoryButtonPanel.add(addBtn);
        inventoryButtonPanel.add(removeBtn);
        inventoryButtonPanel.add(priceBtn);
        inventoryButtonPanel.add(updateStockButton);
        inventoryPanel.add(inventoryButtonPanel, BorderLayout.SOUTH);
        
        // --- THIRD TAB: SALES REPORTS ---
        JPanel reportPanel = new JPanel(new BorderLayout());
        
        JLabel reportLabel = new JLabel("", SwingConstants.CENTER);
        reportLabel.setFont(new Font("Monospaced", Font.PLAIN, 16));
        reportLabel.setVerticalAlignment(SwingConstants.TOP);
        
        // Wrap it in a JPanel with a white background so it looks like a document
        JPanel labelWrapper = new JPanel(new BorderLayout());
        labelWrapper.setBackground(Color.WHITE);
        labelWrapper.add(reportLabel, BorderLayout.CENTER);
        
        reportPanel.add(new JScrollPane(labelWrapper), BorderLayout.CENTER);
        
        // Add a control panel at the bottom for the dropdown and button
        JPanel controlPanel = new JPanel(new FlowLayout());
        
        String[] periods = {"Hoje", "Esta Semana", "Este Mês"};
        JComboBox<String> periodCombo = new JComboBox<>(periods);
        
        JButton generateReportBtn = new JButton("Gerar Relatório");
        generateReportBtn.setFont(new Font("Arial", Font.BOLD, 16));
        generateReportBtn.addActionListener(e -> {
            SalesManager.ReportPeriod p;
            if (periodCombo.getSelectedIndex() == 0) p = SalesManager.ReportPeriod.TODAY;
            else if (periodCombo.getSelectedIndex() == 1) p = SalesManager.ReportPeriod.WEEK;
            else p = SalesManager.ReportPeriod.MONTH;
            
            String reportText = salesManager.generateReportText(inventory, p);
            // Convert the plain text to HTML so it respects the center alignment and line breaks
            reportLabel.setText("<html><center>" + reportText.replace("\n", "<br>") + "</center></html>");
        });
        
        controlPanel.add(new JLabel("Período: "));
        controlPanel.add(periodCombo);
        controlPanel.add(generateReportBtn);
        
        reportPanel.add(controlPanel, BorderLayout.SOUTH);
        
        // Add tabs to the Main Frame
        tabbedPane.addTab("Vendas", posPanel);
        tabbedPane.addTab("Estoque", inventoryPanel);
        tabbedPane.addTab("Relatórios", reportPanel);
        
        add(tabbedPane);
        
        updateOrderDisplay(); 
    }

    // This method completely redraws the Menu tabs when products are added/removed/changed
    private void refreshMenuTabs() {
        menuTabbedPane.removeAll(); // Clear existing tabs
        Map<String, JPanel> categoryPanels = new HashMap<>();
        
        for (Product product : inventory.getProducts()) {
            String category = product.getCategory();
            
            if (!categoryPanels.containsKey(category)) {
                JPanel newCategoryPanel = new JPanel(new GridLayout(0, 2, 10, 10));
                newCategoryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                categoryPanels.put(category, newCategoryPanel);
                menuTabbedPane.addTab(category, newCategoryPanel);
            }
            
            JButton productButton = new JButton("<html><center>" + product.getName() + "<br>R$" + String.format("%.2f", product.getPrice()) + "</center></html>");
            productButton.setFont(new Font("Arial", Font.BOLD, 18));
            productButton.addActionListener(e -> addProductToOrder(product));
            
            categoryPanels.get(category).add(productButton);
        }
        
        menuTabbedPane.revalidate();
        menuTabbedPane.repaint();
    }

    private void refreshInventoryTable() {
        // Clear all existing rows
        inventoryTableModel.setRowCount(0);
        
        // Fill the model with fresh data from the Inventory
        for (Product p : inventory.getProducts()) {
            Object[] rowData = {
                p.getId(),
                p.getName(),
                String.format("%.2f", p.getPrice()),
                p.getStockQuantity(),
                p.getCategory()
            };
            inventoryTableModel.addRow(rowData);
        }
    }

    private void addProductToOrder(Product product) {
        currentOrder.addItem(product, 1);
        updateOrderDisplay();
    }

    private void updateOrderDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Recibo Java Café ---\n\n");
        
        for (Map.Entry<Product, Integer> entry : currentOrder.getItems().entrySet()) {
            Product p = entry.getKey();
            int qty = entry.getValue();
            double lineTotal = p.getPrice() * qty;
            sb.append(qty).append("x ").append(p.getName())
              .append("   R$").append(String.format("%.2f", lineTotal)).append("\n");
        }
        
        sb.append("\n-------------------------\n");
        sb.append("Subtotal: R$").append(String.format("%.2f", currentOrder.calculateSubtotal())).append("\n");
        sb.append("Imposto (10%): R$").append(String.format("%.2f", currentOrder.calculateTax())).append("\n");
        
        orderTextArea.setText(sb.toString());
        totalLabel.setText("Total: R$" + String.format("%.2f", currentOrder.calculateTotal()));
    }
    
    private void finalizeSale() {
        if (currentOrder.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "O pedido está vazio!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
    
        try {
            // Attempt to reduce stock for all items in the order
            for (Map.Entry<Product, Integer> entry : currentOrder.getItems().entrySet()) {
                Product p = entry.getKey();
                int qty = entry.getValue();
                
                // This line can throw the OutOfStockException!
                inventory.reduceStock(p, qty);
            }
            
            // If it reaches here without error, the sale was successful!
            // Save the record to the CSV file
            salesManager.recordSale(currentOrder);
            
            JOptionPane.showMessageDialog(this, "Venda finalizada no valor de R$" + String.format("%.2f", currentOrder.calculateTotal()) + "!");
            
            // Clear the cart for the next customer
            currentOrder = new Order();
            updateOrderDisplay();
            
            // Refresh the inventory table so the new stock levels are visible!
            refreshInventoryTable();
            
        } catch (OutOfStockException ex) {
            // Catch the custom exception and show an error pop-up
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de Estoque", JOptionPane.ERROR_MESSAGE);
        }
    }
}
