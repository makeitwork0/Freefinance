package com.example.data.local.model

/**
 * Standard account types matching Wallet by BudgetBakers multi-account model.
 */
enum class AccountType(val displayName: String) {
    CASH("Cash"),
    BANK("Bank Account"),
    E_WALLET("E-Wallet"),
    SAVINGS("Savings"),
    INVESTMENT("Locked Deposit / Investment");

    companion object {
        fun fromString(value: String): AccountType = entries.firstOrNull { 
            it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) 
        } ?: CASH
    }
}

/**
 * Transaction type supported across accounts and categories.
 */
enum class TransactionType(val displayName: String) {
    INCOME("Income"),
    EXPENSE("Expense"),
    TRANSFER("Transfer");

    companion object {
        fun fromString(value: String): TransactionType = entries.firstOrNull { 
            it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) 
        } ?: EXPENSE
    }
}

/**
 * Status of the transaction: Cleared (realized in balance) or Planned (future/pending).
 */
enum class TransactionStatus(val displayName: String) {
    CLEARED("Cleared"),
    PLANNED("Planned");

    companion object {
        fun fromString(value: String): TransactionStatus = entries.firstOrNull { 
            it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) 
        } ?: CLEARED
    }
}

/**
 * Frequency of recurring scheduled transactions for cash flow forecasting.
 */
enum class RecurrenceRule(val displayName: String) {
    NONE("None"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly");

    companion object {
        fun fromString(value: String): RecurrenceRule = entries.firstOrNull { 
            it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) 
        } ?: NONE
    }
}

/**
 * Category types separating expense and income streams.
 */
enum class CategoryType(val displayName: String) {
    EXPENSE("Expense"),
    INCOME("Income");

    companion object {
        fun fromString(value: String): CategoryType = entries.firstOrNull { 
            it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) 
        } ?: EXPENSE
    }
}

/**
 * Budgeting periods for category spending limits.
 */
enum class BudgetPeriod(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    CUSTOM("Custom");

    companion object {
        fun fromString(value: String): BudgetPeriod = entries.firstOrNull { 
            it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) 
        } ?: MONTHLY
    }
}

/**
 * Compounding frequency for account interest yields (P.A.).
 */
enum class CompoundingFrequency(val displayName: String) {
    NONE("None"),
    DAILY("Daily"),
    MONTHLY("Monthly"),
    ANNUALLY("Annually");

    companion object {
        fun fromString(value: String): CompoundingFrequency = entries.firstOrNull {
            it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true)
        } ?: NONE
    }
}
