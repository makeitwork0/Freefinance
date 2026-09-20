package com.example.data.local.converter

import androidx.room.TypeConverter
import org.json.JSONArray
import java.util.Date

/**
 * Room TypeConverters for Date objects and List<String> labels.
 */
class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        val list = mutableListOf<String>()
        return try {
            val jsonArray = JSONArray(value)
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            list
        } catch (_: Exception) {
            // Fallback for comma-separated values if legacy format
            value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String {
        if (list.isNullOrEmpty()) return "[]"
        val jsonArray = JSONArray()
        for (item in list) {
            jsonArray.put(item)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun fromCompoundingFrequency(value: String?): com.example.data.local.model.CompoundingFrequency {
        return value?.let { com.example.data.local.model.CompoundingFrequency.fromString(it) }
            ?: com.example.data.local.model.CompoundingFrequency.NONE
    }

    @TypeConverter
    fun compoundingFrequencyToString(frequency: com.example.data.local.model.CompoundingFrequency?): String {
        return (frequency ?: com.example.data.local.model.CompoundingFrequency.NONE).name
    }
}
