# Classes and Methods

## Transaction
Stores one financial record. Every setter validates its input and throws `IllegalArgumentException` for bad data, so an invalid transaction can never exist in the system.

- **Attributes:** `transactionId`, `transactionType` ("Income"/"Expense"), `amount`, `category`, `description`, `date` (`LocalDate`)
- **Constructor:** validates all fields
- **Getters / setters:** one per attribute; setters reject zero/negative amounts, empty text, and invalid types
- `displayDetails()` - prints all fields (used before updating/deleting)
- `toTableRow()` / `tableHeader()` - formatted table output
- `formatMoney(double)` - one consistent money format (`$1,234.56`) used by the whole program
- `toCsvLine()` - one CSV row for export

## ExpenseTracker
Manages the data. Holds `Transaction[] transactions` (size 100) and `transactionCount`.

- `addTransaction()` - creates the transaction with the next unique ID and inserts it at the end; throws if the array is full
- `updateTransaction()` - validates the new values first, then saves them
- `deleteTransaction()` - finds the index, shifts all later elements one position left, clears the last slot, decrements the count
- `findById()` / `findIndexById()` - linear search by ID
- `searchTransaction(int id)`, `searchTransaction(String category)`, `searchByType()`, `searchByDateRange()` - linear searches returning an array of matches
- `getSortedCopy()` - insertion sort by date or amount on a copy (stored order unchanged)
- `displayTransactions()` - formatted table, or a message when empty
- `calculateTotalIncome()`, `calculateTotalExpenses()`, `calculateBalance()`, `calculateAverageExpense()`, `findHighestExpense()` - all computed by looping over the stored transactions
- `calculateMonthlyIncome()` / `calculateMonthlyExpenses()` - same, filtered by month and year
- `generateFinancialReport()` / `generateMonthlyReport()` - overall and monthly summaries
- `displayCategoryReport()` / `displayExpenseChart()` - totals by category, and the text bar chart
- `displayBudgetStatus()` / `budgetNoticeFor()` - budget status using spending calculated for the budget's month
- `exportToCsv()` / `importFromCsv()` - file handling with `IOException` handling; invalid rows skipped
- `addRecurring()` / `generateRecurringTransactions()` - recurring expense templates (array of 20) and generation without duplicates

## Budget
Manages the monthly budget. Attributes: `monthlyBudget`, `month`, `year`.

- `setBudget()` - validates amount, month, year
- `calculateRemainingBudget(spent)` - budget minus spending
- `calculateBudgetPercentage(spent)` - percent of budget used
- `checkBudgetStatus(spent)` - "OK", a warning at 80% or more, or an alert when exceeded

## RecurringTransaction
A template (category, description, amount, day of month) for an expense that repeats every month. If the day does not exist in a month (for example day 31 in November) the last day of that month is used.

## ExpenseTrackerApplication
Contains `main()`. Shows the menu, reads user input through helper methods (`readInt`, `readPositiveAmount`, `readNonEmpty`, `readDate`, `readType`, `readYesNo`) that keep asking until the input is valid, and calls the other classes. Each menu option has its own method (`addTransaction()`, `searchTransactions()`, ...) to keep the code modular.

## Key design points
- Array of objects with a count variable; deleting shifts elements to avoid gaps
- No collection classes are used for transactions
- Nothing is stored as a running total: every figure is calculated from the array when requested
- try-catch is used for number parsing, date parsing, file I/O, and invalid-data exceptions, so bad input never crashes the program
