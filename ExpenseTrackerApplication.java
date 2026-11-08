import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * ExpenseTrackerApplication - entry point. Shows the menu, reads and validates
 * user input, and calls ExpenseTracker / Budget methods to do the real work.
 */
public class ExpenseTrackerApplication {

    private static final Scanner scanner = new Scanner(System.in);
    private static final ExpenseTracker tracker = new ExpenseTracker();

    public static void main(String[] args) {
        System.out.println("=======================================");
        System.out.println("     PERSONAL EXPENSE TRACKER");
        System.out.println("=======================================");

        boolean running = true;
        while (running) {
            try {
                showMenu();
                int choice = readInt("Enter your choice: ");
                switch (choice) {
                    case 1:  addTransaction(); break;
                    case 2:  updateTransaction(); break;
                    case 3:  deleteTransaction(); break;
                    case 4:  tracker.displayTransactions(); break;
                    case 5:  searchTransactions(); break;
                    case 6:  tracker.generateFinancialReport(); break;
                    case 7:  tracker.displayCategoryReport(); break;
                    case 8:  setMonthlyBudget(); break;
                    case 9:  tracker.displayBudgetStatus(); break;
                    case 10: monthlySummary(); break;
                    case 11: sortAndDisplay(); break;
                    case 12: tracker.displayExpenseChart(); break;
                    case 13: recurringMenu(); break;
                    case 14: exportCsv(); break;
                    case 15: importCsv(); break;
                    case 0:
                        System.out.println("Thank you for using Personal Expense Tracker. Goodbye!");
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number from the menu (0-15).");
                }
            } catch (NoSuchElementException e) {
                // Input stream closed (for example Ctrl+D or end of piped input)
                System.out.println("\nInput ended. Exiting.");
                running = false;
            }
            if (running) {
                pause();
            }
        }
    }

    // =====================================================================
    //  Menu
    // =====================================================================
    private static void showMenu() {
        System.out.println();
        System.out.println("============== MAIN MENU ==============");
        System.out.println(" 1. Add Income/Expense");
        System.out.println(" 2. Update Transaction");
        System.out.println(" 3. Delete Transaction");
        System.out.println(" 4. Display All Transactions");
        System.out.println(" 5. Search Transactions");
        System.out.println(" 6. Financial Summary");
        System.out.println(" 7. Category-wise Expense Report");
        System.out.println(" 8. Set Monthly Budget");
        System.out.println(" 9. View Monthly Budget Status");
        System.out.println("--------------- Extras ----------------");
        System.out.println("10. Monthly Financial Summary");
        System.out.println("11. Sort and Display Transactions");
        System.out.println("12. Expense Bar Chart");
        System.out.println("13. Recurring Expenses");
        System.out.println("14. Export Transactions to CSV");
        System.out.println("15. Import Transactions from CSV");
        System.out.println(" 0. Exit");
        System.out.println("=======================================");
    }

    /** Waits for Enter so the user can read the output before the menu returns. */
    private static void pause() {
        System.out.print("\nPress Enter to return to the main menu...");
        try {
            scanner.nextLine();
        } catch (NoSuchElementException e) {
            // handled by the main loop on the next read
        }
    }

    // =====================================================================
    //  Menu actions
    // =====================================================================

