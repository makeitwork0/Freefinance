package com.example.util

import android.content.Context
import com.example.data.sync.DataSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AutoBackupHelper {

    private const val AUTO_BACKUP_FILE_NAME = "finance_autobackup_latest.json"

    fun getSnapshotFile(context: Context): File {
        return File(context.filesDir, AUTO_BACKUP_FILE_NAME)
    }

    fun hasSnapshot(context: Context): Boolean {
        val file = getSnapshotFile(context)
        return file.exists() && file.length() > 0
    }

    fun getSnapshotLastModified(context: Context): String? {
        val file = getSnapshotFile(context)
        if (!file.exists()) return null
        val date = Date(file.lastModified())
        return SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(date)
    }

    suspend fun saveAutoSnapshot(context: Context, dataSyncManager: DataSyncManager, baseCurrency: String) {
        withContext(Dispatchers.IO) {
            try {
                val json = dataSyncManager.exportStateToJson(baseCurrency)
                val file = getSnapshotFile(context)
                file.writeText(json, Charsets.UTF_8)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun restoreAutoSnapshot(context: Context, dataSyncManager: DataSyncManager): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val file = getSnapshotFile(context)
                if (!file.exists()) {
                    return@withContext Result.failure(IllegalStateException("No local snapshot file found"))
                }
                val json = file.readText(Charsets.UTF_8)
                dataSyncManager.importStateFromJson(json)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
