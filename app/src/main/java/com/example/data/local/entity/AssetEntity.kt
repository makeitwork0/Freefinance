package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * AssetEntity: Stores physical, digital, and tangible valuables (e.g. Vehicles, Real Estate,
 * Electronics, Jewelry, Collectibles) and their estimated market values.
 */
@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "category")
    val category: String, // Vehicle, Electronics, Real Estate, Jewelry, Collectibles, Equipment, Other

    @ColumnInfo(name = "estimated_value")
    val estimatedValue: Double,

    @ColumnInfo(name = "purchase_price", defaultValue = "0.0")
    val purchasePrice: Double = 0.0,

    @ColumnInfo(name = "purchase_date")
    val purchaseDate: Date? = null,

    @ColumnInfo(name = "currency_code", defaultValue = "USD")
    val currencyCode: String = "USD",

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "icon_name", defaultValue = "ic_asset")
    val iconName: String = "ic_asset",

    @ColumnInfo(name = "color", defaultValue = "-16738120")
    val color: Int = -16738120, // ARGB Color Int

    @ColumnInfo(name = "include_in_net_worth", defaultValue = "1")
    val includeInNetWorth: Boolean = true,

    @ColumnInfo(name = "created_at", defaultValue = "0")
    val createdAt: Long = System.currentTimeMillis()
)
