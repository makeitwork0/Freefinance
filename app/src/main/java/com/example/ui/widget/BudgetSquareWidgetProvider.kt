package com.example.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.model.BudgetPeriod
import com.example.data.local.model.TransactionType
import com.example.data.preferences.UserPreferencesRepository
import com.example.util.CurrencyUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class BudgetSquareWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
            val pendingBudgetsIntent = FinanceAppWidgetManager.getAppOpenPendingIntent(context, destination = "budgets")

            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val budgets = try { db.budgetDao().getAllBudgets().first() } catch (e: Exception) { emptyList() }
                val totalBudgetLimit = budgets.filter { it.period == BudgetPeriod.MONTHLY.displayName || it.period == "Monthly" }.sumOf { it.amountLimit }

                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val currentMonth = calendar.get(Calendar.MONTH)

                val transactions = try { db.transactionDao().getAllTransactions().first() } catch (e: Exception) { emptyList() }
                var totalSpent = 0.0

                for (tx in transactions) {
                    if (tx.status == "Cleared" && tx.type == TransactionType.EXPENSE.displayName) {
                        val txCal = Calendar.getInstance().apply { time = tx.date }
                        if (txCal.get(Calendar.YEAR) == currentYear && txCal.get(Calendar.MONTH) == currentMonth) {
                            totalSpent += tx.amount
                        }
                    }
                }

                val remaining = (totalBudgetLimit - totalSpent).coerceAtLeast(0.0)
                val percent = if (totalBudgetLimit > 0) ((totalSpent / totalBudgetLimit) * 100).toInt() else 0
                val baseCurrency = try { UserPreferencesRepository(context).baseCurrency.first() } catch (e: Exception) { "PHP" }

                withContext(Dispatchers.Main) {
                    for (widgetId in appWidgetIds) {
                        val views = RemoteViews(context.packageName, R.layout.widget_budget_square).apply {
                            setTextViewText(R.id.tv_budget_pct, "$percent%")
                            setTextViewText(R.id.tv_budget_remaining, "${CurrencyUtils.formatCurrency(remaining, baseCurrency)} Left")
                            setTextViewText(
                                R.id.tv_budget_spent_summary,
                                "Spent ${CurrencyUtils.formatCompact(totalSpent, baseCurrency)} / ${CurrencyUtils.formatCompact(totalBudgetLimit, baseCurrency)}"
                            )

                            setOnClickPendingIntent(R.id.widget_root, pendingBudgetsIntent)
                            setOnClickPendingIntent(R.id.btn_open_budget, pendingBudgetsIntent)
                        }
                        appWidgetManager.updateAppWidget(widgetId, views)
                    }
                }
            }
        }
    }
}
