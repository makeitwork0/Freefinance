package com.example.data.local.model

enum class DebtType(val displayName: String) {
    LENT("Lent"),         // Money I lent to someone (Receivable / Owed to me)
    BORROWED("Borrowed"); // Money I borrowed (Payable / I owe)

    companion object {
        fun fromString(value: String): DebtType =
            entries.find { it.displayName.equals(value, ignoreCase = true) } ?: LENT
    }
}

enum class DebtStatus(val displayName: String) {
    ACTIVE("Active"),
    SETTLED("Settled");

    companion object {
        fun fromString(value: String): DebtStatus =
            entries.find { it.displayName.equals(value, ignoreCase = true) } ?: ACTIVE
    }
}
