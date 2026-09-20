package com.example.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.model.BudgetPeriod
import com.example.data.local.model.TransactionType
import com.example.util.CurrencyUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FinanceAppWidgetManager {

    fun isPinSupported(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
            appWidgetManager?.isRequestPinAppWidgetSupported == true
        } else {
            false
        }
    }

    fun requestPinWidget(context: Context, providerClass: Class<out AppWidgetProvider>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = context.getSystemService(AppWidgetManager::class.java) ?: return false
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                val myProvider = ComponentName(context, providerClass)
                val successIntent = Intent(context, providerClass).apply {
                    action = "com.example.ACTION_WIDGET_PINNED"
                }
                val successPendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    successIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                return appWidgetManager.requestPinAppWidget(myProvider, null, successPendingIntent)
            }
        }
        return false
    }

    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return

        val balanceWidget = ComponentName(context, QuickBalanceWidgetProvider::class.java)
        val balanceIds = appWidgetManager.getAppWidgetIds(balanceWidget)
        if (balanceIds.isNotEmpty()) {
            QuickBalanceWidgetProvider.updateWidgets(context, appWidgetManager, balanceIds)
        }

        val actionsWidget = ComponentName(context, QuickActionsWidgetProvider::class.java)
        val actionIds = appWidgetManager.getAppWidgetIds(actionsWidget)
        if (actionIds.isNotEmpty()) {
            QuickActionsWidgetProvider.updateWidgets(context, appWidgetManager, actionIds)
        }

        val budgetWidget = ComponentName(context, BudgetBurnWidgetProvider::class.java)
        val budgetIds = appWidgetManager.getAppWidgetIds(budgetWidget)
        if (budgetIds.isNotEmpty()) {
            BudgetBurnWidgetProvider.updateWidgets(context, appWidgetManager, budgetIds)
        }

        val trendsWidget = ComponentName(context, MonthlyTrendsWidgetProvider::class.java)
        val trendIds = appWidgetManager.getAppWidgetIds(trendsWidget)
        if (trendIds.isNotEmpty()) {
            MonthlyTrendsWidgetProvider.updateWidgets(context, appWidgetManager, trendIds)
        }

        val balanceSquareWidget = ComponentName(context, BalanceSquareWidgetProvider::class.java)
        val balanceSquareIds = appWidgetManager.getAppWidgetIds(balanceSquareWidget)
        if (balanceSquareIds.isNotEmpty()) {
            BalanceSquareWidgetProvider.updateWidgets(context, appWidgetManager, balanceSquareIds)
        }

        val budgetSquareWidget = ComponentName(context, BudgetSquareWidgetProvider::class.java)
        val budgetSquareIds = appWidgetManager.getAppWidgetIds(budgetSquareWidget)
        if (budgetSquareIds.isNotEmpty()) {
            BudgetSquareWidgetProvider.updateWidgets(context, appWidgetManager, budgetSquareIds)
        }

        val actionsSquareWidget = ComponentName(context, QuickActionsSquareWidgetProvider::class.java)
        val actionsSquareIds = appWidgetManager.getAppWidgetIds(actionsSquareWidget)
        if (actionsSquareIds.isNotEmpty()) {
            QuickActionsSquareWidgetProvider.updateWidgets(context, appWidgetManager, actionsSquareIds)
        }
    }

    fun getAppOpenPendingIntent(
        context: Context,
        destination: String? = null,
        quickAdd: Boolean = false,
        quickPic: Boolean = false
    ): PendingIntent {
        val dest = destination?.uppercase()?.trim()
        val actionName = "com.example.action.OPEN_${dest ?: "DASHBOARD"}_QA_${quickAdd}_QP_${quickPic}"
        val intent = Intent(context, MainActivity::class.java).apply {
            action = actionName
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (dest != null) {
                putExtra("OPEN_DESTINATION", dest)
            }
            putExtra("QUICK_ADD", quickAdd)
            putExtra("QUICK_PIC", quickPic)
        }
        val requestCode = (actionName.hashCode()).let { if (it == Int.MIN_VALUE) 0 else kotlin.math.abs(it) }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
