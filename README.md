# Personal Expense Tracker

A console-based Java application for recording and managing personal income and expenses, with category reports and monthly budget alerts. Built to practise object-oriented design, arrays of objects, searching and sorting, and file handling in plain Java (no external libraries).

## Features

- **Transactions:** add, update, delete and display income and expenses, each with an auto-generated unique ID
- **Search:** by ID, category, type (income/expense) or date range
- **Reports:** total income, total expenses, balance, average and highest expense, expenses by category, and a monthly summary
- **Budgeting:** set a monthly budget, see the amount remaining and percentage spent, get a warning at 80% and an alert when the budget is exceeded
- **Input validation:** rejects zero or negative amounts, invalid dates, empty fields and non-numeric input without crashing
- **Extras:** CSV export and import, sorting by date or amount, a text bar chart of spending by category, and recurring monthly expenses

## Screenshots


## Getting started

Requires JDK 11 or newer.

```
git clone https://github.com/MDybala19/java-expense-tracker.git
cd java-expense-tracker
javac *.java
java ExpenseTrackerApplication
```

To try it with data, choose **15. Import Transactions from CSV** and enter `sample_transactions.csv` (20 sample transactions).

## How it is built

| Class | Responsibility |
|-------|----------------|
| `Transaction` | One income/expense record; validates every field |
| `ExpenseTracker` | Stores transactions in a fixed-size array (capacity 100) and handles add, update, delete, search, sort, totals, reports and CSV |
| `Budget` | Monthly budget, remaining amount, percentage spent, warning and alert |
| `RecurringTransaction` | Template for a monthly recurring expense |
| `ExpenseTrackerApplication` | `main()`, the menu, and input handling |

Design notes:
- Transactions are stored in a plain array with a counter, not an `ArrayList`. Deleting an item shifts the later elements left so there are no gaps.
- Totals and reports are calculated from the stored transactions every time, never cached.
- Invalid input is handled with `try`/`catch`, so the program keeps running.

See [CLASS_EXPLANATION.md](CLASS_EXPLANATION.md) for a method-by-method description.

## What I learned

This project taught me how to split a program into classes that each have one job. The part that took me longest to understand was storing transactions in a fixed-size array. When one is deleted, the later items have to shift left so there are no gaps. I also practised validating user input with try/catch so that bad entries show a message instead of crashing the program. Next I want to add automatic saving between runs and unit tests.

## Future improvements

- Save data automatically between runs, instead of relying on manual CSV export
- Keep a separate budget for each month
- Replace the fixed-size array with a growing structure so there is no 100-transaction limit
- Add a graphical interface (JavaFX or Swing)
- Add unit tests with JUnit
- Edit recurring expenses and delete them from the menu