package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * ReceiptEntity: Represents a quickly snapped receipt/bill photo stored in the Inbox queue
 * awaiting review, amount estimation, and conversion into an Expense or Income transaction.
 */
@Entity(tableName = "receipts_inbox")
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "image_path")
    val imagePath: String,

    @ColumnInfo(name = "captured_at")
    val capturedAt: Date = Date(),

    @ColumnInfo(name = "note")
    val note: String? = null,

    @ColumnInfo(name = "suggested_amount")
    val suggestedAmount: Double? = null,

    @ColumnInfo(name = "suggested_type")
    val suggestedType: String = "Expense", // Expense or Income

    @ColumnInfo(name = "status")
    val status: String = "PENDING", // PENDING, CONVERTED, DISCARDED

    @ColumnInfo(name = "converted_transaction_id")
    val convertedTransactionId: Long? = null
)
