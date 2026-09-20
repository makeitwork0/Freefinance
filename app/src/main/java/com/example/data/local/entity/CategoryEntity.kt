package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * CategoryEntity: Supports primary categories and subcategories with hierarchical parent_category_id.
 */
@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["parent_category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["parent_category_id"])
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "icon_res_id")
    val iconResId: String, // Resource name or icon key (e.g., "ic_food", "ic_shopping")

    @ColumnInfo(name = "type")
    val type: String, // Expense, Income

    @ColumnInfo(name = "color")
    val color: Int,

    @ColumnInfo(name = "parent_category_id")
    val parentCategoryId: Long? = null
)
