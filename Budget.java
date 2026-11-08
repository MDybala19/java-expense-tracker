/**
 * Budget - manages the spending budget for one month.
 * The amount already spent is supplied by the caller (ExpenseTracker calculates it
 * dynamically from the stored transactions).
 */
public class Budget {

    /** Percentage of the budget at which a warning is shown. */
    public static final double WARNING_THRESHOLD = 80.0;

    private double monthlyBudget;
    private int month;
    private int year;
    private boolean set;

    public Budget() {
        this.set = false;
    }

    /** Sets (or replaces) the budget for the given month and year. */
    public void setBudget(double monthlyBudget, int month, int year) {
        if (Double.isNaN(monthlyBudget) || Double.isInfinite(monthlyBudget) || monthlyBudget <= 0) {
            throw new IllegalArgumentException("Budget must be greater than zero.");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12.");
        }
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("Year must be between 1900 and 2100.");
        }
        this.monthlyBudget = monthlyBudget;
        this.month = month;
        this.year = year;
        this.set = true;
    }

    public boolean isSet() { return set; }
    public double getMonthlyBudget() { return monthlyBudget; }
    public int getMonth() { return month; }
    public int getYear() { return year; }

    /** Budget minus spending. Negative when the budget has been exceeded. */
    public double calculateRemainingBudget(double totalSpent) {
        return monthlyBudget - totalSpent;
    }

    /** Percentage of the budget already spent. */
    public double calculateBudgetPercentage(double totalSpent) {
        if (!set || monthlyBudget == 0) {
            return 0;
        }
        return (totalSpent / monthlyBudget) * 100.0;
    }

    /** Returns a status message: OK, 80% warning, or exceeded alert. */
    public String checkBudgetStatus(double totalSpent) {
        double pct = calculateBudgetPercentage(totalSpent);
        if (pct > 100.0) {
            return "ALERT: Monthly budget EXCEEDED by "
                    + Transaction.formatMoney(totalSpent - monthlyBudget) + "!";
        } else if (pct >= WARNING_THRESHOLD) {
            return String.format("WARNING: You have used %.1f%% of your monthly budget.", pct);
        }
        return "OK: You are within your budget.";
    }
}
