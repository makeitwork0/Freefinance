package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

object DataExportUtils {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun generateCsv(
        accounts: List<AccountEntity>,
        transactions: List<TransactionEntity>
    ): String {
        val builder = StringBuilder()

        // Section 1: ACCOUNTS
        builder.appendLine("--- ACCOUNTS ---")
        builder.appendLine("Account ID,Name,Type,Initial Balance,Current Balance,Interest Rate P.A. (%),Compounding Frequency")
        for (account in accounts) {
            val line = listOf(
                account.id.toString(),
                escapeCsv(account.name),
                escapeCsv(account.type),
                String.format(Locale.getDefault(), "%.2f", account.initialBalance),
                String.format(Locale.getDefault(), "%.2f", account.currentBalance),
                String.format(Locale.getDefault(), "%.2f", account.interestRatePa),
                escapeCsv(account.compoundingFrequency.displayName)
            ).joinToString(",")
            builder.appendLine(line)
        }

        builder.appendLine()

        // Section 2: TRANSACTIONS
        builder.appendLine("--- TRANSACTIONS ---")
        builder.appendLine("Transaction ID,Amount,Type,Date,Status,Account ID,To Account ID,Category ID,Labels,Note,Recurrence Rule")
        for (tx in transactions) {
            val line = listOf(
                tx.id.toString(),
                String.format(Locale.getDefault(), "%.2f", tx.amount),
                escapeCsv(tx.type),
                escapeCsv(dateFormat.format(tx.date)),
                escapeCsv(tx.status),
                tx.accountId.toString(),
                tx.toAccountId?.toString() ?: "",
                tx.categoryId?.toString() ?: "",
                escapeCsv(tx.labels.joinToString(";")),
                escapeCsv(tx.note ?: ""),
                escapeCsv(tx.recurrenceRule)
            ).joinToString(",")
            builder.appendLine(line)
        }

        return builder.toString()
    }

    suspend fun writeCsvToUri(context: Context, uri: Uri, csvContent: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.bufferedWriter().use { writer ->
                        writer.write(csvContent)
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n")
        return if (needsQuotes) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
