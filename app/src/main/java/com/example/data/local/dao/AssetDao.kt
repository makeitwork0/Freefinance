package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AssetEntity
import kotlinx.coroutines.flow.Flow

/**
 * AssetDao: Data Access Object for querying and modifying valuables and asset inventory.
 */
@Dao
interface AssetDao {

    @Query("SELECT * FROM assets ORDER BY estimated_value DESC")
    fun getAllAssets(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE include_in_net_worth = 1 ORDER BY estimated_value DESC")
    fun getIncludedAssets(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE id = :id")
    suspend fun getAssetById(id: Long): AssetEntity?

    @Query("SELECT * FROM assets WHERE category = :category ORDER BY estimated_value DESC")
    fun getAssetsByCategory(category: String): Flow<List<AssetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: AssetEntity): Long

    @Update
    suspend fun updateAsset(asset: AssetEntity)

    @Delete
    suspend fun deleteAsset(asset: AssetEntity)

    @Query("DELETE FROM assets WHERE id = :id")
    suspend fun deleteAssetById(id: Long)

    @Query("SELECT SUM(estimated_value) FROM assets WHERE include_in_net_worth = 1")
    fun getTotalAssetsValue(): Flow<Double?>
}
