package com.example.data.local.model

/**
 * DashboardCardType: Enumerates all modular cards available on the customizable dashboard.
 */
enum class DashboardCardType(
    val title: String,
    val description: String,
    val defaultEnabled: Boolean = true
) {
    TOTAL_BALANCE(
        title = "Total Balance",
        description = "Total liquid balance and active account count overview",
        defaultEnabled = true
    ),
    MONEY_FLOW(
        title = "Money Flow",
        description = "Income vs expense breakdown and net cash flow gauge",
        defaultEnabled = true
    ),
    BUDGET_PROGRESS(
        title = "Budget Consumption",
        description = "Circular visualizer tracking progress against active monthly budgets",
        defaultEnabled = true
    ),
    DEBT_SUMMARY(
        title = "Receivables & Debts",
        description = "Top receivables summary of who owes you money",
        defaultEnabled = true
    ),
    LIQUID_VS_LOCKED(
        title = "Liquid vs Locked",
        description = "Spendable liquid funds vs locked savings and time-deposits",
        defaultEnabled = true
    ),
    PROJECT_TRACKER(
        title = "Project / Label Tracker",
        description = "Net balance and performance for tagged initiatives (#Project, #Client)",
        defaultEnabled = true
    ),
    LONG_TERM_EXPECTED(
        title = "Long-Term Expected",
        description = "Upcoming delayed receivables, allowances, or projected payouts",
        defaultEnabled = true
    ),
    WEEKLY_FORECAST(
        title = "7-Day Weekly Sparkline",
        description = "Minimalist sparkline chart showing projected cash flow over next 7 days",
        defaultEnabled = true
    ),
    UPCOMING_BILLS(
        title = "Upcoming Bills & Subs",
        description = "Next active bills, recurring subscriptions, and due dates",
        defaultEnabled = true
    ),
    SPENDING_CATEGORIES(
        title = "Top Spending Categories",
        description = "Top expense categories breakdown and distribution",
        defaultEnabled = true
    ),
    SAVINGS_RATE(
        title = "Savings Rate Gauge",
        description = "Percentage of income saved vs spent this month",
        defaultEnabled = true
    ),
    SPENDING_TRENDS(
        title = "Monthly Spending Trends",
        description = "Interactive 6-month multi-bar and trajectory chart comparing monthly expenses & income",
        defaultEnabled = true
    );

    companion object {
        val DEFAULT_CARDS: Set<DashboardCardType> = entries.filter { it.defaultEnabled }.toSet()

        fun fromString(name: String): DashboardCardType? =
            entries.find { it.name.equals(name, ignoreCase = true) }
    }
}
