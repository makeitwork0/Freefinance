package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.local.model.CompoundingFrequency
import java.util.Date

/**
 * AccountEntity: Tracks accounts across Cash, Bank, E-Wallets, and Locked Deposits with P.A. Yields.
 */
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "type")
    val type: String, // Cash, Bank, E-Wallet, Investment

    @ColumnInfo(name = "initial_balance")
    val initialBalance: Double = 0.0,

    @ColumnInfo(name = "current_balance")
    val currentBalance: Double = 0.0,

    @ColumnInfo(name = "color")
    val color: Int, // ARGB Int representation for visual branding

    @ColumnInfo(name = "interest_rate_pa")
    val interestRatePa: Float = 0.0f,

    @ColumnInfo(name = "compounding_frequency")
    val compoundingFrequency: CompoundingFrequency = CompoundingFrequency.NONE,

    @ColumnInfo(name = "currency_code", defaultValue = "USD")
    val currencyCode: String = "USD",

    @ColumnInfo(name = "is_locked", defaultValue = "0")
    val isLocked: Boolean = false,

    @ColumnInfo(name = "locked_until")
    val lockedUntil: Date? = null,

    @ColumnInfo(name = "linked_app_package")
    val linkedAppPackage: String? = null
)
