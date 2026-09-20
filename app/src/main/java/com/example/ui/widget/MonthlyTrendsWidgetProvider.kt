package com.example.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.model.TransactionType
import com.example.data.preferences.UserPreferencesRepository
import com.example.util.CurrencyUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

class MonthlyTrendsWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
            val pendingAppIntent = FinanceAppWidgetManager.getAppOpenPendingIntent(context)

            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val transactions = try { db.transactionDao().getAllTransactions().first() } catch (e: Exception) { emptyList() }

                val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                val cal = Calendar.getInstance()
                val currentYear = cal.get(Calendar.YEAR)
                val currentMonth = cal.get(Calendar.MONTH)
                val currentDay = cal.get(Calendar.DAY_OF_MONTH)
                val totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

                var currentExpense = 0.0
                var count = 0
                for (tx in transactions) {
                    if (tx.status == "Cleared" && tx.type == TransactionType.EXPENSE.displayName) {
                        val txCal = Calendar.getInstance().apply { time = tx.date }
                        if (txCal.get(Calendar.YEAR) == currentYear && txCal.get(Calendar.MONTH) == currentMonth) {
                            currentExpense += tx.amount
                            count++
                        }
                    }
                }

                val dailyRate = if (currentDay > 0) currentExpense / currentDay else 0.0
                val remainingDays = (totalDays - currentDay).coerceAtLeast(0)
                val projected = currentExpense + (dailyRate * remainingDays)
                val baseCurrency = try { UserPreferencesRepository(context).baseCurrency.first() } catch (e: Exception) { "PHP" }
                val monthLabel = "${monthNames[currentMonth]} $currentYear"

                withContext(Dispatchers.Main) {
                    for (appWidgetId in appWidgetIds) {
                        val views = RemoteViews(context.packageName, R.layout.widget_monthly_trends).apply {
                            setTextViewText(R.id.tv_trends_month_label, monthLabel)
                            setTextViewText(R.id.tv_trends_spent, CurrencyUtils.formatCurrency(currentExpense, baseCurrency))
                            setTextViewText(R.id.tv_trends_projected, "Proj: ${CurrencyUtils.formatCompact(projected, baseCurrency)}")
                            setTextViewText(R.id.tv_trends_delta, "Daily Run Rate: ~${CurrencyUtils.formatCurrency(dailyRate, baseCurrency)}/day")
                            setTextViewText(R.id.tv_trends_count, "$count Cleared Transactions")

                            setOnClickPendingIntent(R.id.widget_root, pendingAppIntent)
                            setOnClickPendingIntent(R.id.btn_open_trends, pendingAppIntent)
                        }
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
            }
        }
    }
}
