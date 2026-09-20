package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "milestones",
    foreignKeys = [
        ForeignKey(
            entity = FreelanceProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["project_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("project_id"),
        Index("status"),
        Index("expected_date")
    ]
)
data class MilestoneEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "project_id")
    val projectId: Long,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "expected_date")
    val expectedDate: Date,

    @ColumnInfo(name = "status")
    val status: String = "Pending", // "Pending", "Paid"

    @ColumnInfo(name = "paid_at")
    val paidAt: Date? = null
)
