package com.example.data.local.model

/**
 * CategoryExpense: Aggregated expense totals per category for pie/donut charts and visualizers.
 */
data class CategoryExpense(
    val categoryId: Long,
    val categoryName: String,
    val categoryColor: String,
    val categoryIcon: String? = null,
    val totalAmount: Double
)
