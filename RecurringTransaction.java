/**
 * RecurringTransaction - template for an expense that repeats every month
 * (rent, subscriptions, utility bills). Bonus feature C.
 */
public class RecurringTransaction {

    private String category;
    private String description;
    private double amount;
    private int dayOfMonth;

    public RecurringTransaction(String category, String description, double amount, int dayOfMonth) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty.");
        }
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        if (dayOfMonth < 1 || dayOfMonth > 31) {
            throw new IllegalArgumentException("Day of month must be between 1 and 31.");
        }
        this.category = category.trim();
        this.description = description.trim();
        this.amount = amount;
        this.dayOfMonth = dayOfMonth;
    }

    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public int getDayOfMonth() { return dayOfMonth; }

    @Override
    public String toString() {
        return String.format("%-15s %-30s %14s  day %d", category, description,
                Transaction.formatMoney(amount), dayOfMonth);
    }
}
