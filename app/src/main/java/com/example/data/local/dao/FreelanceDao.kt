package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.FreelanceProjectEntity
import com.example.data.local.entity.MilestoneEntity
import com.example.data.local.model.ProjectWithMilestones
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface FreelanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: FreelanceProjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<FreelanceProjectEntity>): List<Long>

    @Update
    suspend fun updateProject(project: FreelanceProjectEntity)

    @Delete
    suspend fun deleteProject(project: FreelanceProjectEntity)

    @Query("SELECT * FROM freelance_projects ORDER BY created_at DESC")
    fun getAllProjects(): Flow<List<FreelanceProjectEntity>>

    @Query("SELECT * FROM freelance_projects WHERE status = 'Active' ORDER BY created_at DESC")
    fun getActiveProjects(): Flow<List<FreelanceProjectEntity>>

    @Transaction
    @Query("SELECT * FROM freelance_projects WHERE status = 'Active' ORDER BY created_at DESC")
    fun getActiveProjectsWithMilestones(): Flow<List<ProjectWithMilestones>>

    @Transaction
    @Query("SELECT * FROM freelance_projects ORDER BY created_at DESC")
    fun getAllProjectsWithMilestones(): Flow<List<ProjectWithMilestones>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: MilestoneEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<MilestoneEntity>): List<Long>

    @Update
    suspend fun updateMilestone(milestone: MilestoneEntity)

    @Delete
    suspend fun deleteMilestone(milestone: MilestoneEntity)

    @Query("SELECT * FROM milestones ORDER BY expected_date ASC")
    fun getAllMilestones(): Flow<List<MilestoneEntity>>

    @Query("SELECT * FROM milestones WHERE project_id = :projectId ORDER BY expected_date ASC")
    fun getMilestonesForProject(projectId: Long): Flow<List<MilestoneEntity>>

    @Query("SELECT * FROM milestones WHERE status = 'Pending' ORDER BY expected_date ASC")
    fun getPendingMilestones(): Flow<List<MilestoneEntity>>

    @Query("SELECT * FROM milestones WHERE status = 'Pending' ORDER BY expected_date ASC")
    suspend fun getPendingMilestonesImmediate(): List<MilestoneEntity>

    @Query("SELECT * FROM milestones WHERE id = :milestoneId LIMIT 1")
    suspend fun getMilestoneById(milestoneId: Long): MilestoneEntity?

    @Query("UPDATE milestones SET status = :status, paid_at = :paidAt WHERE id = :id")
    suspend fun updateMilestoneStatus(id: Long, status: String, paidAt: Date? = null)

    @Query("UPDATE freelance_projects SET status = :status WHERE id = :id")
    suspend fun updateProjectStatus(id: Long, status: String)
}
