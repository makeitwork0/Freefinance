package com.example.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.data.local.entity.FreelanceProjectEntity
import com.example.data.local.entity.MilestoneEntity

data class ProjectWithMilestones(
    @Embedded val project: FreelanceProjectEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "project_id"
    )
    val milestones: List<MilestoneEntity>
) {
    val totalPaid: Double
        get() = milestones.filter { it.status.equals("Paid", ignoreCase = true) }.sumOf { it.amount }

    val progressRatio: Float
        get() = if (project.totalExpectedFee > 0) (totalPaid / project.totalExpectedFee).coerceIn(0.0, 1.0).toFloat() else 0f
}
