# Personal Expense Tracker (Java Console Application)

A menu-driven console application for recording, managing, analyzing and budgeting personal income and expenses. Transactions are stored in a plain **array of `Transaction` objects** (capacity 100); no `ArrayList` or other collection classes are used for storage.

## Files

| File | Purpose |
|------|---------|
| `Transaction.java` | One income/expense record (validated setters, table/detail display, CSV line) |
| `ExpenseTracker.java` | Manages the transaction array: add, update, delete, search, sort, totals, reports, CSV, recurring |
| `Budget.java` | Monthly budget: remaining amount, percentage spent, 80% warning, exceeded alert |
| `RecurringTransaction.java` | Template for a monthly recurring expense (bonus) |
| `ExpenseTrackerApplication.java` | `main()`, menu, input reading and validation |
| `sample_transactions.csv` | Sample dataset with 20 transactions (Sept - Nov 2026) |
| `CLASS_EXPLANATION.md` | Brief explanation of the classes and methods |

## Requirements

- Java JDK 11 or newer (developed and tested with a recent JDK)
- Check with: `java -version` and `javac -version`

## How to compile

Open a terminal in the project folder (the one containing the `.java` files) and run:

```
javac *.java
```

This creates the `.class` files in the same folder. (To keep them separate, use `mkdir out` then `javac -d out *.java`.)

## How to run

```
java ExpenseTrackerApplication
```

If you compiled into `out`:

```
java -cp out ExpenseTrackerApplication
```

### Loading the sample dataset

Run the program, choose **15. Import Transactions from CSV**, and type `sample_transactions.csv`. Run the program from the folder that contains the file, or give the full path. The 20 sample transactions are loaded and can be used with every other menu option.

## Menu

```
 1. Add Income/Expense              10. Monthly Financial Summary
 2. Update Transaction              11. Sort and Display Transactions
 3. Delete Transaction              12. Expense Bar Chart
 4. Display All Transactions        13. Recurring Expenses
 5. Search Transactions             14. Export Transactions to CSV
 6. Financial Summary               15. Import Transactions from CSV
 7. Category-wise Expense Report     0. Exit
 8. Set Monthly Budget
 9. View Monthly Budget Status
```

Options 1-9 follow the required menu; 10-15 are extras (monthly summary, sorting, and the three bonus features). After every operation the program returns to the main menu.

## Features

**Required**
- Add income/expense with an auto-generated unique ID (IDs are never reused)
- Update (amount, category, description, date) and delete by ID, with details shown and a y/n confirmation before deleting
- Display all transactions in a formatted table
- Search by ID, category, type, or date range
- Financial summary: total income, total expenses, balance, expenses by category, highest expense, average expense
- Monthly summary for any month and year
- Monthly budget: view budget, remaining amount, percentage spent, warning at 80%, alert when exceeded (a warning/alert is also shown right after adding an expense that triggers it)

**Bonus**
- CSV export and import (invalid rows are skipped and counted)
- Text bar chart of expenses by category
- Recurring expenses (define once, generate for any month; running it twice does not create duplicates)

**Also included**
- Sorting (date or amount, ascending or descending) using insertion sort on a copy of the array

## Input validation and error handling

- Amounts must be greater than zero; non-numeric input is rejected and re-asked
- Category and description cannot be empty
- Dates must be valid `yyyy-MM-dd` (for example `2026-11-07`)
- Invalid menu choices are rejected with a message
- Adding a transaction when 100 are stored shows an error instead of crashing
- Missing or unreadable CSV files are reported without terminating the program

## CSV format

```
TransactionID,Type,Amount,Category,Description,Date
1001,Income,2400.00,Salary,Part-time bank paycheque,2026-09-15
```

Commas inside text are replaced with semicolons on export. On import, the ID column is ignored and a new unique ID is assigned.

## Notes

- Data is kept in memory while the program runs. Use option 14 (export) to save and option 15 (import) to reload it.
- Only one budget is active at a time; setting a new one replaces the old one.
