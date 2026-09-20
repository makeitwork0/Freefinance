package com.example.data.local.model

/**
 * MonthlyCategorySegment: Represents a category spending portion inside a month.
 */
data class MonthlyCategorySegment(
    val categoryId: Long?,
    val categoryName: String,
    val color: Int,
    val amount: Double,
    val percentage: Float
)

/**
 * MonthlySpendingTrend: Aggregates total spent, category breakdowns, income, projections, and budget targets.
 */
data class MonthlySpendingTrend(
    val monthLabel: String,      // e.g., "Jan", "Feb", "Mar"
    val year: Int,
    val monthIndex: Int,         // 0 for Jan, 11 for Dec
    val totalExpense: Double,
    val totalIncome: Double,
    val transactionCount: Int,
    val averageDailySpending: Double = 0.0,
    val categorySegments: List<MonthlyCategorySegment> = emptyList(),
    val monthlyBudgetLimit: Double = 0.0,
    val projectedMonthEndExpense: Double = 0.0,
    val isCurrentMonth: Boolean = false,
    val daysElapsed: Int = 0,
    val totalDaysInMonth: Int = 30
)
