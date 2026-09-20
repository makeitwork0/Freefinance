package com.example.data.local.model

import java.util.Date

/**
 * Budget progress breakdown with category details and spending ratio.
 */
data class BudgetProgress(
    val budgetId: Long,
    val categoryId: Long,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: Int,
    val amountLimit: Double,
    val period: String,
    val startDate: Date,
    val endDate: Date,
    val currentSpent: Double,
    val remainingAmount: Double,
    val progressRatio: Double
) {
    val isOverBudget: Boolean
        get() = currentSpent > amountLimit
}
