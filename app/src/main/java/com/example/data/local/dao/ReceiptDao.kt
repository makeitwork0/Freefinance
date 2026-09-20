package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ReceiptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {

    @Query("SELECT * FROM receipts_inbox WHERE status = 'PENDING' ORDER BY captured_at DESC")
    fun getPendingReceipts(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts_inbox WHERE status = 'PENDING' ORDER BY captured_at DESC")
    suspend fun getPendingReceiptsImmediate(): List<ReceiptEntity>

    @Query("SELECT COUNT(*) FROM receipts_inbox WHERE status = 'PENDING'")
    fun getPendingReceiptCount(): Flow<Int>

    @Query("SELECT * FROM receipts_inbox ORDER BY captured_at DESC")
    fun getAllReceipts(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts_inbox WHERE id = :id")
    fun getReceiptById(id: Long): Flow<ReceiptEntity?>

    @Query("SELECT * FROM receipts_inbox WHERE id = :id")
    suspend fun getReceiptByIdImmediate(id: Long): ReceiptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity): Long

    @Update
    suspend fun updateReceipt(receipt: ReceiptEntity)

    @Delete
    suspend fun deleteReceipt(receipt: ReceiptEntity)

    @Query("DELETE FROM receipts_inbox WHERE id = :id")
    suspend fun deleteReceiptById(id: Long)

    @Query("DELETE FROM receipts_inbox WHERE status = 'DISCARDED'")
    suspend fun clearDiscardedReceipts()
}
