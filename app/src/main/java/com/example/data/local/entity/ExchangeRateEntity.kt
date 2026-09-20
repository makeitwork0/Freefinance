package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * ExchangeRateEntity: Stores currency exchange rates relative to each other or a base currency.
 * Used for multi-currency conversion across different account balances.
 */
@Entity(
    tableName = "exchange_rates",
    indices = [Index(value = ["from_currency", "to_currency"], unique = true)]
)
data class ExchangeRateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "from_currency")
    val fromCurrency: String,

    @ColumnInfo(name = "to_currency")
    val toCurrency: String,

    @ColumnInfo(name = "rate")
    val rate: Double,

    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
)
