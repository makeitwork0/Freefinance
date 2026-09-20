package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ExchangeRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeRateDao {
    @Query("SELECT * FROM exchange_rates ORDER BY from_currency ASC, to_currency ASC")
    fun getAllRates(): Flow<List<ExchangeRateEntity>>

    @Query("SELECT * FROM exchange_rates")
    suspend fun getAllRatesList(): List<ExchangeRateEntity>

    @Query("SELECT * FROM exchange_rates WHERE from_currency = :fromCurrency AND to_currency = :toCurrency LIMIT 1")
    suspend fun getRate(fromCurrency: String, toCurrency: String): ExchangeRateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRate(rate: ExchangeRateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRates(rates: List<ExchangeRateEntity>): List<Long>

    @Query("DELETE FROM exchange_rates WHERE id = :id")
    suspend fun deleteRate(id: Long)

    @Query("DELETE FROM exchange_rates")
    suspend fun clearAll()
}
