package com.example.data.local.model

import java.util.Date

/**
 * Data model representing daily projected balance point for cash flow charts.
 */
data class DailyBalance(
    val date: Date,
    val dayLabel: String,
    val projectedBalance: Double,
    val dailyNetChange: Double
)
