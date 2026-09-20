package com.example.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.R

class QuickActionsSquareWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
            for (widgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_quick_actions_square).apply {
                    setOnClickPendingIntent(
                        R.id.btn_action_add,
                        FinanceAppWidgetManager.getAppOpenPendingIntent(context, quickAdd = true)
                    )
                    setOnClickPendingIntent(
                        R.id.btn_action_budgets,
                        FinanceAppWidgetManager.getAppOpenPendingIntent(context, destination = "budgets")
                    )
                    setOnClickPendingIntent(
                        R.id.btn_action_ledger,
                        FinanceAppWidgetManager.getAppOpenPendingIntent(context, destination = "records")
                    )
                    setOnClickPendingIntent(
                        R.id.btn_action_debts,
                        FinanceAppWidgetManager.getAppOpenPendingIntent(context, destination = "debts")
                    )
                    setOnClickPendingIntent(
                        R.id.widget_root,
                        FinanceAppWidgetManager.getAppOpenPendingIntent(context)
                    )
                }
                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }
}
