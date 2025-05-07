import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ReportGenerator {

    // Product class to store product information
    static class Product {
        private int productID;
        private String productName;
        private double price;

        public Product(int productID, String productName, double price) {
            this.productID = productID;
            this.productName = productName;
            this.price = price;
        }

        // Getter methods
        public int getProductID() {
            return productID;
        }

        public String getProductName() {
            return productName;
        }

        public double getPrice() {
            return price;
        }
    }

    // Thread task to process each order file
    static class OrderProcessor implements Runnable {
        private final String filePath;
        private double totalCost;
        private int totalAmount;
        private int totalDiscount;
        private int totalSales;
        private Product mostExpensiveProduct;
        private double highestCostAfterDiscount;

        public OrderProcessor(String filePath) {
            this.filePath = filePath;
            this.totalCost = 0;
            this.totalAmount = 0;
            this.totalDiscount = 0;
            this.totalSales = 0;
            this.highestCostAfterDiscount = 0;
            this.mostExpensiveProduct = null;
        }

        @Override
        public void run() {
            try {
                File orderFile = new File(filePath);
                Scanner scanner = new Scanner(orderFile);

                System.out.println("Processing file: " + orderFile.getName());

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] parts = line.split(",");

                    // Parse order details
                    int productId = Integer.parseInt(parts[0]);
                    int amount = Integer.parseInt(parts[1]);
                    int discount = Integer.parseInt(parts[2]);

                    // Get product info
                    Product product = productCatalog[productId];

                    // Calculate cost after discount
                    double cost = amount * product.getPrice() * (100 - discount) / 100;

                    // Update statistics
                    totalCost += cost;
                    totalAmount += amount;
                    totalDiscount += discount;
                    totalSales++;

                    // Track highest cost
                    if (cost > highestCostAfterDiscount) {
                        highestCostAfterDiscount = cost;
                    }

                    // Track most expensive product
                    if (mostExpensiveProduct == null ||
                            product.getPrice() > mostExpensiveProduct.getPrice()) {
                        mostExpensiveProduct = product;
                    }
                }
                scanner.close();
            } catch (FileNotFoundException e) {
                System.out.println("Error: Could not find file " + filePath);
            } catch (Exception e) {
                System.out.println("Error processing file: " + e.getMessage());
            }
        }

        public void generateReport() {
            System.out.println("\n=====================================");
            System.out.println("Report for: " + new File(filePath).getName());
            System.out.printf("Total Cost: $%,.2f\n", totalCost);
            System.out.println("Total Items Sold: " + totalAmount);
            System.out.println("Total Discount Applied: " + totalDiscount);
            System.out.printf("Highest Transaction: $%,.2f\n", highestCostAfterDiscount);
            System.out.println("Most Expensive Product: " +
                    (mostExpensiveProduct != null ? mostExpensiveProduct.getProductName() : "N/A"));
            System.out.println("Total Sales Transactions: " + totalSales);
            System.out.println("=====================================\n");
        }
    }

    // File paths and product catalog
    private static final String[] ORDER_FILES = {
            "src\\main\\resources\\2021_order_details.txt",
            "src\\main\\resources\\2022_order_details.txt",
            "src\\main\\resources\\2023_order_details.txt",
            "src\\main\\resources\\2024_order_details.txt"
    };

    private static Product[] productCatalog = new Product[10];

    // Load product data from file
    public static void loadProducts() {
        File productFile = new File("src\\main\\resources\\Products.txt");
        try {
            Scanner scanner = new Scanner(productFile);
            System.out.println("Loading product catalog...");

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");

                int id = Integer.parseInt(parts[0]);
                String name = parts[1];
                double price = Double.parseDouble(parts[2]);

                productCatalog[id] = new Product(id, name, price);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error: Product catalog file not found!");
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        // Load product data first
        loadProducts();

        // Create tasks and threads
        OrderProcessor[] processors = new OrderProcessor[ORDER_FILES.length];
        Thread[] threads = new Thread[ORDER_FILES.length];

        System.out.println("\nStarting order processing...");

        // Start processing each file in separate thread
        for (int i = 0; i < ORDER_FILES.length; i++) {
            processors[i] = new OrderProcessor(ORDER_FILES[i]);
            threads[i] = new Thread(processors[i]);
            threads[i].start();

            // Small delay between thread starts
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println("Thread startup interrupted");
            }
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println("Error waiting for thread completion");
            }
        }

        // Generate all reports
        System.out.println("\nGenerating reports...");
        for (OrderProcessor processor : processors) {
            processor.generateReport();
        }

        System.out.println("All reports generated successfully!");
    }
}