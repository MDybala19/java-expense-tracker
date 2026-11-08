import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/**
 * ExpenseTracker - manages the collection of transactions.
 * Transactions are stored in a plain array (no ArrayList / collections) with a
 * fixed capacity of 100 and a counter for the number currently stored.
 * Every total and report is calculated dynamically from the stored array.
 */
public class ExpenseTracker {

    public static final int MAX_TRANSACTIONS = 100;
    public static final int MAX_RECURRING = 20;

    private Transaction[] transactions = new Transaction[MAX_TRANSACTIONS];
    private int transactionCount = 0;
    private int nextId = 1001; // IDs are never reused, so they stay unique

    private RecurringTransaction[] recurring = new RecurringTransaction[MAX_RECURRING];
    private int recurringCount = 0;

    private Budget budget = new Budget();

    // =====================================================================
    //  Basic accessors
    // =====================================================================
    public int getTransactionCount() { return transactionCount; }
    public boolean isFull() { return transactionCount >= MAX_TRANSACTIONS; }
    public boolean isEmpty() { return transactionCount == 0; }
    public Budget getBudget() { return budget; }
    public int getRecurringCount() { return recurringCount; }

    // =====================================================================
    //  A. Add
    // =====================================================================

    /**
     * Adds a new transaction with an automatically generated ID.
     * @throws IllegalStateException if the array is full
     * @throws IllegalArgumentException if any field is invalid
     */
    public Transaction addTransaction(String type, double amount, String category,
                                      String description, LocalDate date) {
        if (isFull()) {
            throw new IllegalStateException("Storage is full (maximum " + MAX_TRANSACTIONS + " transactions).");
        }
        Transaction t = new Transaction(nextId, type, amount, category, description, date);
        transactions[transactionCount] = t; // insert at the end
        transactionCount++;
        nextId++;
        return t;
    }

    // =====================================================================
    //  B. Update / Delete
    // =====================================================================

    /** Returns the array index of the transaction with this ID, or -1 if absent. */
    private int findIndexById(int id) {
        for (int i = 0; i < transactionCount; i++) {
            if (transactions[i].getTransactionId() == id) {
                return i;
            }
        }
        return -1;
    }

    /** Looks up one transaction by ID (null if not found). */
    public Transaction findById(int id) {
        int index = findIndexById(id);
        return index == -1 ? null : transactions[index];
    }

    /**
     * Updates amount, category, description and date of an existing transaction.
     * The new values are validated first on a scratch copy, so the stored record
     * is left untouched if any value is invalid.
     * @return false if the ID does not exist
     */
    public boolean updateTransaction(int id, double amount, String category,
                                     String description, LocalDate date) {
        Transaction t = findById(id);
        if (t == null) {
            return false;
        }
        // Validate everything before saving (constructor throws if invalid)
        new Transaction(id, t.getTransactionType(), amount, category, description, date);
        t.setAmount(amount);
        t.setCategory(category);
        t.setDescription(description);
        t.setDate(date);
        return true;
    }

    /**
     * Deletes the transaction with this ID and shifts later elements left
     * so the array has no gaps.
     * @return false if the ID does not exist
     */
    public boolean deleteTransaction(int id) {
        int index = findIndexById(id);
        if (index == -1) {
            return false;
        }
        for (int i = index; i < transactionCount - 1; i++) {
            transactions[i] = transactions[i + 1];
        }
        transactions[transactionCount - 1] = null;
        transactionCount--;
        return true;
    }

    // =====================================================================
    //  C. Display / Search / Sort
    // =====================================================================

    /** Returns a right-sized copy of the stored transactions. */
    private Transaction[] copyOfStored() {
        Transaction[] copy = new Transaction[transactionCount];
        for (int i = 0; i < transactionCount; i++) {
            copy[i] = transactions[i];
        }
        return copy;
    }

    /** Displays all stored transactions in a table. */
    public void displayTransactions() {
        displayTransactions(copyOfStored());
    }

