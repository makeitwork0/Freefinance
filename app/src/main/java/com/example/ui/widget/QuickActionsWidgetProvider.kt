package com.example.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.R

class QuickActionsWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
            val pendingQuickAddIntent = FinanceAppWidgetManager.getAppOpenPendingIntent(context, quickAdd = true)
            val pendingBudgetsIntent = FinanceAppWidgetManager.getAppOpenPendingIntent(context, destination = "BUDGETS")
            val pendingLedgerIntent = FinanceAppWidgetManager.getAppOpenPendingIntent(context, destination = "RECORDS")
            val pendingDebtsIntent = FinanceAppWidgetManager.getAppOpenPendingIntent(context, destination = "DEBTS")
            val pendingAppIntent = FinanceAppWidgetManager.getAppOpenPendingIntent(context)

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_quick_actions).apply {
                    setOnClickPendingIntent(R.id.btn_action_log, pendingQuickAddIntent)
                    setOnClickPendingIntent(R.id.btn_action_budgets, pendingBudgetsIntent)
                    setOnClickPendingIntent(R.id.btn_action_ledger, pendingLedgerIntent)
                    setOnClickPendingIntent(R.id.btn_action_debts, pendingDebtsIntent)
                    setOnClickPendingIntent(R.id.widget_root, pendingAppIntent)
                }
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
