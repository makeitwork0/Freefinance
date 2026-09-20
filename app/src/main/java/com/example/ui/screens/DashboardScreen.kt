package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.model.DashboardCardType
import com.example.data.local.model.TransactionType
import com.example.data.preferences.HeroBackgroundStyle
import com.example.ui.components.AddAccountSheet
import com.example.ui.components.AddTransactionSheet
import com.example.ui.components.CashFlowForecastChart
import com.example.ui.components.EmptyListPlaceholder
import com.example.ui.components.modular.BudgetPercentCard
import com.example.ui.components.modular.CardDetailModalSheet
import com.example.ui.components.modular.DebtSummaryCard
import com.example.ui.components.modular.LiquidVsLockedCard
import com.example.ui.components.modular.LongTermExpectedCard
import com.example.ui.components.modular.MoneyFlowCard
import com.example.ui.components.modular.MonthlySpendingTrendsCard
import com.example.ui.components.modular.ProjectTrackerCard
import com.example.ui.components.modular.SavingsRateCard
import com.example.ui.components.modular.SpendingCategoriesCard
import com.example.ui.components.modular.UpcomingBillsCard
import com.example.ui.components.modular.WeeklyForecastCard
import com.example.util.BankAppLauncher
import com.example.util.CurrencyUtils
import com.example.viewmodels.DashboardEvent
import com.example.viewmodels.DashboardViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToAccountLog: (Long) -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToDebts: () -> Unit = {},
    onNavigateToCustomize: () -> Unit = {},
    onNavigateToCommission: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToBudgets: () -> Unit = {},
    onNavigateToSubscriptions: () -> Unit = {},
    onNavigateToRecords: () -> Unit = {},
    onNavigateToWidgets: () -> Unit = {},
    onNavigateToReceiptInbox: () -> Unit = {},
    onNavigateToAssets: () -> Unit = {},
    initialOpenQuickAdd: Boolean = false,
    onQuickAddHandled: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val activeCards by viewModel.activeCards.collectAsState()
    val cardOrder by viewModel.cardOrder.collectAsState()
    val forecastState by viewModel.forecastState.collectAsState()
    val weeklyForecastState by viewModel.weeklyForecastState.collectAsState()
    val budgetState by viewModel.budgetState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val moneyFlowState by viewModel.moneyFlowState.collectAsState()
    val liquidVsLockedState by viewModel.liquidVsLockedState.collectAsState()
    val projectTrackerState by viewModel.projectTrackerState.collectAsState()
    val longTermExpectedState by viewModel.longTermExpectedState.collectAsState()
    val topReceivables by viewModel.topReceivables.collectAsState()
    val totalActiveLent by viewModel.totalActiveLent.collectAsState()
    val heroBackgroundStyle by viewModel.heroBackgroundStyle.collectAsState()
    val recurringTransactions by viewModel.recurringTransactions.collectAsState()
    val categoryExpenses by viewModel.categoryExpenses.collectAsState()
    val monthlySpendingTrends by viewModel.monthlySpendingTrends.collectAsState()
    val pendingReceiptCount by viewModel.pendingReceiptCount.collectAsState()
    val hideMoney by viewModel.hideMoney.collectAsState()

    var selectedCardDetail by remember { mutableStateOf<DashboardCardType?>(null) }
    var showAddTransactionSheet by remember { mutableStateOf(initialOpenQuickAdd) }
    var showAddAccountSheet by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(initialOpenQuickAdd) {
        if (initialOpenQuickAdd) {
            showAddTransactionSheet = true
            onQuickAddHandled()
        }
    }

    // JSON Export / Import Launchers
    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportStateToJson(context, uri)
        }
    }

    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importStateFromJson(context, uri)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is DashboardEvent.TransactionLogged -> snackbarHostState.showSnackbar(event.message)
                is DashboardEvent.DebtUpdated -> snackbarHostState.showSnackbar(event.message)
                is DashboardEvent.AccountCreated -> snackbarHostState.showSnackbar(event.message)
                is DashboardEvent.ProjectUpdated -> snackbarHostState.showSnackbar(event.message)
                is DashboardEvent.BudgetUpdated -> snackbarHostState.showSnackbar(event.message)
                is DashboardEvent.SubscriptionUpdated -> snackbarHostState.showSnackbar(event.message)
                is DashboardEvent.CurrencyUpdated -> snackbarHostState.showSnackbar(event.message)
                is DashboardEvent.ReceiptSnapped -> snackbarHostState.showSnackbar(event.message)
                is DashboardEvent.ReceiptConverted -> snackbarHostState.showSnackbar(event.message)
                is DashboardEvent.ReceiptDiscarded -> snackbarHostState.showSnackbar(event.message)
                is DashboardEvent.AssetUpdated -> snackbarHostState.showSnackbar(event.message)
                is DashboardEvent.SyncCompleted -> snackbarHostState.showSnackbar(event.message)
                is DashboardEvent.ExportCompleted -> snackbarHostState.showSnackbar(event.message)
                is DashboardEvent.Error -> snackbarHostState.showSnackbar(event.error)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Finance",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("dashboard_hamburger_menu")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onNavigateToSettings()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Receipts Inbox & Review")
                                        if (pendingReceiptCount > 0) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.error
                                            ) {
                                                Text(
                                                    text = "$pendingReceiptCount",
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.PhotoCamera, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onNavigateToReceiptInbox()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Category Manager") },
                                leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onNavigateToCategories()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Assets & Valuables") },
                                leadingIcon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onNavigateToAssets()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Customize Dashboard") },
                                leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onNavigateToCustomize()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Home Screen Widgets") },
                                leadingIcon = { Icon(Icons.Default.Widgets, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onNavigateToWidgets()
                                }
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleHideMoney(!hideMoney) },
                        modifier = Modifier.testTag("dashboard_toggle_hide_money")
                    ) {
                        Icon(
                            imageVector = if (hideMoney) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (hideMoney) "Show Balances" else "Hide Balances",
                            tint = if (hideMoney) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onNavigateToReceiptInbox,
                        modifier = Modifier.testTag("btn_top_receipts_inbox")
                    ) {
                        if (pendingReceiptCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text("$pendingReceiptCount")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Receipt Inbox",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Receipt Inbox",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = { showAddTransactionSheet = true },
                        modifier = Modifier.testTag("dashboard_quick_add_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Log Entry",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = onNavigateToReceiptInbox,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("dashboard_fab_quick_pic")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Quick Pic", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Quick Pic", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    }
                }

                ExtendedFloatingActionButton(
                    onClick = { showAddTransactionSheet = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Quick Add") },
                    text = { Text("Log Entry", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("dashboard_fab_log_entry")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val accountMap = remember(uiState.accounts) { uiState.accounts.associateBy { it.id } }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // -------------------------------------------------------------
                // PERMANENT HERO ITEM 1: Current Total Wealth Header (Pinned)
                // -------------------------------------------------------------
                item(key = "PERMANENT_HERO_TOTAL_BALANCE", contentType = "hero") {
                    TotalBalanceHeroCard(
                        totalBalance = uiState.totalBalance,
                        baseCurrency = uiState.baseCurrency,
                        accountCount = uiState.accounts.size,
                        onAddAccountClick = { showAddAccountSheet = true },
                        onNavigateToAssets = onNavigateToAssets,
                        backgroundStyle = heroBackgroundStyle,
                        hideMoney = hideMoney,
                        onClick = { selectedCardDetail = DashboardCardType.TOTAL_BALANCE },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // -------------------------------------------------------------
                // PERMANENT HERO ITEM 2: Accounts Breakdown Row (Modes of Cash)
                // -------------------------------------------------------------
                item(key = "PERMANENT_ACCOUNTS_BREAKDOWN", contentType = "accounts_grid") {
                    AccountsBreakdownRow(
                        accounts = uiState.accounts,
                        hideMoney = hideMoney,
                        onAddAccountClick = { showAddAccountSheet = true },
                        onAccountClick = onNavigateToAccountLog
                    )
                }

                // -------------------------------------------------------------
                // QUICK NAVIGATION GRID: Always visible at all times, no scrolling needed
                // -------------------------------------------------------------
                item(key = "QUICK_NAVIGATION_GRID", contentType = "nav_grid") {
                    DashboardNavigationGrid(
                        onNavigateToReceiptInbox = onNavigateToReceiptInbox,
                        pendingReceiptCount = pendingReceiptCount,
                        onNavigateToCategories = onNavigateToCategories,
                        onNavigateToRecords = onNavigateToRecords,
                        onNavigateToBudgets = onNavigateToBudgets,
                        onNavigateToDebts = onNavigateToDebts,
                        onNavigateToCommission = onNavigateToCommission,
                        onNavigateToSubscriptions = onNavigateToSubscriptions,
                        onNavigateToAssets = onNavigateToAssets
                    )
                }

                // -------------------------------------------------------------
                // MODULAR CARDS: Dynamically rendered based on activeCards & custom cardOrder
                // -------------------------------------------------------------
                cardOrder.forEach { cardType ->
                    if (cardType != DashboardCardType.TOTAL_BALANCE && activeCards.contains(cardType)) {
                        when (cardType) {
                            DashboardCardType.TOTAL_BALANCE -> Unit // Already rendered as Hero
                            DashboardCardType.MONEY_FLOW -> {
                                item(key = cardType.name, contentType = "modular_card") {
                                    MoneyFlowCard(
                                        moneyFlow = moneyFlowState,
                                        currencyCode = uiState.baseCurrency,
                                        onClick = { selectedCardDetail = DashboardCardType.MONEY_FLOW },
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                            DashboardCardType.BUDGET_PROGRESS -> {
                                item(key = cardType.name, contentType = "modular_card") {
                                    BudgetPercentCard(
                                        budgets = budgetState,
                                        currencyCode = uiState.baseCurrency,
                                        onClick = { selectedCardDetail = DashboardCardType.BUDGET_PROGRESS },
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                            DashboardCardType.DEBT_SUMMARY -> {
                                item(key = cardType.name, contentType = "modular_card") {
                                    DebtSummaryCard(
                                        topReceivables = topReceivables,
                                        totalLent = totalActiveLent,
                                        currencyCode = uiState.baseCurrency,
                                        onViewDebtsClicked = onNavigateToDebts,
                                        onClick = { selectedCardDetail = DashboardCardType.DEBT_SUMMARY },
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                            DashboardCardType.LIQUID_VS_LOCKED -> {
                                item(key = cardType.name, contentType = "modular_card") {
                                    LiquidVsLockedCard(
                                        state = liquidVsLockedState,
                                        currencyCode = uiState.baseCurrency,
                                        onClick = { selectedCardDetail = DashboardCardType.LIQUID_VS_LOCKED },
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                            DashboardCardType.PROJECT_TRACKER -> {
                                item(key = cardType.name, contentType = "modular_card") {
                                    ProjectTrackerCard(
                                        projectState = projectTrackerState,
                                        currencyCode = uiState.baseCurrency,
                                        onClick = { selectedCardDetail = DashboardCardType.PROJECT_TRACKER },
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                            DashboardCardType.LONG_TERM_EXPECTED -> {
                                item(key = cardType.name, contentType = "modular_card") {
                                    LongTermExpectedCard(
                                        expectedItems = longTermExpectedState,
                                        currencyCode = uiState.baseCurrency,
                                        onClick = { selectedCardDetail = DashboardCardType.LONG_TERM_EXPECTED },
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                            DashboardCardType.WEEKLY_FORECAST -> {
                                item(key = cardType.name, contentType = "modular_card") {
                                    WeeklyForecastCard(
                                        weeklyBalances = weeklyForecastState,
                                        currencyCode = uiState.baseCurrency,
                                        onClick = { selectedCardDetail = DashboardCardType.WEEKLY_FORECAST },
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                            DashboardCardType.UPCOMING_BILLS -> {
                                item(key = cardType.name, contentType = "modular_card") {
                                    UpcomingBillsCard(
                                        recurringBills = recurringTransactions,
                                        currencyCode = uiState.baseCurrency,
                                        onViewBillsClicked = onNavigateToSubscriptions,
                                        onClick = { selectedCardDetail = DashboardCardType.UPCOMING_BILLS },
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                            DashboardCardType.SPENDING_CATEGORIES -> {
                                item(key = cardType.name, contentType = "modular_card") {
                                    SpendingCategoriesCard(
                                        categoryExpenses = categoryExpenses,
                                        currencyCode = uiState.baseCurrency,
                                        onViewCategoriesClicked = onNavigateToCategories,
                                        onClick = { selectedCardDetail = DashboardCardType.SPENDING_CATEGORIES },
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                            DashboardCardType.SAVINGS_RATE -> {
                                item(key = cardType.name, contentType = "modular_card") {
                                    SavingsRateCard(
                                        moneyFlow = moneyFlowState,
                                        currencyCode = uiState.baseCurrency,
                                        onClick = { selectedCardDetail = DashboardCardType.SAVINGS_RATE },
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                            DashboardCardType.SPENDING_TRENDS -> {
                                item(key = cardType.name, contentType = "modular_card") {
                                    MonthlySpendingTrendsCard(
                                        trends = monthlySpendingTrends,
                                        currencyCode = uiState.baseCurrency,
                                        onClick = { selectedCardDetail = DashboardCardType.SPENDING_TRENDS },
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 30-Day Comprehensive Trajectory Chart
                item(key = "CASH_FLOW_FORECAST_CHART", contentType = "chart") {
                    CashFlowForecastChart(
                        dailyBalances = forecastState,
                        currencyCode = uiState.baseCurrency,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Recent Transactions Section Header
                item(key = "RECENT_TRANSACTIONS_HEADER", contentType = "header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RECENT TRANSACTIONS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        TextButton(onClick = onNavigateToRecords) {
                            Text("See All Records", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 12. Recent Transactions List (Starts zero-mock empty)
                if (uiState.recentTransactions.isEmpty()) {
                    item(key = "EMPTY_RECENT_TRANSACTIONS", contentType = "placeholder") {
                        EmptyListPlaceholder(
                            icon = Icons.Default.ReceiptLong,
                            title = "No Transactions Logged Yet",
                            message = "Tap '+ Log Entry' to record your first income, expense, or transfer. All data starts 100% clean and offline.",
                            actionLabel = "Log Entry",
                            onAction = { showAddTransactionSheet = true },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    items(
                        items = uiState.recentTransactions,
                        key = { it.id },
                        contentType = { "transaction_item" }
                    ) { tx ->
                        val account = accountMap[tx.accountId]
                        val accountName = account?.name ?: "Account"
                        val currencyCode = account?.currencyCode ?: uiState.baseCurrency
                        TransactionListItem(
                            transaction = tx,
                            accountName = accountName,
                            currencyCode = currencyCode,
                            hideMoney = hideMoney,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showAddTransactionSheet) {
        AddTransactionSheet(
            accounts = uiState.accounts,
            categories = categories,
            baseCurrency = uiState.baseCurrency,
            onDismissRequest = { showAddTransactionSheet = false },
            onSaveTransaction = { amount, type, accountId, toAccountId, categoryId, labels, note ->
                viewModel.addQuickTransaction(
                    amount = amount,
                    type = type,
                    accountId = accountId,
                    toAccountId = toAccountId,
                    categoryId = categoryId,
                    labels = labels,
                    note = note
                )
                showAddTransactionSheet = false
            }
        )
    }

    if (showAddAccountSheet) {
        AddAccountSheet(
            defaultCurrency = uiState.baseCurrency,
            onDismissRequest = { showAddAccountSheet = false },
            onSaveAccount = { name, type, initialBalance, color, interestRate, compFreq, currencyCode, isLocked, lockedUntil, linkedAppPackage ->
                viewModel.addAccount(
                    name = name,
                    type = type,
                    initialBalance = initialBalance,
                    color = color,
                    interestRatePa = interestRate,
                    compoundingFrequency = compFreq,
                    currencyCode = currencyCode,
                    isLocked = isLocked,
                    lockedUntil = lockedUntil,
                    linkedAppPackage = linkedAppPackage
                )
                showAddAccountSheet = false
            }
        )
    }

    selectedCardDetail?.let { card ->
        CardDetailModalSheet(
            title = card.title,
            categoryTag = card.name.replace("_", " "),
            onDismissRequest = { selectedCardDetail = null }
        ) {
            when (card) {
                DashboardCardType.TOTAL_BALANCE -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Unified Net Assets: ${CurrencyUtils.formatCurrency(uiState.totalBalance, uiState.baseCurrency)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "You currently have ${uiState.accounts.size} active Cash Modes configured. All amounts are calculated dynamically from your verified cleared ledger.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        uiState.accounts.forEach { acc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = acc.name, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${acc.type} • ${acc.currencyCode}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = CurrencyUtils.formatCurrency(acc.currentBalance, acc.currencyCode),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                DashboardCardType.MONEY_FLOW -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Cash Inflow & Outflow Analytics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Total Income: +${CurrencyUtils.formatCurrency(moneyFlowState.totalIncome, uiState.baseCurrency)}\nTotal Expenses: -${CurrencyUtils.formatCurrency(moneyFlowState.totalExpense, uiState.baseCurrency)}\nNet Position: ${if (moneyFlowState.netCashFlow >= 0) "+" else ""}${CurrencyUtils.formatCurrency(moneyFlowState.netCashFlow, uiState.baseCurrency)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                selectedCardDetail = null
                                onNavigateToRecords()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Open Full Transaction Ledger →", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                DashboardCardType.BUDGET_PROGRESS -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Active Budget Limits & Ceilings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (budgetState.isEmpty()) {
                            Text(
                                text = "No category budgets currently established. Setting spending limits helps preserve monthly savings.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            budgetState.forEach { b ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = b.categoryName, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "Spent ${CurrencyUtils.formatCurrency(b.currentSpent, uiState.baseCurrency)} of ${CurrencyUtils.formatCurrency(b.amountLimit, uiState.baseCurrency)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        TextButton(
                            onClick = {
                                selectedCardDetail = null
                                onNavigateToBudgets()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Manage Category Budgets →", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                DashboardCardType.DEBT_SUMMARY -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Receivables & Borrowed Debts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Total Active Lent to Others: ${CurrencyUtils.formatCurrency(totalActiveLent, uiState.baseCurrency)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        topReceivables.forEach { debt ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = debt.personName, fontWeight = FontWeight.Bold)
                                Text(
                                    text = CurrencyUtils.formatCurrency(debt.remainingAmount, uiState.baseCurrency),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00C853)
                                )
                            }
                        }
                        TextButton(
                            onClick = {
                                selectedCardDetail = null
                                onNavigateToDebts()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Open Debts & Loans Manager →", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                DashboardCardType.LIQUID_VS_LOCKED -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Liquidity & Vault Distribution",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• Immediately Spendable: ${CurrencyUtils.formatCurrency(liquidVsLockedState.spendableLiquid, uiState.baseCurrency)}\n• Time-Deposits / Locked Vaults: ${CurrencyUtils.formatCurrency(liquidVsLockedState.lockedSavings, uiState.baseCurrency)}\n• Overall Liquidity: ${(liquidVsLockedState.liquidRatio * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                DashboardCardType.PROJECT_TRACKER -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Tagged Initiative Performance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Initiative Tag: #${projectTrackerState.labelName}\nNet Balance: ${CurrencyUtils.formatCurrency(projectTrackerState.netBalance, uiState.baseCurrency)}\nTotal Entries: ${projectTrackerState.transactionCount}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                DashboardCardType.LONG_TERM_EXPECTED -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Projected Long-Term Inflows",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (longTermExpectedState.isEmpty()) {
                            Text(
                                text = "No delayed receivables or upcoming project milestones scheduled.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            longTermExpectedState.forEach { exp ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = exp.title, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = exp.sourceOrPerson,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "+" + CurrencyUtils.formatCurrency(exp.amount, uiState.baseCurrency),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00C853)
                                    )
                                }
                            }
                        }
                    }
                }
                DashboardCardType.WEEKLY_FORECAST -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "7-Day Projected Trajectory",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Calculated by projecting confirmed recurring bills and scheduled planned revenues against current cleared balances.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        weeklyForecastState.forEach { day ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = day.dayLabel, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = CurrencyUtils.formatCurrency(day.projectedBalance, uiState.baseCurrency),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                DashboardCardType.UPCOMING_BILLS -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Subscriptions & Commitments",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (recurringTransactions.isEmpty()) {
                            Text(
                                text = "No recurring bills or subscriptions scheduled.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            recurringTransactions.forEach { bill ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = bill.note ?: "Bill", fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${bill.recurrenceRule ?: "Monthly"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "-" + CurrencyUtils.formatCurrency(bill.amount, uiState.baseCurrency),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF5252)
                                    )
                                }
                            }
                        }
                        TextButton(
                            onClick = {
                                selectedCardDetail = null
                                onNavigateToSubscriptions()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Manage Subscriptions & Bills →", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                DashboardCardType.SPENDING_CATEGORIES -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Category Expense Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (categoryExpenses.isEmpty()) {
                            Text(
                                text = "No category expense entries logged yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            categoryExpenses.forEach { cat ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = cat.categoryName, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = CurrencyUtils.formatCurrency(cat.totalAmount, uiState.baseCurrency),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        TextButton(
                            onClick = {
                                selectedCardDetail = null
                                onNavigateToCategories()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View All Categories →", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                DashboardCardType.SAVINGS_RATE -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val rate = if (moneyFlowState.totalIncome > 0) {
                            ((moneyFlowState.totalIncome - moneyFlowState.totalExpense) / moneyFlowState.totalIncome * 100)
                        } else 0.0
                        Text(
                            text = "Personal Savings Efficiency",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• Savings Rate: ${String.format(Locale.getDefault(), "%.1f%%", rate)}\n• Gross Earned: ${CurrencyUtils.formatCurrency(moneyFlowState.totalIncome, uiState.baseCurrency)}\n• Gross Spent: ${CurrencyUtils.formatCurrency(moneyFlowState.totalExpense, uiState.baseCurrency)}\n• Retained Capital: ${CurrencyUtils.formatCurrency(moneyFlowState.netCashFlow, uiState.baseCurrency)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                DashboardCardType.SPENDING_TRENDS -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "6-Month Historical Spending Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        monthlySpendingTrends.reversed().forEach { trend ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${trend.monthLabel} ${trend.year}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${trend.transactionCount} transactions · ~${CurrencyUtils.formatCurrency(trend.averageDailySpending, uiState.baseCurrency)}/day",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Spent: " + CurrencyUtils.formatCurrency(trend.totalExpense, uiState.baseCurrency),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    if (trend.totalIncome > 0) {
                                        Text(
                                            text = "Earned: " + CurrencyUtils.formatCurrency(trend.totalIncome, uiState.baseCurrency),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF00C853)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Quick Navigation Grid located directly beneath the Accounts Breakdown.
 * All 8 modules (Receipts, Ledger, Budgets, Assets, Categories, Debts, Freelance, Bills) are visible at all times without scrolling.
 */
@Composable
private fun DashboardNavigationGrid(
    onNavigateToReceiptInbox: () -> Unit,
    pendingReceiptCount: Int,
    onNavigateToCategories: () -> Unit,
    onNavigateToRecords: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToDebts: () -> Unit,
    onNavigateToCommission: () -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    onNavigateToAssets: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickNavCard(
                title = if (pendingReceiptCount > 0) "Receipts ($pendingReceiptCount)" else "Receipts",
                icon = Icons.Default.PhotoCamera,
                badgeCount = pendingReceiptCount,
                highlight = pendingReceiptCount > 0,
                onClick = onNavigateToReceiptInbox,
                modifier = Modifier.weight(1f)
            )
            QuickNavCard(
                title = "Ledger",
                icon = Icons.Default.ReceiptLong,
                onClick = onNavigateToRecords,
                modifier = Modifier.weight(1f)
            )
            QuickNavCard(
                title = "Budgets",
                icon = Icons.Default.PieChart,
                onClick = onNavigateToBudgets,
                modifier = Modifier.weight(1f)
            )
            QuickNavCard(
                title = "Assets",
                icon = Icons.Default.Inventory2,
                onClick = onNavigateToAssets,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickNavCard(
                title = "Categories",
                icon = Icons.Default.Category,
                onClick = onNavigateToCategories,
                modifier = Modifier.weight(1f)
            )
            QuickNavCard(
                title = "Debts",
                icon = Icons.Default.People,
                onClick = onNavigateToDebts,
                modifier = Modifier.weight(1f)
            )
            QuickNavCard(
                title = "Freelance",
                icon = Icons.Default.BusinessCenter,
                onClick = onNavigateToCommission,
                modifier = Modifier.weight(1f)
            )
            QuickNavCard(
                title = "Bills",
                icon = Icons.Default.EventRepeat,
                onClick = onNavigateToSubscriptions,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickNavCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
    highlight: Boolean = false
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (highlight)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (highlight) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                fontWeight = FontWeight.Bold,
                color = if (highlight) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

/**
 * Permanent Hero Item 1: Total Balance / Net Worth Card with interactive Asset inclusion toggle.
 */
@Composable
fun TotalBalanceHeroCard(
    totalBalance: Double,
    baseCurrency: String,
    accountCount: Int,
    onAddAccountClick: () -> Unit,
    onNavigateToAssets: (() -> Unit)? = null,
    backgroundStyle: HeroBackgroundStyle = HeroBackgroundStyle.SOLID,
    hideMoney: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isGradient = backgroundStyle != HeroBackgroundStyle.SOLID

    val gradientBrush = remember(backgroundStyle) {
        when (backgroundStyle) {
            HeroBackgroundStyle.GRADIENT -> Brush.linearGradient(
                colors = listOf(Color(0xFF1E3C72), Color(0xFF2A5298), Color(0xFF0072FF))
            )
            HeroBackgroundStyle.SUNSET_GLOW -> Brush.linearGradient(
                colors = listOf(Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121))
            )
            HeroBackgroundStyle.EMERALD_AURORA -> Brush.linearGradient(
                colors = listOf(Color(0xFF0575E6), Color(0xFF00B4DB), Color(0xFF00F260))
            )
            HeroBackgroundStyle.MIDNIGHT_NEBULA -> Brush.linearGradient(
                colors = listOf(Color(0xFF200122), Color(0xFF6f0000), Color(0xFF9013FE))
            )
            HeroBackgroundStyle.GOLDEN_LUXE -> Brush.linearGradient(
                colors = listOf(Color(0xFF3A1C71), Color(0xFFD76D77), Color(0xFFFFAF7B))
            )
            HeroBackgroundStyle.SOLID -> Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
        }
    }

    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!isGradient) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                Color.Transparent
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isGradient) {
                    Modifier
                        .clip(RoundedCornerShape(26.dp))
                        .background(gradientBrush)
                } else Modifier
            )
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        val contentColor = if (isGradient) {
            Color.White
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isGradient)
                                    Color.White.copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wallet,
                            contentDescription = null,
                            tint = if (isGradient) Color.White else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Total Balance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isGradient)
                        Color.White.copy(alpha = 0.25f)
                    else MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = baseCurrency,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isGradient) Color.White else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = CurrencyUtils.formatCurrency(totalBalance, baseCurrency, hideMoney),
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 38.sp),
                fontWeight = FontWeight.ExtraBold,
                color = contentColor
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (accountCount == 0) "No active accounts" else "$accountCount accounts connected",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (onNavigateToAssets != null) {
                        TextButton(
                            onClick = onNavigateToAssets,
                            modifier = Modifier.height(36.dp).testTag("hero_assets_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = if (isGradient) Color.White else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Assets",
                                fontWeight = FontWeight.Bold,
                                color = if (isGradient) Color.White else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    TextButton(
                        onClick = onAddAccountClick,
                        modifier = Modifier.height(36.dp).testTag("hero_add_account_button")
                    ) {
                        Text(
                            text = "+ Add Account",
                            fontWeight = FontWeight.Bold,
                            color = if (isGradient) Color.White else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Permanent Hero Item 2: 2-Column Grid of Clickable Colored Account Cards (No-Scroll Mode).
 */
@Composable
fun AccountsBreakdownRow(
    accounts: List<AccountEntity>,
    hideMoney: Boolean = false,
    onAddAccountClick: () -> Unit,
    onAccountClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val chunkedAccounts = remember(accounts) { accounts.chunked(2) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "MODES OF CASH",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                if (accounts.isNotEmpty()) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "${accounts.size}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            TextButton(
                onClick = onAddAccountClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Add Mode",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (accounts.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "No accounts configured",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Add cash, bank, or e-wallet accounts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = onAddAccountClick) {
                        Text("+ Add Account", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chunkedAccounts.forEach { rowAccounts ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowAccounts.forEach { account ->
                            AccountMiniCard(
                                account = account,
                                hideMoney = hideMoney,
                                onClick = { onAccountClick(account.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowAccounts.size == 1) {
                            OutlinedCard(
                                onClick = onAddAccountClick,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(84.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Add Mode",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountMiniCard(
    account: AccountEntity,
    hideMoney: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val matchingApp = remember(account.name, account.linkedAppPackage) {
        BankAppLauncher.findMatchingBankApp(account.name, account.linkedAppPackage)
    }

    val accountIcon: ImageVector = remember(account.type) {
        when (account.type.lowercase(Locale.ROOT)) {
            "cash" -> Icons.Default.Payments
            "bank account", "bank" -> Icons.Default.AccountBalance
            "time deposit", "savings" -> Icons.Default.Savings
            "e-wallet" -> Icons.Default.Smartphone
            "investment" -> Icons.Default.TrendingUp
            else -> Icons.Default.CreditCard
        }
    }

    val cardColor = remember(account.color) { Color(account.color) }
    val formattedBalance = remember(account.currentBalance, account.currencyCode, hideMoney) {
        CurrencyUtils.formatCurrency(account.currentBalance, account.currencyCode, hideMoney)
    }
    val formattedLockDate = remember(account.lockedUntil) {
        account.lockedUntil?.let {
            SimpleDateFormat("MMM d", Locale.getDefault()).format(it)
        }
    }
    val interestText = remember(account.interestRatePa) {
        if (account.interestRatePa > 0f) {
            String.format(Locale.getDefault(), "+%.1f%%", account.interestRatePa)
        } else null
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        modifier = modifier
            .height(84.dp)
            .testTag("account_card_${account.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = accountIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (matchingApp != null || !account.linkedAppPackage.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = Color.Black.copy(alpha = 0.28f),
                            modifier = Modifier.clickable {
                                BankAppLauncher.launchBankApp(context, account.name, account.linkedAppPackage)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Launch,
                                    contentDescription = "Open App",
                                    tint = Color.White,
                                    modifier = Modifier.size(9.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "App",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = Color.Black.copy(alpha = 0.22f)
                    ) {
                        Text(
                            text = account.currencyCode,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Column {
                Text(
                    text = formattedBalance,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = account.type,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color.White.copy(alpha = 0.82f),
                            maxLines = 1
                        )
                        if (account.isLocked || account.lockedUntil != null) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (formattedLockDate != null) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.Black.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "Till $formattedLockDate",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                )
                            }
                        }
                        if (interestText != null) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.White.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = interestText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionListItem(
    transaction: TransactionEntity,
    accountName: String,
    currencyCode: String,
    hideMoney: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isIncome = remember(transaction.type) { transaction.type.equals(TransactionType.INCOME.displayName, ignoreCase = true) }
    val isTransfer = remember(transaction.type) { transaction.type.equals(TransactionType.TRANSFER.displayName, ignoreCase = true) }

    val typeColor = remember(isIncome, isTransfer) {
        when {
            isIncome -> Color(0xFF00C853)
            isTransfer -> Color(0xFF0091EA)
            else -> Color(0xFFD50000)
        }
    }

    val typeIcon: ImageVector = remember(isIncome, isTransfer) {
        when {
            isIncome -> Icons.Default.ArrowDownward
            isTransfer -> Icons.Default.SwapHoriz
            else -> Icons.Default.ArrowUpward
        }
    }

    val formattedDate = remember(transaction.date) {
        SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(transaction.date)
    }

    val formattedAmount = remember(transaction.amount, currencyCode, hideMoney, isIncome, isTransfer) {
        val amt = CurrencyUtils.formatCurrency(transaction.amount, currencyCode, hideMoney)
        if (hideMoney) {
            amt
        } else {
            val sign = when {
                isIncome -> "+"
                isTransfer -> ""
                else -> "-"
            }
            "$sign$amt"
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(typeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.note?.takeIf { it.isNotBlank() } ?: transaction.type,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = accountName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (transaction.labels.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        transaction.labels.forEach { label ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "#$label",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formattedAmount,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = typeColor
                )

                if (transaction.status.equals("Planned", ignoreCase = true)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Planned",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
