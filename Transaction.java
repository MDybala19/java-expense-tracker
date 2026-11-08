import java.time.LocalDate;

/**
 * Transaction - stores the information for one income or expense record.
 * Setters validate their input so an invalid Transaction can never be stored.
 */
public final class Transaction {

    public static final String INCOME = "Income";
    public static final String EXPENSE = "Expense";

    private int transactionId;
    private String transactionType;
    private double amount;
    private String category;
    private String description;
    private LocalDate date;

    /** Creates a validated transaction. Throws IllegalArgumentException on bad data. */
    public Transaction(int transactionId, String transactionType, double amount,
                       String category, String description, LocalDate date) {
        this.transactionId = transactionId;
        setTransactionType(transactionType);
        setAmount(amount);
        setCategory(category);
        setDescription(description);
        setDate(date);
    }

    // ---------- Getters ----------
    public int getTransactionId() { return transactionId; }
    public String getTransactionType() { return transactionType; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }

    // ---------- Setters (with validation) ----------
    public void setTransactionId(int transactionId) {
        if (transactionId <= 0) {
            throw new IllegalArgumentException("Transaction ID must be positive.");
        }
        this.transactionId = transactionId;
    }

    public void setTransactionType(String transactionType) {
        if (transactionType != null && transactionType.trim().equalsIgnoreCase(INCOME)) {
            this.transactionType = INCOME;
        } else if (transactionType != null && transactionType.trim().equalsIgnoreCase(EXPENSE)) {
            this.transactionType = EXPENSE;
        } else {
            throw new IllegalArgumentException("Transaction type must be Income or Expense.");
        }
    }

    public void setAmount(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        this.amount = amount;
    }

    public void setCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty.");
        }
        this.category = category.trim();
    }

    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty.");
        }
        this.description = description.trim();
    }

    public void setDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }
        this.date = date;
    }

    // ---------- Helpers ----------
    public boolean isIncome() { return INCOME.equals(transactionType); }
    public boolean isExpense() { return EXPENSE.equals(transactionType); }

    /** Formats a money value the same way everywhere in the program. */
    public static String formatMoney(double value) {
        String text = String.format("$%,.2f", Math.abs(value));
        return value < -0.004 ? "-" + text : text;
    }

    /** Column header line for table output. */
    public static String tableHeader() {
        String header = String.format("%-6s %-9s %14s %-15s %-30s %-10s",
                "ID", "Type", "Amount", "Category", "Description", "Date");
        return header + "\n" + "-".repeat(header.length());
    }

    /** One formatted table row for this transaction. */
    public String toTableRow() {
        String desc = description.length() > 30 ? description.substring(0, 27) + "..." : description;
        String cat = category.length() > 15 ? category.substring(0, 12) + "..." : category;
        return String.format("%-6d %-9s %14s %-15s %-30s %-10s",
                transactionId, transactionType, formatMoney(amount), cat, desc, date);
    }

    /** Prints the full details of this transaction (used before delete/update). */
    public void displayDetails() {
        System.out.println("  Transaction ID : " + transactionId);
        System.out.println("  Type           : " + transactionType);
        System.out.println("  Amount         : " + formatMoney(amount));
        System.out.println("  Category       : " + category);
        System.out.println("  Description    : " + description);
        System.out.println("  Date           : " + date);
    }

    /** CSV line for export. Commas inside text are replaced so the format stays simple. */
    public String toCsvLine() {
        return transactionId + "," + transactionType + "," + String.format("%.2f", amount) + ","
                + category.replace(',', ';') + "," + description.replace(',', ';') + "," + date;
    }

    @Override
    public String toString() {
        return toTableRow();
    }
}
