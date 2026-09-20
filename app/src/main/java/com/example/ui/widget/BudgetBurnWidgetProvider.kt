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
import java.util.Locale

class BudgetBurnWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
            val pendingBudgetsIntent = FinanceAppWidgetManager.getAppOpenPendingIntent(context, destination = "BUDGETS")
            val pendingAppIntent = FinanceAppWidgetManager.getAppOpenPendingIntent(context)

            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val budgets = try { db.budgetDao().getAllBudgets().first() } catch (e: Exception) { emptyList() }
                val totalBudget = budgets.filter { it.period == BudgetPeriod.MONTHLY.displayName || it.period == "Monthly" }.sumOf { it.amountLimit }

                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

                val transactions = try { db.transactionDao().getAllTransactions().first() } catch (e: Exception) { emptyList() }
                var currentMonthSpent = 0.0

                for (tx in transactions) {
                    if (tx.status == "Cleared" && tx.type == TransactionType.EXPENSE.displayName) {
                        val txCal = Calendar.getInstance().apply { time = tx.date }
                        if (txCal.get(Calendar.YEAR) == currentYear && txCal.get(Calendar.MONTH) == currentMonth) {
                            currentMonthSpent += tx.amount
                        }
                    }
                }

                val remaining = (totalBudget - currentMonthSpent).coerceAtLeast(0.0)
                val pct = if (totalBudget > 0) ((currentMonthSpent / totalBudget) * 100).toInt() else 0
                val dailyBurn = if (currentDay > 0) currentMonthSpent / currentDay else 0.0
                val baseCurrency = try { UserPreferencesRepository(context).baseCurrency.first() } catch (e: Exception) { "PHP" }

                withContext(Dispatchers.Main) {
                    for (appWidgetId in appWidgetIds) {
                        val views = RemoteViews(context.packageName, R.layout.widget_budget_burn).apply {
                            setTextViewText(R.id.tv_remaining_allowance, "${CurrencyUtils.formatCurrency(remaining, baseCurrency)} Left")
                            setTextViewText(R.id.tv_budget_pct, "$pct% Spent")
                            setTextViewText(R.id.tv_burn_rate, "Velocity: ${CurrencyUtils.formatCurrency(dailyBurn, baseCurrency)}/day (Day $currentDay)")
                            setTextViewText(R.id.tv_total_spent_budget, "Spent: ${CurrencyUtils.formatCompact(currentMonthSpent, baseCurrency)} / ${CurrencyUtils.formatCompact(totalBudget, baseCurrency)}")

                            setOnClickPendingIntent(R.id.widget_root, pendingAppIntent)
                            setOnClickPendingIntent(R.id.btn_open_budgets, pendingBudgetsIntent)
                        }
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
            }
        }
    }
}
