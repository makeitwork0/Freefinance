package com.example.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.cloud.CloudBackupManager
import com.example.data.cloud.CloudBackupStatus
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AssetEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.ExchangeRateEntity
import com.example.data.local.entity.FreelanceProjectEntity
import com.example.data.local.entity.MilestoneEntity
import com.example.data.local.entity.ReceiptEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.model.AccountType
import com.example.data.local.model.BudgetPeriod
import com.example.data.local.model.BudgetProgress
import com.example.data.local.model.CategoryExpense
import com.example.data.local.model.CompoundingFrequency
import com.example.data.local.model.DailyBalance
import com.example.data.local.model.DashboardCardType
import com.example.data.local.model.DebtStatus
import com.example.data.local.model.DebtType
import com.example.data.local.model.MonthlyCategorySegment
import com.example.data.local.model.MonthlySpendingTrend
import com.example.data.local.model.ProjectWithMilestones
import com.example.data.local.model.RecurrenceRule
import com.example.data.local.model.TransactionStatus
import com.example.data.local.model.TransactionType
import com.example.data.preferences.HeroBackgroundStyle
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.FinanceRepository
import com.example.data.sync.DataSyncManager
import com.example.domain.usecase.ForecastCashFlowUseCase
import com.example.util.CurrencyUtils
import com.example.util.DataExportUtils
import com.example.util.ReceiptStorageHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

data class DashboardUiState(
    val totalBalance: Double = 0.0,
    val cashBalance: Double = 0.0,
    val assetsBalance: Double = 0.0,
    val includeAssetsInHero: Boolean = true,
    val assetsCount: Int = 0,
    val hideMoney: Boolean = false,
    val baseCurrency: String = "USD",
    val exchangeRates: List<ExchangeRateEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val isLoading: Boolean = false
)

data class MoneyFlowState(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netCashFlow: Double = 0.0
)

data class LiquidVsLockedState(
    val spendableLiquid: Double = 0.0,
    val lockedSavings: Double = 0.0,
    val total: Double = 0.0,
    val liquidRatio: Float = 0.5f
)

data class ProjectTrackerState(
    val labelName: String = "Project",
    val netBalance: Double = 0.0,
    val targetBudget: Double = 1000.0,
    val transactionCount: Int = 0
)

data class ExpectedReceivable(
    val title: String,
    val amount: Double,
    val date: Date,
    val sourceOrPerson: String
)

sealed interface DashboardEvent {
    data class TransactionLogged(val message: String) : DashboardEvent
    data class DebtUpdated(val message: String) : DashboardEvent
    data class ProjectUpdated(val message: String) : DashboardEvent
    data class AccountCreated(val message: String) : DashboardEvent
    data class BudgetUpdated(val message: String) : DashboardEvent
    data class SubscriptionUpdated(val message: String) : DashboardEvent
    data class CurrencyUpdated(val message: String) : DashboardEvent
    data class ReceiptSnapped(val message: String) : DashboardEvent
    data class ReceiptConverted(val message: String) : DashboardEvent
    data class ReceiptDiscarded(val message: String) : DashboardEvent
    data class AssetUpdated(val message: String) : DashboardEvent
    data class SyncCompleted(val message: String) : DashboardEvent
    data class ExportCompleted(val success: Boolean, val message: String) : DashboardEvent
    data class Error(val error: String) : DashboardEvent
}

