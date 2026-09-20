package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * DebtEntity: Stores records of money lent to others (receivables) or borrowed from others (payables).
 */
@Entity(
    tableName = "debts",
    indices = [
        Index("type"),
        Index("status")
    ]
)
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "person_name")
    val personName: String,

    @ColumnInfo(name = "type")
    val type: String, // "Lent" or "Borrowed"

    @ColumnInfo(name = "total_amount")
    val totalAmount: Double,

    @ColumnInfo(name = "remaining_amount")
    val remainingAmount: Double,

    @ColumnInfo(name = "due_date")
    val dueDate: Date? = null,

    @ColumnInfo(name = "status")
    val status: String = "Active", // "Active" or "Settled"

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date()
)