    /** Displays the given transactions in a table (or a message if none). */
    public void displayTransactions(Transaction[] list) {
        if (list == null || list.length == 0) {
            System.out.println("No transactions to display.");
            return;
        }
        System.out.println(Transaction.tableHeader());
        for (int i = 0; i < list.length; i++) {
            System.out.println(list[i].toTableRow());
        }
        System.out.println("(" + list.length + " transaction" + (list.length == 1 ? "" : "s") + ")");
    }

    /** Search by ID; returns an array of 0 or 1 matches. */
    public Transaction[] searchTransaction(int id) {
        Transaction t = findById(id);
        return t == null ? new Transaction[0] : new Transaction[] { t };
    }

    /** Search by category (case-insensitive exact match). */
    public Transaction[] searchTransaction(String category) {
        Transaction[] temp = new Transaction[transactionCount];
        int n = 0;
        for (int i = 0; i < transactionCount; i++) {
            if (transactions[i].getCategory().equalsIgnoreCase(category.trim())) {
                temp[n++] = transactions[i];
            }
        }
        return trim(temp, n);
    }

    /** Search by type (Income or Expense). */
    public Transaction[] searchByType(String type) {
        Transaction[] temp = new Transaction[transactionCount];
        int n = 0;
        for (int i = 0; i < transactionCount; i++) {
            if (transactions[i].getTransactionType().equalsIgnoreCase(type.trim())) {
                temp[n++] = transactions[i];
            }
        }
        return trim(temp, n);
    }

    /** Search by inclusive date range. */
    public Transaction[] searchByDateRange(LocalDate from, LocalDate to) {
        Transaction[] temp = new Transaction[transactionCount];
        int n = 0;
        for (int i = 0; i < transactionCount; i++) {
            LocalDate d = transactions[i].getDate();
            if (!d.isBefore(from) && !d.isAfter(to)) {
                temp[n++] = transactions[i];
            }
        }
        return trim(temp, n);
    }

    private Transaction[] trim(Transaction[] source, int n) {
        Transaction[] result = new Transaction[n];
        for (int i = 0; i < n; i++) {
            result[i] = source[i];
        }
        return result;
    }

    /**
     * Returns a sorted copy using insertion sort (the stored order is unchanged).
     * @param field "date" or "amount"
     * @param ascending true for low-to-high / oldest-first
     */
    public Transaction[] getSortedCopy(String field, boolean ascending) {
        Transaction[] sorted = copyOfStored();
        for (int i = 1; i < sorted.length; i++) {
            Transaction key = sorted[i];
            int j = i - 1;
            while (j >= 0 && compare(sorted[j], key, field, ascending) > 0) {
                sorted[j + 1] = sorted[j];
                j--;
            }
            sorted[j + 1] = key;
        }
        return sorted;
    }

    private int compare(Transaction a, Transaction b, String field, boolean ascending) {
        int result;
        if (field.equals("amount")) {
            result = Double.compare(a.getAmount(), b.getAmount());
        } else {
            result = a.getDate().compareTo(b.getDate());
        }
        return ascending ? result : -result;
    }

    // =====================================================================
    //  D. Calculations and reports (always computed from stored data)
    // =====================================================================

    public double calculateTotalIncome() {
        double total = 0;
        for (int i = 0; i < transactionCount; i++) {
            if (transactions[i].isIncome()) {
                total += transactions[i].getAmount();
            }
        }
        return total;
    }

    public double calculateTotalExpenses() {
        double total = 0;
        for (int i = 0; i < transactionCount; i++) {
            if (transactions[i].isExpense()) {
                total += transactions[i].getAmount();
            }
        }
        return total;
    }

    /** Total income for one month. */
    public double calculateMonthlyIncome(int month, int year) {
        double total = 0;
        for (int i = 0; i < transactionCount; i++) {
            if (transactions[i].isIncome() && inMonth(transactions[i], month, year)) {
                total += transactions[i].getAmount();
            }
        }
        return total;
    }

