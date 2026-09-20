package com.example.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.util.CurrencyUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BalanceSquareWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
            val pendingAppIntent = FinanceAppWidgetManager.getAppOpenPendingIntent(context)
            val pendingQuickAddIntent = FinanceAppWidgetManager.getAppOpenPendingIntent(context, quickAdd = true)
            val pendingQuickPicIntent = FinanceAppWidgetManager.getAppOpenPendingIntent(context, destination = "RECEIPTS", quickPic = true)

            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val accounts = try { db.accountDao().getAllAccounts().first() } catch (e: Exception) { emptyList() }
                val totalBalance = accounts.sumOf { it.currentBalance }
                val baseCurrency = try { UserPreferencesRepository(context).baseCurrency.first() } catch (e: Exception) { "PHP" }
                val formattedBalance = CurrencyUtils.formatCurrency(totalBalance, baseCurrency)
                val accountsCount = accounts.size

                withContext(Dispatchers.Main) {
                    for (widgetId in appWidgetIds) {
                        val views = RemoteViews(context.packageName, R.layout.widget_balance_square).apply {
                            setTextViewText(R.id.tv_total_balance, formattedBalance)
                            setTextViewText(R.id.tv_accounts_count, "$accountsCount Accs")
                            setTextViewText(R.id.tv_sub_info, "Unified Net Worth")

                            setOnClickPendingIntent(R.id.widget_root, pendingAppIntent)
                            setOnClickPendingIntent(R.id.btn_quick_pic, pendingQuickPicIntent)
                            setOnClickPendingIntent(R.id.btn_quick_add, pendingQuickAddIntent)
                        }
                        appWidgetManager.updateAppWidget(widgetId, views)
                    }
                }
            }
        }
    }
}