class DashboardViewModel(
    private val repository: FinanceRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val dataSyncManager: DataSyncManager? = null,
    private val cloudBackupManager: CloudBackupManager = CloudBackupManager(),
    private val forecastUseCase: ForecastCashFlowUseCase = ForecastCashFlowUseCase()
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<DashboardEvent>()
    val eventFlow: SharedFlow<DashboardEvent> = _eventFlow.asSharedFlow()

    val cloudBackupStatus: StateFlow<CloudBackupStatus> = cloudBackupManager.status

    val baseCurrency: StateFlow<String> = preferencesRepository.baseCurrency
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "PHP"
        )

    val allExchangeRates: StateFlow<List<ExchangeRateEntity>> = repository.allExchangeRates
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // 1. DataStore Preferences: Active Modular Dashboard Cards & Custom Order
    val activeCards: StateFlow<Set<DashboardCardType>> = preferencesRepository.activeDashboardCards
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardCardType.DEFAULT_CARDS
        )

    val cardOrder: StateFlow<List<DashboardCardType>> = preferencesRepository.dashboardCardOrder
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardCardType.entries
        )

    fun toggleDashboardCard(card: DashboardCardType, isEnabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.toggleCard(card, isEnabled)
        }
    }

    fun reorderDashboardCards(newOrder: List<DashboardCardType>) {
        viewModelScope.launch {
            preferencesRepository.setCardOrder(newOrder)
        }
    }

    val heroBackgroundStyle: StateFlow<HeroBackgroundStyle> = preferencesRepository.heroBackgroundStyle
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HeroBackgroundStyle.SOLID
        )

    fun setHeroBackgroundStyle(style: HeroBackgroundStyle) {
        viewModelScope.launch {
            preferencesRepository.setHeroBackgroundStyle(style)
        }
    }

    // Phase 8: Data Streams for Budgets, Categories, Subscriptions, and Master Ledger
    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val allBudgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val budgetProgressList: StateFlow<List<BudgetProgress>> = repository.budgetProgressList
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val categoryExpenses: StateFlow<List<CategoryExpense>> = repository.categoryExpenses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val recurringTransactions: StateFlow<List<TransactionEntity>> = repository.recurringTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val allAccounts: StateFlow<List<AccountEntity>> = repository.allAccounts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val allAssets: StateFlow<List<AssetEntity>> = repository.allAssets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val includeAssetsInHero: StateFlow<Boolean> = preferencesRepository.includeAssetsInHero
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true
        )

    fun toggleIncludeAssetsInHero(include: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setIncludeAssetsInHero(include)
        }
    }

    val hideMoney: StateFlow<Boolean> = preferencesRepository.hideMoney
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    fun toggleHideMoney(hide: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setHideMoney(hide)
        }
    }

    // 2. Core Dashboard UI State (Multi-Currency aggregated using Exchange Rates into Base Currency)
    val uiState: StateFlow<DashboardUiState> = combine(
        repository.allAccounts,
        repository.allAssets,
        preferencesRepository.includeAssetsInHero,
        preferencesRepository.baseCurrency,
        repository.allExchangeRates,
        repository.allTransactions,
        preferencesRepository.hideMoney
    ) { args: Array<Any> ->
        @Suppress("UNCHECKED_CAST")
        val accounts = args[0] as List<AccountEntity>
        @Suppress("UNCHECKED_CAST")
        val assets = args[1] as List<AssetEntity>
        val includeAssets = args[2] as Boolean
        val baseCurr = args[3] as String
        @Suppress("UNCHECKED_CAST")
        val rates = args[4] as List<com.example.data.local.entity.ExchangeRateEntity>
        @Suppress("UNCHECKED_CAST")
        val transactions = args[5] as List<TransactionEntity>
        val isHidden = args[6] as Boolean

        val convertedCashBalance = accounts.sumOf { acc ->
            CurrencyUtils.convert(
                amount = acc.currentBalance,
                fromCurrency = acc.currencyCode,
                toCurrency = baseCurr,
                exchangeRates = rates
            )
        }
        val includedAssets = assets.filter { it.includeInNetWorth }
        val convertedAssetsBalance = includedAssets.sumOf { asset ->
            CurrencyUtils.convert(
                amount = asset.estimatedValue,
                fromCurrency = asset.currencyCode,
                toCurrency = baseCurr,
                exchangeRates = rates
            )
        }
        val displayedTotal = if (includeAssets) (convertedCashBalance + convertedAssetsBalance) else convertedCashBalance

        DashboardUiState(
            totalBalance = displayedTotal,
            cashBalance = convertedCashBalance,
            assetsBalance = convertedAssetsBalance,
            includeAssetsInHero = includeAssets,
            assetsCount = assets.size,
            hideMoney = isHidden,
            baseCurrency = baseCurr,
            exchangeRates = rates,
            accounts = accounts,
            recentTransactions = transactions.take(20),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(isLoading = false)
    )

    // 3. 30-Day Cash Flow Forecast (Incorporating P.A. Interest Yields & Pending Freelance Milestones)
    val forecastState: StateFlow<List<DailyBalance>> = combine(
        uiState,
        repository.getPlannedAndRecurringTransactions(),
        repository.pendingMilestones,
        repository.allAccounts
    ) { currentUiState, plannedOrRecurringTxs, pendingMilestones, accounts ->
        forecastUseCase.execute(
            startingBalance = currentUiState.totalBalance,
            scheduledTransactions = plannedOrRecurringTxs,
            pendingMilestones = pendingMilestones,
            accounts = accounts,
            daysAhead = 30
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    // 4. 7-Day Weekly Sparkline Forecast
    val weeklyForecastState: StateFlow<List<DailyBalance>> = combine(
        uiState,
        repository.getPlannedAndRecurringTransactions(),
        repository.pendingMilestones,
        repository.allAccounts
    ) { currentUiState, plannedOrRecurringTxs, pendingMilestones, accounts ->
        forecastUseCase.execute(
            startingBalance = currentUiState.totalBalance,
            scheduledTransactions = plannedOrRecurringTxs,
            pendingMilestones = pendingMilestones,
            accounts = accounts,
            daysAhead = 7
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    // 5. Active Budgets
    val budgetState: StateFlow<List<BudgetProgress>> = repository.budgetProgressList
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // 6. Categories list
    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // 7. Money Flow (Income vs Expense)
    val moneyFlowState: StateFlow<MoneyFlowState> = repository.allTransactions
        .combine(repository.totalClearedNetWorth) { transactions, _ ->
            var income = 0.0
            var expense = 0.0
            for (tx in transactions) {
                if (tx.status == "Cleared") {
                    when (tx.type) {
                        TransactionType.INCOME.displayName -> income += tx.amount
                        TransactionType.EXPENSE.displayName -> expense += tx.amount
                    }
                }
            }
            MoneyFlowState(
                totalIncome = income,
                totalExpense = expense,
                netCashFlow = income - expense
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MoneyFlowState()
        )

    // 8. Liquid vs Locked (Spendable vs Time-Deposits/Vaults)
    val liquidVsLockedState: StateFlow<LiquidVsLockedState> = repository.allAccounts
        .combine(repository.totalClearedNetWorth) { accounts, _ ->
            var spendable = 0.0
            var locked = 0.0
            val now = Date()
            for (acc in accounts) {
                val isCurrentlyLocked = acc.isLocked || 
                    (acc.lockedUntil != null && acc.lockedUntil.after(now)) ||
                    acc.type.equals(AccountType.INVESTMENT.displayName, ignoreCase = true)
                if (isCurrentlyLocked) {
                    locked += acc.currentBalance
                } else {
                    spendable += acc.currentBalance
                }
            }
            val total = spendable + locked
            val ratio = if (total > 0) (spendable / total).toFloat().coerceIn(0f, 1f) else 0.5f
            LiquidVsLockedState(
                spendableLiquid = spendable,
                lockedSavings = locked,
                total = total,
                liquidRatio = ratio
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LiquidVsLockedState()
        )

    // 9. Debt & Receivable Streams
    val topReceivables: StateFlow<List<DebtEntity>> = repository.topReceivables
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val activeLentDebts: StateFlow<List<DebtEntity>> = repository.lentDebts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val activeBorrowedDebts: StateFlow<List<DebtEntity>> = repository.borrowedDebts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val totalActiveLent: StateFlow<Double> = repository.totalActiveLent
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0.0
        )

    val totalActiveBorrowed: StateFlow<Double> = repository.totalActiveBorrowed
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0.0
        )

    // 10. Project Tracker (Label/Tag aggregation)
    val projectTrackerState: StateFlow<ProjectTrackerState> = repository.allTransactions
        .combine(repository.totalClearedNetWorth) { transactions, _ ->
            var labelName = "Initiative"
            var net = 0.0
            var count = 0
            for (tx in transactions) {
                if (tx.labels.isNotEmpty()) {
                    labelName = tx.labels.first()
                    when (tx.type) {
                        TransactionType.INCOME.displayName -> net += tx.amount
                        TransactionType.EXPENSE.displayName -> net -= tx.amount
                    }
                    count++
                }
            }
            ProjectTrackerState(
                labelName = labelName,
                netBalance = net,
                targetBudget = 1500.0,
                transactionCount = count
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProjectTrackerState()
        )

    // 11. Long-Term Expected (Delayed receivables, planned inflows, and pending freelance milestones)
    val longTermExpectedState: StateFlow<List<ExpectedReceivable>> = combine(
        repository.lentDebts,
        repository.getPlannedAndRecurringTransactions(),
        repository.pendingMilestones
    ) { lentList, plannedTxs, pendingMilestones ->
        val list = mutableListOf<ExpectedReceivable>()
        lentList.forEach { debt ->
            list.add(
                ExpectedReceivable(
                    title = "Receivable from ${debt.personName}",
                    amount = debt.remainingAmount,
                    date = debt.dueDate ?: Date(),
                    sourceOrPerson = debt.personName
                )
            )
        }
        pendingMilestones.forEach { milestone ->
            list.add(
                ExpectedReceivable(
                    title = "Milestone: ${milestone.title}",
                    amount = milestone.amount,
                    date = milestone.expectedDate,
                    sourceOrPerson = "Milestone"
                )
            )
        }
        plannedTxs.filter { it.type == TransactionType.INCOME.displayName }.forEach { planned ->
            list.add(
                ExpectedReceivable(
                    title = planned.note ?: "Expected Inflow",
                    amount = planned.amount,
                    date = planned.date,
                    sourceOrPerson = "Scheduled"
                )
            )
        }
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    // 12. Freelance Projects with Milestones
    val freelanceProjects: StateFlow<List<ProjectWithMilestones>> = repository.allProjectsWithMilestones
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // 13. Monthly Spending Trends (Aggregated 6-Month Inflow & Outflow Trend Data with Category Breakdown & Projection)
    val monthlySpendingTrends: StateFlow<List<MonthlySpendingTrend>> = combine(
        repository.allTransactions,
        repository.allCategories,
        repository.allBudgets,
        preferencesRepository.baseCurrency,
        repository.allExchangeRates
    ) { transactions, categories, budgets, baseCurr, rates ->
        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val categoryMap = categories.associateBy { it.id }
        val totalMonthlyBudget = budgets
            .filter { it.period == BudgetPeriod.MONTHLY.displayName || it.period == "Monthly" }
            .sumOf { it.amountLimit }

        val todayCal = java.util.Calendar.getInstance()
        val currentYear = todayCal.get(java.util.Calendar.YEAR)
        val currentMonth = todayCal.get(java.util.Calendar.MONTH)
        val currentDay = todayCal.get(java.util.Calendar.DAY_OF_MONTH)

        // Generate past 6 months list
        val result = mutableListOf<MonthlySpendingTrend>()
        for (i in 5 downTo 0) {
            val targetCal = java.util.Calendar.getInstance().apply {
                time = java.util.Date()
                add(java.util.Calendar.MONTH, -i)
            }
            val y = targetCal.get(java.util.Calendar.YEAR)
            val m = targetCal.get(java.util.Calendar.MONTH)
            val monthLabel = monthNames[m]
            val isCurrent = (y == currentYear && m == currentMonth)
            val daysInMonth = targetCal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            val daysElapsed = if (isCurrent) currentDay else daysInMonth

            var expense = 0.0
            var income = 0.0
            var count = 0
            val catSpentMap = mutableMapOf<Long?, Double>()

            for (tx in transactions) {
                if (tx.status == "Cleared") {
                    val txCal = java.util.Calendar.getInstance().apply { time = tx.date }
                    if (txCal.get(java.util.Calendar.YEAR) == y && txCal.get(java.util.Calendar.MONTH) == m) {
                        count++
                        when (tx.type) {
                            TransactionType.EXPENSE.displayName -> {
                                expense += tx.amount
                                catSpentMap[tx.categoryId] = (catSpentMap[tx.categoryId] ?: 0.0) + tx.amount
                            }
                            TransactionType.INCOME.displayName -> income += tx.amount
                        }
                    }
                }
            }

            val avgDaily = if (daysElapsed > 0) expense / daysElapsed else 0.0

            // Category Segments for Stacked Bars & Breakdown
            val segments = mutableListOf<MonthlyCategorySegment>()
            if (expense > 0) {
                catSpentMap.forEach { (catId, catAmt) ->
                    val catObj = catId?.let { categoryMap[it] }
                    val catName = catObj?.name ?: "General / Uncategorized"
                    val catColor = catObj?.color ?: 0xFF808080.toInt()
                    val pct = (catAmt / expense).toFloat()
                    segments.add(
                        MonthlyCategorySegment(
                            categoryId = catId,
                            categoryName = catName,
                            color = catColor,
                            amount = catAmt,
                            percentage = pct
                        )
                    )
                }
                segments.sortByDescending { it.amount }
            }

            // Predictive Month-End Spend Projection
            val projectedExpense = if (isCurrent) {
                val remainingDays = (daysInMonth - daysElapsed).coerceAtLeast(0)
                expense + (avgDaily * remainingDays)
            } else {
                expense
            }

            result.add(
                MonthlySpendingTrend(
                    monthLabel = monthLabel,
                    year = y,
                    monthIndex = m,
                    totalExpense = expense,
                    totalIncome = income,
                    transactionCount = count,
                    averageDailySpending = avgDaily,
                    categorySegments = segments,
                    monthlyBudgetLimit = totalMonthlyBudget,
                    projectedMonthEndExpense = projectedExpense,
                    isCurrentMonth = isCurrent,
                    daysElapsed = daysElapsed,
                    totalDaysInMonth = daysInMonth
                )
            )
        }
        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    // --- Actions: Account Creation ---

    fun addAccount(
        name: String,
        type: String,
        initialBalance: Double,
        color: Int,
        interestRatePa: Float = 0.0f,
        compoundingFrequency: CompoundingFrequency = CompoundingFrequency.NONE,
        currencyCode: String = "USD",
        isLocked: Boolean = false,
        lockedUntil: Date? = null,
        linkedAppPackage: String? = null
    ) {
        viewModelScope.launch {
            try {
                val account = AccountEntity(
                    name = name,
                    type = type,
                    initialBalance = initialBalance,
                    currentBalance = initialBalance,
                    color = color,
                    interestRatePa = interestRatePa,
                    compoundingFrequency = compoundingFrequency,
                    currencyCode = currencyCode.uppercase().trim(),
                    isLocked = isLocked,
                    lockedUntil = lockedUntil,
                    linkedAppPackage = linkedAppPackage
                )
                repository.insertAccount(account)
                _eventFlow.emit(DashboardEvent.AccountCreated("Account '$name' created successfully"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to create account"))
            }
        }
    }

    fun updateAccount(account: AccountEntity) {
        viewModelScope.launch {
            try {
                repository.updateAccount(account)
                _eventFlow.emit(DashboardEvent.AccountCreated("Account '${account.name}' updated successfully"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to update account"))
            }
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            try {
                repository.deleteAccount(account)
                _eventFlow.emit(DashboardEvent.AccountCreated("Account '${account.name}' deleted"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to delete account"))
            }
        }
    }

    // --- Actions: Transactions ---

    fun addQuickTransaction(
        amount: Double,
        type: TransactionType,
        accountId: Long,
        toAccountId: Long? = null,
        categoryId: Long? = null,
        labels: List<String> = emptyList(),
        note: String? = null
    ) {
        viewModelScope.launch {
            try {
                repository.addQuickTransaction(
                    amount = amount,
                    type = type,
                    accountId = accountId,
                    toAccountId = toAccountId,
                    categoryId = categoryId,
                    labels = labels,
                    note = note
                )
                val account = allAccounts.value.find { it.id == accountId }
                val currCode = account?.currencyCode ?: preferencesRepository.baseCurrency.first()
                _eventFlow.emit(
                    DashboardEvent.TransactionLogged("${type.displayName} of ${CurrencyUtils.formatCurrency(amount, currCode)} logged successfully")
                )
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to log transaction"))
            }
        }
    }

    // --- Actions: Debts ---

    fun addDebt(
        personName: String,
        type: DebtType,
        amount: Double,
        dueDate: Date? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            try {
                val debt = DebtEntity(
                    personName = personName,
                    type = type.displayName,
                    totalAmount = amount,
                    remainingAmount = amount,
                    dueDate = dueDate,
                    notes = notes
                )
                repository.insertDebt(debt)
                _eventFlow.emit(DashboardEvent.DebtUpdated("Recorded debt for $personName"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to add debt"))
            }
        }
    }

    fun updateDebt(
        debtId: Long,
        personName: String,
        type: DebtType,
        totalAmount: Double,
        remainingAmount: Double,
        dueDate: Date? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            try {
                val existingDebt = repository.getDebtByIdImmediate(debtId)
                if (existingDebt != null) {
                    val updated = existingDebt.copy(
                        personName = personName,
                        type = type.displayName,
                        totalAmount = totalAmount,
                        remainingAmount = remainingAmount,
                        dueDate = dueDate,
                        notes = notes,
                        status = if (remainingAmount <= 0.0001) DebtStatus.SETTLED.displayName else DebtStatus.ACTIVE.displayName
                    )
                    repository.updateDebt(updated)
                    _eventFlow.emit(DashboardEvent.DebtUpdated("Updated debt for $personName"))
                }
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to update debt"))
            }
        }
    }

    fun settleDebt(debtId: Long) {
        viewModelScope.launch {
            try {
                repository.settleDebt(debtId)
                _eventFlow.emit(DashboardEvent.DebtUpdated("Debt marked as settled"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to settle debt"))
            }
        }
    }

    fun payDebtPortion(debtId: Long, paymentAmount: Double) {
        viewModelScope.launch {
            try {
                repository.payDebtPortion(debtId, paymentAmount)
                val currCode = preferencesRepository.baseCurrency.first()
                _eventFlow.emit(DashboardEvent.DebtUpdated("Payment of ${CurrencyUtils.formatCurrency(paymentAmount, currCode)} applied"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to apply payment"))
            }
        }
    }

    fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch {
            try {
                repository.deleteDebt(debt)
                _eventFlow.emit(DashboardEvent.DebtUpdated("Debt record deleted"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to delete debt"))
            }
        }
    }

    // --- Actions: Freelance Projects & Milestones ---

    fun addFreelanceProject(
        projectName: String,
        client: String,
        totalExpectedFee: Double
    ) {
        viewModelScope.launch {
            try {
                val project = FreelanceProjectEntity(
                    projectName = projectName,
                    client = client,
                    totalExpectedFee = totalExpectedFee
                )
                repository.insertProject(project)
                _eventFlow.emit(DashboardEvent.ProjectUpdated("Project '$projectName' added"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to add project"))
            }
        }
    }

    fun addMilestone(
        projectId: Long,
        title: String,
        amount: Double,
        expectedDate: Date
    ) {
        viewModelScope.launch {
            try {
                val milestone = MilestoneEntity(
                    projectId = projectId,
                    title = title,
                    amount = amount,
                    expectedDate = expectedDate
                )
                repository.insertMilestone(milestone)
                _eventFlow.emit(DashboardEvent.ProjectUpdated("Milestone '$title' added"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to add milestone"))
            }
        }
    }

    fun markMilestoneAsPaid(milestoneId: Long, depositAccountId: Long) {
        viewModelScope.launch {
            try {
                repository.markMilestoneAsPaid(milestoneId, depositAccountId)
                _eventFlow.emit(DashboardEvent.ProjectUpdated("Milestone marked as paid & income logged"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to mark milestone as paid"))
            }
        }
    }

    fun deleteFreelanceProject(project: FreelanceProjectEntity) {
        viewModelScope.launch {
            try {
                repository.deleteProject(project)
                _eventFlow.emit(DashboardEvent.ProjectUpdated("Project deleted"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to delete project"))
            }
        }
    }

    fun deleteMilestone(milestone: MilestoneEntity) {
        viewModelScope.launch {
            try {
                repository.deleteMilestone(milestone)
                _eventFlow.emit(DashboardEvent.ProjectUpdated("Milestone deleted"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to delete milestone"))
            }
        }
    }

    fun getTransactionsForAccount(accountId: Long) = repository.getTransactionsForAccount(accountId)

    // --- Actions: Categories ---

    fun addCategory(
        name: String,
        type: String,
        color: Int,
        iconResId: String = "ic_category",
        parentId: Long? = null
    ) {
        viewModelScope.launch {
            try {
                val category = CategoryEntity(
                    name = name.trim(),
                    type = type,
                    color = color,
                    iconResId = iconResId,
                    parentCategoryId = parentId
                )
                repository.insertCategory(category)
                _eventFlow.emit(DashboardEvent.BudgetUpdated("Category '$name' created"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to create category"))
            }
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            try {
                repository.updateCategory(category)
                _eventFlow.emit(DashboardEvent.BudgetUpdated("Category '${category.name}' updated"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to update category"))
            }
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            try {
                repository.deleteCategory(category)
                _eventFlow.emit(DashboardEvent.BudgetUpdated("Category '${category.name}' deleted"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to delete category"))
            }
        }
    }

    fun restoreDefaultCategories() {
        viewModelScope.launch {
            try {
                val defaultCats = listOf(
                    CategoryEntity(name = "Food & Dining", iconResId = "ic_food", type = "Expense", color = -13447886),
                    CategoryEntity(name = "Transport", iconResId = "ic_transport", type = "Expense", color = -14579213),
                    CategoryEntity(name = "Housing & Rent", iconResId = "ic_home", type = "Expense", color = -6543440),
                    CategoryEntity(name = "Utilities & Bills", iconResId = "ic_utilities", type = "Expense", color = -26624),
                    CategoryEntity(name = "Entertainment", iconResId = "ic_entertainment", type = "Expense", color = -1499549),
                    CategoryEntity(name = "Healthcare", iconResId = "ic_health", type = "Expense", color = -16728876),
                    CategoryEntity(name = "Shopping", iconResId = "ic_shopping", type = "Expense", color = -16744193),
                    CategoryEntity(name = "Education", iconResId = "ic_education", type = "Expense", color = -10597711),
                    CategoryEntity(name = "Salary", iconResId = "ic_salary", type = "Income", color = -11751600),
                    CategoryEntity(name = "Freelance", iconResId = "ic_freelance", type = "Income", color = -16738120),
                    CategoryEntity(name = "Investments", iconResId = "ic_investment", type = "Income", color = -12627531),
                    CategoryEntity(name = "Savings", iconResId = "ic_savings", type = "Income", color = -8599486)
                )
                repository.insertCategories(defaultCats)
                _eventFlow.emit(DashboardEvent.BudgetUpdated("Default categories added successfully"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to restore default categories"))
            }
        }
    }

    // --- Actions: Budgets ---

    fun createBudget(
        categoryId: Long,
        amountLimit: Double,
        period: BudgetPeriod,
        startDate: Date,
        endDate: Date
    ) {
        viewModelScope.launch {
            try {
                val budget = BudgetEntity(
                    categoryId = categoryId,
                    amountLimit = amountLimit,
                    period = period.displayName,
                    startDate = startDate,
                    endDate = endDate
                )
                repository.insertBudget(budget)
                _eventFlow.emit(DashboardEvent.BudgetUpdated("Budget created successfully"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to create budget"))
            }
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            try {
                repository.deleteBudget(budget)
                _eventFlow.emit(DashboardEvent.BudgetUpdated("Budget removed"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to delete budget"))
            }
        }
    }

    // --- Actions: Subscriptions & Bills ---

    fun stopSubscription(transactionId: Long) {
        viewModelScope.launch {
            try {
                repository.stopRecurrence(transactionId)
                _eventFlow.emit(DashboardEvent.SubscriptionUpdated("Subscription recurrence stopped"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to stop subscription"))
            }
        }
    }

    fun addSubscription(
        amount: Double,
        accountId: Long,
        categoryId: Long?,
        recurrenceRule: RecurrenceRule,
        startDate: Date = Date(),
        note: String? = null
    ) {
        addRecurringItem(
            amount = amount,
            type = TransactionType.EXPENSE,
            accountId = accountId,
            categoryId = categoryId,
            recurrenceRule = recurrenceRule,
            startDate = startDate,
            note = note,
            labels = listOf("subscription", "bill")
        )
    }

    fun addRecurringItem(
        amount: Double,
        type: TransactionType,
        accountId: Long,
        categoryId: Long?,
        recurrenceRule: RecurrenceRule,
        startDate: Date = Date(),
        note: String? = null,
        labels: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            try {
                val tx = TransactionEntity(
                    amount = amount,
                    type = type.displayName,
                    date = startDate,
                    status = TransactionStatus.PLANNED.displayName,
                    accountId = accountId,
                    categoryId = categoryId,
                    recurrenceRule = recurrenceRule.displayName,
                    labels = labels,
                    note = note
                )
                repository.insertTransaction(tx)
                _eventFlow.emit(DashboardEvent.SubscriptionUpdated("Recurring schedule '${note ?: type.displayName}' saved"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to add recurring item"))
            }
        }
    }

    fun updateRecurringItem(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                repository.updateTransaction(transaction)
                _eventFlow.emit(DashboardEvent.SubscriptionUpdated("Recurring schedule updated"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to update recurring schedule"))
            }
        }
    }

    fun markRecurringPaidOrReceived(recurringItem: TransactionEntity) {
        viewModelScope.launch {
            try {
                // Log a cleared payment transaction for today
                val type = if (recurringItem.type.equals(TransactionType.INCOME.displayName, ignoreCase = true)) {
                    TransactionType.INCOME
                } else {
                    TransactionType.EXPENSE
                }

                repository.addQuickTransaction(
                    amount = recurringItem.amount,
                    type = type,
                    accountId = recurringItem.accountId,
                    categoryId = recurringItem.categoryId,
                    labels = recurringItem.labels + "recurring_cleared",
                    note = "${recurringItem.note ?: recurringItem.type} (Paid/Settled)"
                )
                _eventFlow.emit(DashboardEvent.TransactionLogged("Logged cleared payment for ${recurringItem.note ?: "recurring item"}"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to log payment"))
            }
        }
    }

    // --- Actions: Master Records Ledger ---

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transaction)
                _eventFlow.emit(DashboardEvent.TransactionLogged("Transaction deleted"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to delete transaction"))
            }
        }
    }

    // --- Actions: CSV Export ---

    fun exportDataToCsv(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val accounts = repository.allAccounts.first()
                val transactions = repository.allTransactions.first()
                val csvContent = DataExportUtils.generateCsv(accounts, transactions)
                val success = DataExportUtils.writeCsvToUri(context, uri, csvContent)
                if (success) {
                    _eventFlow.emit(DashboardEvent.ExportCompleted(true, "Database exported successfully to CSV"))
                } else {
                    _eventFlow.emit(DashboardEvent.ExportCompleted(false, "Failed to write CSV file"))
                }
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.ExportCompleted(false, e.message ?: "Export failed"))
            }
        }
    }

    // --- Actions: Multi-Currency & Exchange Rates ---

    fun setBaseCurrency(currencyCode: String) {
        viewModelScope.launch {
            try {
                preferencesRepository.setBaseCurrency(currencyCode)
                _eventFlow.emit(DashboardEvent.CurrencyUpdated("Base currency set to $currencyCode"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to set base currency"))
            }
        }
    }

    fun addOrUpdateExchangeRate(from: String, to: String, rate: Double) {
        viewModelScope.launch {
            try {
                repository.insertExchangeRate(
                    ExchangeRateEntity(
                        fromCurrency = from.uppercase().trim(),
                        toCurrency = to.uppercase().trim(),
                        rate = rate,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
                _eventFlow.emit(DashboardEvent.CurrencyUpdated("Exchange rate ${from.uppercase()}/${to.uppercase()} updated"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to save exchange rate"))
            }
        }
    }

    fun deleteExchangeRate(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteExchangeRate(id)
                _eventFlow.emit(DashboardEvent.CurrencyUpdated("Exchange rate deleted"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error(e.message ?: "Failed to delete exchange rate"))
            }
        }
    }

    // --- Actions: Universal JSON State Importer & Exporter ---

    fun exportStateToJson(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                if (dataSyncManager == null) {
                    _eventFlow.emit(DashboardEvent.Error("DataSyncManager is not initialized"))
                    return@launch
                }
                val currentBase = preferencesRepository.baseCurrency.first()
                val jsonString = dataSyncManager.exportStateToJson(currentBase)
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(jsonString.toByteArray(Charsets.UTF_8))
                }
                _eventFlow.emit(DashboardEvent.ExportCompleted(true, "Application state exported successfully to JSON"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error("JSON Export failed: ${e.localizedMessage}"))
            }
        }
    }

    fun importStateFromJson(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                if (dataSyncManager == null) {
                    _eventFlow.emit(DashboardEvent.Error("DataSyncManager is not initialized"))
                    return@launch
                }
                val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader(Charsets.UTF_8).readText()
                } ?: throw IllegalArgumentException("Could not read file from selected URI")

                val result = dataSyncManager.importStateFromJson(jsonString)
                if (result.isSuccess) {
                    val count = result.getOrNull() ?: 0
                    _eventFlow.emit(DashboardEvent.SyncCompleted("Successfully imported $count entities into database"))
                } else {
                    _eventFlow.emit(DashboardEvent.Error("Import failed: ${result.exceptionOrNull()?.localizedMessage}"))
                }
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error("Failed to import JSON: ${e.localizedMessage}"))
            }
        }
    }

    // --- Actions: Google Drive Cloud Backup ---

    fun authenticateWithGoogle(context: Context, clientId: String? = null) {
        viewModelScope.launch {
            val result = cloudBackupManager.authenticateWithGoogle(context, clientId)
            if (result.isSuccess) {
                _eventFlow.emit(DashboardEvent.SyncCompleted("Connected as: ${result.getOrNull()}"))
            } else {
                _eventFlow.emit(DashboardEvent.Error("Sign in failed: ${result.exceptionOrNull()?.localizedMessage}"))
            }
        }
    }

    fun backupToCloudDrive(context: Context) {
        viewModelScope.launch {
            val result = cloudBackupManager.backupToDrive(context)
            if (result.isSuccess) {
                _eventFlow.emit(DashboardEvent.SyncCompleted(result.getOrNull() ?: "Backup uploaded successfully"))
            } else {
                _eventFlow.emit(DashboardEvent.Error("Drive backup failed: ${result.exceptionOrNull()?.localizedMessage}"))
            }
        }
    }

    fun restoreFromCloudDrive(context: Context) {
        viewModelScope.launch {
            val result = cloudBackupManager.restoreFromDrive(context)
            if (result.isSuccess) {
                _eventFlow.emit(DashboardEvent.SyncCompleted("Cloud Restore succeeded! Local database refreshed."))
            } else {
                _eventFlow.emit(DashboardEvent.Error("Drive restore failed: ${result.exceptionOrNull()?.localizedMessage}"))
            }
        }
    }

    // Receipts Inbox Streams
    val pendingReceipts: StateFlow<List<ReceiptEntity>> = repository.pendingReceipts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val pendingReceiptCount: StateFlow<Int> = repository.pendingReceiptCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    val allReceipts: StateFlow<List<ReceiptEntity>> = repository.allReceipts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun saveCapturedReceipt(
        imagePath: String,
        note: String? = null,
        suggestedAmount: Double? = null,
        suggestedType: String = "Expense"
    ) {
        viewModelScope.launch {
            try {
                val receipt = ReceiptEntity(
                    imagePath = imagePath,
                    capturedAt = Date(),
                    note = note,
                    suggestedAmount = suggestedAmount,
                    suggestedType = suggestedType,
                    status = "PENDING"
                )
                repository.insertReceipt(receipt)
                _eventFlow.emit(DashboardEvent.ReceiptSnapped("Receipt photo saved to Inbox! Review anytime."))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error("Failed to save receipt: ${e.localizedMessage}"))
            }
        }
    }

    fun convertReceiptToTransaction(
        receiptId: Long,
        accountId: Long,
        amount: Double,
        type: TransactionType,
        categoryId: Long?,
        date: Date,
        note: String?
    ) {
        viewModelScope.launch {
            try {
                repository.convertReceiptToTransaction(
                    receiptId = receiptId,
                    accountId = accountId,
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    date = date,
                    note = note
                )
                _eventFlow.emit(DashboardEvent.ReceiptConverted("Receipt successfully classified and logged as ${type.displayName}!"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error("Failed to convert receipt: ${e.localizedMessage}"))
            }
        }
    }

    fun discardReceipt(receiptId: Long) {
        viewModelScope.launch {
            try {
                repository.discardReceipt(receiptId)
                _eventFlow.emit(DashboardEvent.ReceiptDiscarded("Receipt discarded from Inbox queue."))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error("Failed to discard receipt: ${e.localizedMessage}"))
            }
        }
    }

    fun deleteReceiptPermanently(receipt: ReceiptEntity) {
        viewModelScope.launch {
            try {
                ReceiptStorageHelper.deleteReceiptFile(receipt.imagePath)
                repository.deleteReceipt(receipt)
                _eventFlow.emit(DashboardEvent.ReceiptDiscarded("Receipt permanently deleted."))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error("Failed to delete receipt: ${e.localizedMessage}"))
            }
        }
    }

    // ==========================================
    // Valuables & Physical / Digital Assets CRUD
    // ==========================================

    fun addAsset(
        name: String,
        category: String,
        estimatedValue: Double,
        purchasePrice: Double = 0.0,
        purchaseDate: Date? = null,
        currencyCode: String = "USD",
        notes: String? = null,
        iconName: String = "ic_asset",
        color: Int = -16738120,
        includeInNetWorth: Boolean = true
    ) {
        viewModelScope.launch {
            try {
                val asset = AssetEntity(
                    name = name.trim(),
                    category = category,
                    estimatedValue = estimatedValue,
                    purchasePrice = purchasePrice,
                    purchaseDate = purchaseDate,
                    currencyCode = currencyCode,
                    notes = notes?.trim()?.takeIf { it.isNotBlank() },
                    iconName = iconName,
                    color = color,
                    includeInNetWorth = includeInNetWorth,
                    createdAt = System.currentTimeMillis()
                )
                repository.insertAsset(asset)
                _eventFlow.emit(DashboardEvent.AssetUpdated("Asset added: ${asset.name}"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error("Failed to add asset: ${e.localizedMessage}"))
            }
        }
    }

    fun updateAsset(asset: AssetEntity) {
        viewModelScope.launch {
            try {
                repository.updateAsset(asset)
                _eventFlow.emit(DashboardEvent.AssetUpdated("Asset updated: ${asset.name}"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error("Failed to update asset: ${e.localizedMessage}"))
            }
        }
    }

    fun deleteAsset(asset: AssetEntity) {
        viewModelScope.launch {
            try {
                repository.deleteAsset(asset)
                _eventFlow.emit(DashboardEvent.AssetUpdated("Asset removed: ${asset.name}"))
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error("Failed to remove asset: ${e.localizedMessage}"))
            }
        }
    }

    fun toggleAssetInclusion(asset: AssetEntity, include: Boolean) {
        viewModelScope.launch {
            try {
                repository.updateAsset(asset.copy(includeInNetWorth = include))
                _eventFlow.emit(
                    DashboardEvent.AssetUpdated(
                        if (include) "${asset.name} included in net worth" else "${asset.name} excluded from net worth"
                    )
                )
            } catch (e: Exception) {
                _eventFlow.emit(DashboardEvent.Error("Failed to update asset: ${e.localizedMessage}"))
            }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = AppDatabase.getDatabase(context.applicationContext)
                    val repository = FinanceRepository(
                        accountDao = db.accountDao(),
                        transactionDao = db.transactionDao(),
                        categoryDao = db.categoryDao(),
                        budgetDao = db.budgetDao(),
                        debtDao = db.debtDao(),
                        freelanceDao = db.freelanceDao(),
                        exchangeRateDao = db.exchangeRateDao(),
                        receiptDao = db.receiptDao(),
                        assetDao = db.assetDao()
                    )
                    val preferencesRepository = UserPreferencesRepository(context.applicationContext)
                    val dataSyncManager = DataSyncManager(db)
                    val cloudBackupManager = CloudBackupManager()
                    return DashboardViewModel(
                        repository = repository,
                        preferencesRepository = preferencesRepository,
                        dataSyncManager = dataSyncManager,
                        cloudBackupManager = cloudBackupManager
                    ) as T
                }
            }
    }
}