    /** Total expenses for one month. */
    public double calculateMonthlyExpenses(int month, int year) {
        double total = 0;
        for (int i = 0; i < transactionCount; i++) {
            if (transactions[i].isExpense() && inMonth(transactions[i], month, year)) {
                total += transactions[i].getAmount();
            }
        }
        return total;
    }

    private boolean inMonth(Transaction t, int month, int year) {
        return t.getDate().getMonthValue() == month && t.getDate().getYear() == year;
    }

    public double calculateBalance() {
        return calculateTotalIncome() - calculateTotalExpenses();
    }

    /** Highest single expense (null when there are no expenses). */
    public Transaction findHighestExpense() {
        Transaction highest = null;
        for (int i = 0; i < transactionCount; i++) {
            if (transactions[i].isExpense()
                    && (highest == null || transactions[i].getAmount() > highest.getAmount())) {
                highest = transactions[i];
            }
        }
        return highest;
    }

    /** Average expense amount (0 when there are no expenses). */
    public double calculateAverageExpense() {
        double total = 0;
        int count = 0;
        for (int i = 0; i < transactionCount; i++) {
            if (transactions[i].isExpense()) {
                total += transactions[i].getAmount();
                count++;
            }
        }
        return count == 0 ? 0 : total / count;
    }

    /**
     * Builds the expense-by-category totals from the stored transactions.
     * Pass month = 0 to include all dates.
     */
    private CategoryTotals expensesByCategory(int month, int year) {
        CategoryTotals ct = new CategoryTotals(MAX_TRANSACTIONS);
        for (int i = 0; i < transactionCount; i++) {
            Transaction t = transactions[i];
            if (t.isExpense() && (month == 0 || inMonth(t, month, year))) {
                ct.add(t.getCategory(), t.getAmount());
            }
        }
        return ct;
    }

    /** Small helper holding category names and their totals in parallel arrays. */
    private static class CategoryTotals {
        String[] names;
        double[] totals;
        int size = 0;

        CategoryTotals(int capacity) {
            names = new String[capacity];
            totals = new double[capacity];
        }

        void add(String name, double amount) {
            for (int i = 0; i < size; i++) {
                if (names[i].equalsIgnoreCase(name)) {
                    totals[i] += amount;
                    return;
                }
            }
            names[size] = name;
            totals[size] = amount;
            size++;
        }

        double max() {
            double m = 0;
            for (int i = 0; i < size; i++) {
                if (totals[i] > m) m = totals[i];
            }
            return m;
        }
    }

    /** Prints total expenses grouped by category. */
    public void displayCategoryReport() {
        CategoryTotals ct = expensesByCategory(0, 0);
        if (ct.size == 0) {
            System.out.println("No expenses recorded yet.");
            return;
        }
        printCategoryTotals(ct);
    }

    private void printCategoryTotals(CategoryTotals ct) {
        System.out.println(String.format("%-18s %14s", "Category", "Total Expenses"));
        System.out.println("-".repeat(33));
        for (int i = 0; i < ct.size; i++) {
            System.out.println(String.format("%-18s %14s", ct.names[i], Transaction.formatMoney(ct.totals[i])));
        }
    }

    /** Overall financial report (all dates). */
    public void generateFinancialReport() {
        if (isEmpty()) {
            System.out.println("No transactions available. Add some first.");
            return;
        }
        System.out.println("========== FINANCIAL SUMMARY ==========");
        System.out.println("Total Income    : " + Transaction.formatMoney(calculateTotalIncome()));
        System.out.println("Total Expenses  : " + Transaction.formatMoney(calculateTotalExpenses()));
        System.out.println("Current Balance : " + Transaction.formatMoney(calculateBalance()));
        System.out.println("Average Expense : " + Transaction.formatMoney(calculateAverageExpense()));
        Transaction highest = findHighestExpense();
        if (highest == null) {
            System.out.println("Highest Expense : none recorded");
        } else {
            System.out.println("Highest Expense : " + Transaction.formatMoney(highest.getAmount())
                    + " (" + highest.getCategory() + " - " + highest.getDescription()
                    + ", " + highest.getDate() + ")");
        }
        System.out.println();
        System.out.println("Expenses by category:");
        CategoryTotals ct = expensesByCategory(0, 0);
        if (ct.size == 0) {
            System.out.println("  (no expenses)");
        } else {
            printCategoryTotals(ct);
        }
    }

