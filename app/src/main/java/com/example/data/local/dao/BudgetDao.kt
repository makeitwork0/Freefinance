package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.model.BudgetProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets ORDER BY start_date DESC")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE id = :budgetId LIMIT 1")
    fun getBudgetById(budgetId: Long): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE category_id = :categoryId LIMIT 1")
    fun getBudgetByCategoryId(categoryId: Long): Flow<BudgetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>): List<Long>

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    /**
     * Requirement (c): Get budget progress by category.
     * Computes current spent within the budget period, remaining limit, and progress ratio.
     */
    @Query("""
        SELECT 
            b.id AS budgetId,
            b.category_id AS categoryId,
            c.name AS categoryName,
            c.icon_res_id AS categoryIcon,
            c.color AS categoryColor,
            b.amount_limit AS amountLimit,
            b.period AS period,
            b.start_date AS startDate,
            b.end_date AS endDate,
            COALESCE(SUM(CASE WHEN t.type = 'Expense' AND t.status = 'Cleared' THEN t.amount ELSE 0.0 END), 0.0) AS currentSpent,
            (b.amount_limit - COALESCE(SUM(CASE WHEN t.type = 'Expense' AND t.status = 'Cleared' THEN t.amount ELSE 0.0 END), 0.0)) AS remainingAmount,
            CASE 
                WHEN b.amount_limit > 0 THEN (COALESCE(SUM(CASE WHEN t.type = 'Expense' AND t.status = 'Cleared' THEN t.amount ELSE 0.0 END), 0.0) * 1.0 / b.amount_limit)
                ELSE 0.0 
            END AS progressRatio
        FROM budgets b
        INNER JOIN categories c ON b.category_id = c.id
        LEFT JOIN transactions t ON t.category_id = b.category_id 
             AND t.date BETWEEN b.start_date AND b.end_date
        GROUP BY b.id
        ORDER BY progressRatio DESC
    """)
    fun getBudgetProgressByCategory(): Flow<List<BudgetProgress>>

    /**
     * Requirement (c): Get budget progress for a specific budget.
     */
    @Query("""
        SELECT 
            b.id AS budgetId,
            b.category_id AS categoryId,
            c.name AS categoryName,
            c.icon_res_id AS categoryIcon,
            c.color AS categoryColor,
            b.amount_limit AS amountLimit,
            b.period AS period,
            b.start_date AS startDate,
            b.end_date AS endDate,
            COALESCE(SUM(CASE WHEN t.type = 'Expense' AND t.status = 'Cleared' THEN t.amount ELSE 0.0 END), 0.0) AS currentSpent,
            (b.amount_limit - COALESCE(SUM(CASE WHEN t.type = 'Expense' AND t.status = 'Cleared' THEN t.amount ELSE 0.0 END), 0.0)) AS remainingAmount,
            CASE 
                WHEN b.amount_limit > 0 THEN (COALESCE(SUM(CASE WHEN t.type = 'Expense' AND t.status = 'Cleared' THEN t.amount ELSE 0.0 END), 0.0) * 1.0 / b.amount_limit)
                ELSE 0.0 
            END AS progressRatio
        FROM budgets b
        INNER JOIN categories c ON b.category_id = c.id
        LEFT JOIN transactions t ON t.category_id = b.category_id 
             AND t.date BETWEEN b.start_date AND b.end_date
        WHERE b.id = :budgetId
        GROUP BY b.id
    """)
    fun getBudgetProgressById(budgetId: Long): Flow<BudgetProgress?>
}
