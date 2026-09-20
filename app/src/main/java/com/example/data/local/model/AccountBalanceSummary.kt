package com.example.data.local.model

/**
 * Aggregated account summary holding cleared balance vs planned future balance.
 */
data class AccountBalanceSummary(
    val accountId: Long,
    val accountName: String,
    val accountType: String,
    val color: Int,
    val initialBalance: Double,
    val clearedBalance: Double,
    val projectedBalance: Double
)