    /** Financial summary for one month and year. */
    public void generateMonthlyReport(int month, int year) {
        String title = java.time.Month.of(month).toString() + " " + year;
        System.out.println("===== MONTHLY SUMMARY: " + title + " =====");
        int count = 0;
        for (int i = 0; i < transactionCount; i++) {
            if (inMonth(transactions[i], month, year)) count++;
        }
        if (count == 0) {
            System.out.println("No transactions found for this month.");
            return;
        }
        double income = calculateMonthlyIncome(month, year);
        double expenses = calculateMonthlyExpenses(month, year);
        System.out.println("Transactions    : " + count);
        System.out.println("Total Income    : " + Transaction.formatMoney(income));
        System.out.println("Total Expenses  : " + Transaction.formatMoney(expenses));
        System.out.println("Net Balance     : " + Transaction.formatMoney(income - expenses));
        System.out.println();
        CategoryTotals ct = expensesByCategory(month, year);
        if (ct.size == 0) {
            System.out.println("No expenses this month.");
        } else {
            System.out.println("Expenses by category:");
            printCategoryTotals(ct);
        }
    }

    /** Text bar chart of expenses by category (bonus B). One '#' per 10 dollars, min 1. */
    public void displayExpenseChart() {
        CategoryTotals ct = expensesByCategory(0, 0);
        if (ct.size == 0) {
            System.out.println("No expenses recorded yet.");
            return;
        }
        double max = ct.max();
        double scale = max > 50 ? max / 40.0 : 1.0; // keep the longest bar at 40 characters
        System.out.println("Expenses by category (each # = " + Transaction.formatMoney(scale) + ")");
        for (int i = 0; i < ct.size; i++) {
            int bars = (int) Math.max(1, Math.round(ct.totals[i] / scale));
            System.out.println(String.format("%-15s: %s (%s)", ct.names[i], "#".repeat(bars),
                    Transaction.formatMoney(ct.totals[i])));
        }
    }

    // =====================================================================
    //  E. Budget
    // =====================================================================

    /** Prints the budget status using spending calculated for the budget's month. */
    public void displayBudgetStatus() {
        if (!budget.isSet()) {
            System.out.println("No monthly budget has been set yet. Use option 8 first.");
            return;
        }
        double spent = calculateMonthlyExpenses(budget.getMonth(), budget.getYear());
        System.out.println("===== BUDGET STATUS: "
                + java.time.Month.of(budget.getMonth()) + " " + budget.getYear() + " =====");
        System.out.println("Monthly Budget   : " + Transaction.formatMoney(budget.getMonthlyBudget()));
        System.out.println("Spent This Month : " + Transaction.formatMoney(spent));
        System.out.println("Remaining Budget : " + Transaction.formatMoney(budget.calculateRemainingBudget(spent)));
        System.out.println(String.format("Percent Spent    : %.1f%%", budget.calculateBudgetPercentage(spent)));
        System.out.println(budget.checkBudgetStatus(spent));
    }

    /**
     * Shows a budget warning right after an expense is added, if that expense's month
     * has a budget and has reached 80% or been exceeded.
     */
    public String budgetNoticeFor(Transaction t) {
        if (!budget.isSet() || !t.isExpense()
                || !inMonth(t, budget.getMonth(), budget.getYear())) {
            return null;
        }
        double spent = calculateMonthlyExpenses(budget.getMonth(), budget.getYear());
        double pct = budget.calculateBudgetPercentage(spent);
        return pct >= Budget.WARNING_THRESHOLD ? budget.checkBudgetStatus(spent) : null;
    }

