package com.example.domain.usecase

import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.MilestoneEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.model.CompoundingFrequency
import com.example.data.local.model.DailyBalance
import com.example.data.local.model.RecurrenceRule
import com.example.data.local.model.TransactionStatus
import com.example.data.local.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Clean Architecture UseCase: Computes day-by-day cash flow projections for the next N days.
 * Incorporates discrete planned transactions, projected recurrences, pending freelance milestones,
 * and P.A. compound interest yields on active accounts.
 */
class ForecastCashFlowUseCase {

    private val labelDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

    fun execute(
        startingBalance: Double,
        scheduledTransactions: List<TransactionEntity>,
        pendingMilestones: List<MilestoneEntity> = emptyList(),
        accounts: List<AccountEntity> = emptyList(),
        daysAhead: Int = 30
    ): List<DailyBalance> {
        val forecastList = mutableListOf<DailyBalance>()
        var runningBalance = startingBalance

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        for (dayIndex in 0 until daysAhead) {
            val targetDate = calendar.time
            val targetCal = Calendar.getInstance().apply { time = targetDate }

            var dailyNetChange = 0.0

            // 1. Transactions (Planned & Recurring)
            for (tx in scheduledTransactions) {
                if (occursOnDate(tx, targetCal)) {
                    val delta = when (tx.type) {
                        TransactionType.INCOME.displayName -> tx.amount
                        TransactionType.EXPENSE.displayName -> -tx.amount
                        else -> 0.0 // Internal transfers do not alter aggregate net worth
                    }
                    dailyNetChange += delta
                }
            }

            // 2. Pending Freelance Milestones (inject into balance on expected_date)
            for (milestone in pendingMilestones) {
                if (milestone.status.equals("Pending", ignoreCase = true)) {
                    val mCal = Calendar.getInstance().apply { time = milestone.expectedDate }
                    if (isSameDay(mCal, targetCal)) {
                        dailyNetChange += milestone.amount
                    }
                }
            }

            // 3. Predicted Interest Yield Accrual (P.A. from active accounts)
            var dailyAccruedInterest = 0.0
            for (account in accounts) {
                if (account.interestRatePa > 0f && account.currentBalance > 0.0) {
                    val annualRate = account.interestRatePa / 100.0
                    val dailyRate = when (account.compoundingFrequency) {
                        CompoundingFrequency.NONE -> 0.0
                        CompoundingFrequency.DAILY, CompoundingFrequency.MONTHLY, CompoundingFrequency.ANNUALLY -> {
                            annualRate / 365.0
                        }
                    }
                    dailyAccruedInterest += account.currentBalance * dailyRate
                }
            }
            dailyNetChange += dailyAccruedInterest

            runningBalance += dailyNetChange

            forecastList.add(
                DailyBalance(
                    date = targetDate,
                    dayLabel = labelDateFormat.format(targetDate),
                    projectedBalance = runningBalance,
                    dailyNetChange = dailyNetChange
                )
            )

            // Advance by 1 day
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return forecastList
    }

    private fun occursOnDate(tx: TransactionEntity, targetCal: Calendar): Boolean {
        val txCal = Calendar.getInstance().apply { time = tx.date }
        txCal.set(Calendar.HOUR_OF_DAY, 0)
        txCal.set(Calendar.MINUTE, 0)
        txCal.set(Calendar.SECOND, 0)
        txCal.set(Calendar.MILLISECOND, 0)

        // Target date must be on or after original transaction date
        if (targetCal.before(txCal)) return false

        val rule = RecurrenceRule.fromString(tx.recurrenceRule)

        return when {
            // One-off Planned transaction matching the exact calendar date
            rule == RecurrenceRule.NONE && tx.status.equals(TransactionStatus.PLANNED.displayName, ignoreCase = true) -> {
                isSameDay(txCal, targetCal)
            }
            // Daily recurring rule
            rule == RecurrenceRule.DAILY -> true
            // Weekly recurring rule
            rule == RecurrenceRule.WEEKLY -> {
                txCal.get(Calendar.DAY_OF_WEEK) == targetCal.get(Calendar.DAY_OF_WEEK)
            }
            // Monthly recurring rule
            rule == RecurrenceRule.MONTHLY -> {
                val txDayOfMonth = txCal.get(Calendar.DAY_OF_MONTH)
                val maxDayOfTargetMonth = targetCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                val targetDayOfMonth = targetCal.get(Calendar.DAY_OF_MONTH)
                if (txDayOfMonth > maxDayOfTargetMonth) {
                    targetDayOfMonth == maxDayOfTargetMonth
                } else {
                    txDayOfMonth == targetDayOfMonth
                }
            }
            else -> false
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
