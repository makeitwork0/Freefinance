package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * TransactionEntity: Captures financial movements, cleared vs planned status, recurrence for forecasting, and labels.
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["account_id"]),
        Index(value = ["category_id"]),
        Index(value = ["date"]),
        Index(value = ["status"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "account_id")
    val accountId: Long,

    @ColumnInfo(name = "to_account_id")
    val toAccountId: Long? = null, // Destination account if type is Transfer

    @ColumnInfo(name = "type")
    val type: String, // Income, Expense, Transfer

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "category_id")
    val categoryId: Long?,

    @ColumnInfo(name = "date")
    val date: Date,

    @ColumnInfo(name = "status")
    val status: String, // Cleared, Planned

    @ColumnInfo(name = "recurrence_rule")
    val recurrenceRule: String, // None, Daily, Weekly, Monthly

    @ColumnInfo(name = "labels")
    val labels: List<String> = emptyList(), // Project/Debt labels

    @ColumnInfo(name = "note")
    val note: String? = null
)