    // =====================================================================
    //  Bonus A: CSV export / import
    // =====================================================================

    /** Writes all transactions to a CSV file. Returns the number written. */
    public int exportToCsv(String fileName) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(fileName))) {
            out.println("TransactionID,Type,Amount,Category,Description,Date");
            for (int i = 0; i < transactionCount; i++) {
                out.println(transactions[i].toCsvLine());
            }
        }
        return transactionCount;
    }

    /**
     * Imports transactions from a CSV file. Invalid lines are skipped and counted.
     * Imported rows get new unique IDs. Returns {imported, skipped}.
     */
    public int[] importFromCsv(String fileName) throws IOException {
        int imported = 0;
        int skipped = 0;
        try (BufferedReader in = new BufferedReader(new FileReader(fileName))) {
            String line = in.readLine(); // header line
            while ((line = in.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                if (isFull()) {
                    skipped++;
                    continue;
                }
                String[] p = line.split(",", -1);
                try {
                    if (p.length != 6) {
                        throw new IllegalArgumentException("wrong number of columns");
                    }
                    double amount = Double.parseDouble(p[2].trim());
                    LocalDate date = LocalDate.parse(p[5].trim());
                    addTransaction(p[1].trim(), amount, p[3], p[4], date);
                    imported++;
                } catch (IllegalArgumentException | DateTimeParseException e) {
                    skipped++; // covers NumberFormatException too
                }
            }
        }
        return new int[] { imported, skipped };
    }

    // =====================================================================
    //  Bonus C: Recurring transactions
    // =====================================================================

    /** Defines a new recurring monthly expense. */
    public void addRecurring(String category, String description, double amount, int dayOfMonth) {
        if (recurringCount >= MAX_RECURRING) {
            throw new IllegalStateException("Recurring list is full (maximum " + MAX_RECURRING + ").");
        }
        recurring[recurringCount++] = new RecurringTransaction(category, description, amount, dayOfMonth);
    }

    public void displayRecurring() {
        if (recurringCount == 0) {
            System.out.println("No recurring expenses defined.");
            return;
        }
        System.out.println(String.format("%-3s %-15s %-30s %14s", "#", "Category", "Description", "Amount"));
        System.out.println("-".repeat(70));
        for (int i = 0; i < recurringCount; i++) {
            System.out.println(String.format("%-3d %s", i + 1, recurring[i]));
        }
    }

    /**
     * Creates one expense transaction per recurring item for the given month.
     * Items already generated for that month (same category, description, amount, date)
     * are skipped so the option can be run twice safely.
     * @return number of transactions created
     */
    public int generateRecurringTransactions(int month, int year) {
        int created = 0;
        YearMonth ym = YearMonth.of(year, month);
        for (int i = 0; i < recurringCount; i++) {
            RecurringTransaction r = recurring[i];
            int day = Math.min(r.getDayOfMonth(), ym.lengthOfMonth());
            LocalDate date = ym.atDay(day);
            if (alreadyExists(r, date)) {
                continue;
            }
            if (isFull()) {
                throw new IllegalStateException("Storage is full after creating " + created + " transaction(s).");
            }
            addTransaction(Transaction.EXPENSE, r.getAmount(), r.getCategory(), r.getDescription(), date);
            created++;
        }
        return created;
    }

    private boolean alreadyExists(RecurringTransaction r, LocalDate date) {
        for (int i = 0; i < transactionCount; i++) {
            Transaction t = transactions[i];
            if (t.isExpense() && t.getDate().equals(date)
                    && t.getCategory().equalsIgnoreCase(r.getCategory())
                    && t.getDescription().equalsIgnoreCase(r.getDescription())
                    && Math.abs(t.getAmount() - r.getAmount()) < 0.005) {
                return true;
            }
        }
        return false;
    }
}
