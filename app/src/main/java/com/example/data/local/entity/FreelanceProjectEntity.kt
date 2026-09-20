package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "freelance_projects",
    indices = [
        Index("status")
    ]
)
data class FreelanceProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "project_name")
    val projectName: String,

    @ColumnInfo(name = "client")
    val client: String,

    @ColumnInfo(name = "total_expected_fee")
    val totalExpectedFee: Double,

    @ColumnInfo(name = "status")
    val status: String = "Active", // "Active", "Completed"

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date()
)
