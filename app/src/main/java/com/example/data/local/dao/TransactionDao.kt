package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.model.CategoryExpense
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :transactionId LIMIT 1")
    fun getTransactionById(transactionId: Long): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions WHERE account_id = :accountId OR to_account_id = :accountId ORDER BY date DESC")
    fun getTransactionsByAccount(accountId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE category_id = :categoryId ORDER BY date DESC")
    fun getTransactionsByCategory(categoryId: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long>

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET status = 'Cleared' WHERE id = :transactionId")
    suspend fun markTransactionCleared(transactionId: Long)

    /**
     * Requirement (a): Get cleared balance change vs planned future balance change overall.
     * Returns total cleared delta (Income - Expense) and total planned delta.
     */
    @Query("""
        SELECT 
            COALESCE(SUM(CASE WHEN status = 'Cleared' AND type = 'Income' THEN amount 
                              WHEN status = 'Cleared' AND type = 'Expense' THEN -amount 
                              ELSE 0.0 END), 0.0)
        FROM transactions
    """)
    fun getTotalClearedCashFlow(): Flow<Double>

    @Query("""
        SELECT 
            COALESCE(SUM(CASE WHEN status = 'Planned' AND type = 'Income' THEN amount 
                              WHEN status = 'Planned' AND type = 'Expense' THEN -amount 
                              ELSE 0.0 END), 0.0)
        FROM transactions
    """)
    fun getTotalPlannedCashFlow(): Flow<Double>

    /**
     * Requirement (b): Get transactions scheduled for future dates (for Cash Flow Forecasting).
     * Retrieves all future-dated transactions from a given timestamp onwards.
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE date >= :fromTimestamp 
        ORDER BY date ASC, id ASC
    """)
    fun getTransactionsScheduledFromDate(fromTimestamp: Date): Flow<List<TransactionEntity>>

    /**
     * Requirement (b): Get all planned or recurring transactions for forecasting projection engines.
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE status = 'Planned' OR recurrence_rule != 'None' 
        ORDER BY date ASC
    """)
    fun getPlannedAndRecurringTransactions(): Flow<List<TransactionEntity>>

    /**
     * Requirement (b): Get transactions in a specific date range for day-increment forecasting.
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE date BETWEEN :startDate AND :endDate 
        ORDER BY date ASC
    """)
    fun getTransactionsForDateRange(startDate: Date, endDate: Date): Flow<List<TransactionEntity>>

    /**
     * Filter transactions by project/debt label.
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE labels LIKE '%' || :label || '%' 
        ORDER BY date DESC
    """)
    fun getTransactionsByLabel(label: String): Flow<List<TransactionEntity>>

    /**
     * Requirement (2): Category Expense visualizer.
     * Groups expenses by category and sums total amounts.
     */
    @Query("""
        SELECT 
            c.id AS categoryId,
            c.name AS categoryName,
            c.color AS categoryColor,
            c.icon_res_id AS categoryIcon,
            COALESCE(SUM(t.amount), 0.0) AS totalAmount
        FROM transactions t
        INNER JOIN categories c ON t.category_id = c.id
        WHERE t.type = 'Expense' AND t.status = 'Cleared'
        GROUP BY c.id
        HAVING totalAmount > 0
        ORDER BY totalAmount DESC
    """)
    fun getCategoryExpenses(): Flow<List<CategoryExpense>>

    /**
     * Requirement (4): Subscriptions and recurring bills.
     * Fetches all transactions where recurrence_rule != 'None'.
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE UPPER(recurrence_rule) != 'NONE' 
        ORDER BY date DESC
    """)
    fun getRecurringTransactions(): Flow<List<TransactionEntity>>

    /**
     * Cancel / stop recurring transaction.
     */
    @Query("UPDATE transactions SET recurrence_rule = 'None' WHERE id = :id")
    suspend fun stopRecurrence(id: Long)
}
