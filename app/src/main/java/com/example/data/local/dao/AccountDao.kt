package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AccountEntity
import com.example.data.local.model.AccountBalanceSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts ORDER BY name ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :accountId LIMIT 1")
    fun getAccountById(accountId: Long): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts WHERE id = :accountId LIMIT 1")
    suspend fun getAccountByIdImmediate(accountId: Long): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>): List<Long>

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("UPDATE accounts SET current_balance = :newBalance WHERE id = :accountId")
    suspend fun updateCurrentBalance(accountId: Long, newBalance: Double)

    /**
     * Requirement (a): Get cleared balance vs planned future balance per account.
     * Computes cleared balance (initial + cleared transactions) and projected balance (including planned).
     */
    @Query("""
        SELECT 
            a.id AS accountId,
            a.name AS accountName,
            a.type AS accountType,
            a.color AS color,
            a.initial_balance AS initialBalance,
            (a.initial_balance + 
                COALESCE(
                    (SELECT SUM(
                        CASE 
                            WHEN t.type = 'Income' THEN t.amount
                            WHEN t.type = 'Expense' THEN -t.amount
                            WHEN t.type = 'Transfer' AND t.to_account_id = a.id THEN t.amount
                            WHEN t.type = 'Transfer' AND t.account_id = a.id THEN -t.amount
                            ELSE 0.0 
                        END
                    ) FROM transactions t WHERE (t.account_id = a.id OR t.to_account_id = a.id) AND t.status = 'Cleared'),
                    0.0
                )
            ) AS clearedBalance,
            (a.initial_balance + 
                COALESCE(
                    (SELECT SUM(
                        CASE 
                            WHEN t.type = 'Income' THEN t.amount
                            WHEN t.type = 'Expense' THEN -t.amount
                            WHEN t.type = 'Transfer' AND t.to_account_id = a.id THEN t.amount
                            WHEN t.type = 'Transfer' AND t.account_id = a.id THEN -t.amount
                            ELSE 0.0 
                        END
                    ) FROM transactions t WHERE (t.account_id = a.id OR t.to_account_id = a.id)),
                    0.0
                )
            ) AS projectedBalance
        FROM accounts a
        ORDER BY a.name ASC
    """)
    fun getAccountBalanceSummaries(): Flow<List<AccountBalanceSummary>>

    /**
     * Total cleared net worth across all accounts.
     */
    @Query("""
        SELECT 
            COALESCE(SUM(a.initial_balance), 0.0) + 
            COALESCE((
                SELECT SUM(
                    CASE 
                        WHEN t.type = 'Income' THEN t.amount
                        WHEN t.type = 'Expense' THEN -t.amount
                        ELSE 0.0 
                    END
                ) FROM transactions t WHERE t.status = 'Cleared'
            ), 0.0)
        FROM accounts a
    """)
    fun getTotalClearedNetWorth(): Flow<Double>
}
