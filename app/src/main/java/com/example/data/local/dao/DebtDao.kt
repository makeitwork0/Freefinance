package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DebtEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebts(debts: List<DebtEntity>): List<Long>

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Delete
    suspend fun deleteDebt(debt: DebtEntity)

    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    fun getDebtById(id: Long): Flow<DebtEntity?>

    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    suspend fun getDebtByIdImmediate(id: Long): DebtEntity?

    @Query("SELECT * FROM debts ORDER BY created_at DESC")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE type = :type ORDER BY created_at DESC")
    fun getDebtsByType(type: String): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE type = :type AND status = :status ORDER BY created_at DESC")
    fun getDebtsByTypeAndStatus(type: String, status: String = "Active"): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE type = 'Lent' AND status = 'Active' ORDER BY remaining_amount DESC LIMIT :limit")
    fun getTopReceivables(limit: Int = 3): Flow<List<DebtEntity>>

    @Query("SELECT COALESCE(SUM(remaining_amount), 0.0) FROM debts WHERE type = 'Lent' AND status = 'Active'")
    fun getTotalActiveLent(): Flow<Double>

    @Query("SELECT COALESCE(SUM(remaining_amount), 0.0) FROM debts WHERE type = 'Borrowed' AND status = 'Active'")
    fun getTotalActiveBorrowed(): Flow<Double>

    @Query("UPDATE debts SET remaining_amount = :remaining, status = :status WHERE id = :id")
    suspend fun updateRemainingAndStatus(id: Long, remaining: Double, status: String)
}