    private static void addTransaction() {
        System.out.println("\n--- Add Income/Expense ---");
        if (tracker.isFull()) {
            System.out.println("Error: storage is full (maximum " + ExpenseTracker.MAX_TRANSACTIONS
                    + " transactions). Delete a transaction first.");
            return;
        }
        String type = readType();
        double amount = readPositiveAmount("Amount (must be greater than 0): ");
        System.out.println("Suggested categories: Food, Transportation, Rent, Entertainment, Shopping, Salary, Other");
        String category = readNonEmpty("Category: ");
        String description = readNonEmpty("Description: ");
        LocalDate date = readDate("Date (yyyy-MM-dd): ");

        try {
            Transaction t = tracker.addTransaction(type, amount, category, description, date);
            System.out.println("Success: transaction saved with ID " + t.getTransactionId() + ".");
            String notice = tracker.budgetNoticeFor(t);
            if (notice != null) {
                System.out.println(notice);
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void updateTransaction() {
        System.out.println("\n--- Update Transaction ---");
        if (tracker.isEmpty()) {
            System.out.println("No transactions available to update.");
            return;
        }
        int id = readInt("Enter the Transaction ID to update: ");
        Transaction t = tracker.findById(id);
        if (t == null) {
            System.out.println("Error: no transaction found with ID " + id + ".");
            return;
        }
        System.out.println("Current details:");
        t.displayDetails();
        System.out.println("(Press Enter to keep the current value.)");

        double amount = t.getAmount();
        String category = t.getCategory();
        String description = t.getDescription();
        LocalDate date = t.getDate();

        String in = readLine("New amount [" + String.format("%.2f", amount) + "]: ");
        if (!in.isEmpty()) {
            try {
                amount = Double.parseDouble(in);
                if (amount <= 0) {
                    System.out.println("Error: amount must be greater than zero. Update cancelled.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: invalid number. Update cancelled.");
                return;
            }
        }
        in = readLine("New category [" + category + "]: ");
        if (!in.isEmpty()) category = in;
        in = readLine("New description [" + description + "]: ");
        if (!in.isEmpty()) description = in;
        in = readLine("New date yyyy-MM-dd [" + date + "]: ");
        if (!in.isEmpty()) {
            try {
                date = LocalDate.parse(in);
            } catch (DateTimeParseException e) {
                System.out.println("Error: invalid date. Update cancelled.");
                return;
            }
        }

        try {
            tracker.updateTransaction(id, amount, category, description, date);
            System.out.println("Success: transaction " + id + " updated.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage() + " Update cancelled.");
        }
    }

    private static void deleteTransaction() {
        System.out.println("\n--- Delete Transaction ---");
        if (tracker.isEmpty()) {
            System.out.println("No transactions available to delete.");
            return;
        }
        int id = readInt("Enter the Transaction ID to delete: ");
        Transaction t = tracker.findById(id);
        if (t == null) {
            System.out.println("Error: no transaction found with ID " + id + ".");
            return;
        }
        System.out.println("Transaction to delete:");
        t.displayDetails();
        if (readYesNo("Permanently delete this transaction? (y/n): ")) {
            tracker.deleteTransaction(id);
            System.out.println("Success: transaction " + id + " deleted.");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private static void searchTransactions() {
        System.out.println("\n--- Search Transactions ---");
        if (tracker.isEmpty()) {
            System.out.println("No transactions available to search.");
            return;
        }
        System.out.println("1. By Transaction ID");
        System.out.println("2. By Category");
        System.out.println("3. By Type (Income/Expense)");
        System.out.println("4. By Date Range");
        int choice = readInt("Choose a search option: ");
        Transaction[] results;
        switch (choice) {
            case 1:
                results = tracker.searchTransaction(readInt("Transaction ID: "));
                break;
            case 2:
                results = tracker.searchTransaction(readNonEmpty("Category: "));
                break;
            case 3:
                results = tracker.searchByType(readType());
                break;
            case 4:
                LocalDate from = readDate("Start date (yyyy-MM-dd): ");
                LocalDate to = readDate("End date (yyyy-MM-dd): ");
                if (to.isBefore(from)) {
                    System.out.println("Error: end date is before the start date.");
                    return;
                }
                results = tracker.searchByDateRange(from, to);
                break;
            default:
                System.out.println("Invalid search option.");
                return;
        }
        if (results.length == 0) {
            System.out.println("No matching transactions found.");
        } else {
            tracker.displayTransactions(results);
        }
    }

    private static void setMonthlyBudget() {
        System.out.println("\n--- Set Monthly Budget ---");
        int month = readIntInRange("Month (1-12): ", 1, 12);
        int year = readIntInRange("Year (e.g. 2026): ", 1900, 2100);
        double amount = readPositiveAmount("Budget amount: ");
        try {
            tracker.getBudget().setBudget(amount, month, year);
            System.out.println("Success: budget of " + Transaction.formatMoney(amount) + " set for "
                    + java.time.Month.of(month) + " " + year + ".");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void monthlySummary() {
        System.out.println("\n--- Monthly Financial Summary ---");
        if (tracker.isEmpty()) {
            System.out.println("No transactions available.");
            return;
        }
        int month = readIntInRange("Month (1-12): ", 1, 12);
        int year = readIntInRange("Year (e.g. 2026): ", 1900, 2100);
        tracker.generateMonthlyReport(month, year);
    }

    private static void sortAndDisplay() {
        System.out.println("\n--- Sort and Display ---");
        if (tracker.isEmpty()) {
            System.out.println("No transactions available.");
            return;
        }
        System.out.println("1. Date (oldest first)");
        System.out.println("2. Date (newest first)");
        System.out.println("3. Amount (lowest first)");
        System.out.println("4. Amount (highest first)");
        int choice = readIntInRange("Choose sort order: ", 1, 4);
        Transaction[] sorted;
        switch (choice) {
            case 1:  sorted = tracker.getSortedCopy("date", true); break;
            case 2:  sorted = tracker.getSortedCopy("date", false); break;
            case 3:  sorted = tracker.getSortedCopy("amount", true); break;
            default: sorted = tracker.getSortedCopy("amount", false); break;
        }
        tracker.displayTransactions(sorted);
    }

    private static void recurringMenu() {
        System.out.println("\n--- Recurring Expenses ---");
        System.out.println("1. Define a recurring expense");
        System.out.println("2. View recurring expenses");
        System.out.println("3. Generate transactions for a month");
        int choice = readInt("Choose an option: ");
        try {
            switch (choice) {
                case 1:
                    String category = readNonEmpty("Category: ");
                    String description = readNonEmpty("Description (e.g. Monthly rent): ");
                    double amount = readPositiveAmount("Amount: ");
                    int day = readIntInRange("Day of month (1-31): ", 1, 31);
                    tracker.addRecurring(category, description, amount, day);
                    System.out.println("Success: recurring expense saved.");
                    break;
                case 2:
                    tracker.displayRecurring();
                    break;
                case 3:
                    if (tracker.getRecurringCount() == 0) {
                        System.out.println("No recurring expenses defined yet.");
                        break;
                    }
                    int month = readIntInRange("Month (1-12): ", 1, 12);
                    int year = readIntInRange("Year (e.g. 2026): ", 1900, 2100);
                    int created = tracker.generateRecurringTransactions(month, year);
                    System.out.println("Success: " + created + " transaction(s) generated"
                            + (created == 0 ? " (already generated for this month)." : "."));
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void exportCsv() {
        System.out.println("\n--- Export to CSV ---");
        if (tracker.isEmpty()) {
            System.out.println("No transactions to export.");
            return;
        }
        String file = readNonEmpty("File name (e.g. export.csv): ");
        try {
            int n = tracker.exportToCsv(file);
            System.out.println("Success: " + n + " transaction(s) exported to " + file + ".");
        } catch (IOException e) {
            System.out.println("Error: could not write the file (" + e.getMessage() + ").");
        }
    }

    private static void importCsv() {
        System.out.println("\n--- Import from CSV ---");
        String file = readNonEmpty("File name (e.g. sample_transactions.csv): ");
        try {
            int[] result = tracker.importFromCsv(file);
            System.out.println("Import finished: " + result[0] + " imported, " + result[1]
                    + " invalid or skipped.");
        } catch (FileNotFoundException e) {
            System.out.println("Error: file not found: " + file);
        } catch (IOException e) {
            System.out.println("Error: could not read the file (" + e.getMessage() + ").");
        }
    }

    // =====================================================================
    //  Input helpers - every one loops until the input is valid
    // =====================================================================

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int readInt(String prompt) {
        while (true) {
            String in = readLine(prompt);
            try {
                return Integer.parseInt(in);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a whole number.");
            }
        }
    }

    private static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.println("Please enter a number between " + min + " and " + max + ".");
        }
    }

    private static double readPositiveAmount(String prompt) {
        while (true) {
            String in = readLine(prompt);
            try {
                double value = Double.parseDouble(in);
                if (Double.isNaN(value) || Double.isInfinite(value)) {
                    System.out.println("Invalid number. Please try again.");
                } else if (value <= 0) {
                    System.out.println("Amount must be greater than zero.");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter an amount such as 25.50.");
            }
        }
    }

    private static String readNonEmpty(String prompt) {
        while (true) {
            String in = readLine(prompt);
            if (!in.isEmpty()) {
                return in;
            }
            System.out.println("This field cannot be empty.");
        }
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            String in = readLine(prompt);
            try {
                return LocalDate.parse(in);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date. Use the format yyyy-MM-dd (for example 2026-11-07).");
            }
        }
    }

    private static String readType() {
        while (true) {
            System.out.println("Type: 1) Income  2) Expense");
            int choice = readInt("Select type: ");
            if (choice == 1) return Transaction.INCOME;
            if (choice == 2) return Transaction.EXPENSE;
            System.out.println("Please choose 1 or 2.");
        }
    }

    private static boolean readYesNo(String prompt) {
        while (true) {
            String in = readLine(prompt).toLowerCase();
            if (in.equals("y") || in.equals("yes")) return true;
            if (in.equals("n") || in.equals("no")) return false;
            System.out.println("Please answer y or n.");
        }
    }
}
