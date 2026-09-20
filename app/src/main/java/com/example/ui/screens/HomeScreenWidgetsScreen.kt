package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.ui.widget.BalanceSquareWidgetProvider
import com.example.ui.widget.BudgetBurnWidgetProvider
import com.example.ui.widget.BudgetSquareWidgetProvider
import com.example.ui.widget.FinanceAppWidgetManager
import com.example.ui.widget.MonthlyTrendsWidgetProvider
import com.example.ui.widget.QuickActionsSquareWidgetProvider
import com.example.ui.widget.QuickActionsWidgetProvider
import com.example.ui.widget.QuickBalanceWidgetProvider
import com.example.util.CurrencyUtils
import com.example.viewmodels.DashboardViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenWidgetsScreen(
    viewModel: DashboardViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel.uiState.collectAsState()
    val budgetState by viewModel.budgetState.collectAsState()
    val monthlySpendingTrends by viewModel.monthlySpendingTrends.collectAsState()

    var showManualGuide by remember { mutableStateOf(false) }

    val totalBudget = budgetState.sumOf { it.amountLimit }
    val totalSpent = budgetState.sumOf { it.currentSpent }
    val remainingBudget = (totalBudget - totalSpent).coerceAtLeast(0.0)
    val budgetPct = if (totalBudget > 0) ((totalSpent / totalBudget) * 100).toInt() else 0

    val currentMonthTrend = monthlySpendingTrends.lastOrNull()
    val currentMonthExpense = currentMonthTrend?.totalExpense ?: 0.0
    val projectedMonthExpense = currentMonthTrend?.projectedMonthEndExpense ?: 0.0
    val dailyRunRate = currentMonthTrend?.averageDailySpending ?: 0.0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Home Screen Widgets",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Live Glances & 1-Tap Quick Actions",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("widgets_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            FinanceAppWidgetManager.updateAllWidgets(context)
                            scope.launch {
                                snackbarHostState.showSnackbar("All active home screen widgets refreshed with latest data!")
                            }
                        },
                        modifier = Modifier.testTag("widgets_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Widgets",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Banner
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Widgets,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Instant Financial Control",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Place live interactive widgets onto your Android home screen for one-tap entry logging and real-time net worth tracking.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Widget Item 1: Net Worth & Balance Glance
            item {
                WidgetPreviewItemCard(
                    title = "Net Worth & Quick Balance",
                    gridSize = "3 × 2 Size",
                    description = "Displays unified multi-currency total net worth, active cash modes, and instant shortcut to log new entries.",
                    icon = Icons.Default.TouchApp,
                    accentColor = Color(0xFF448AFF),
                    onPinClick = {
                        val pinned = FinanceAppWidgetManager.requestPinWidget(context, QuickBalanceWidgetProvider::class.java)
                        if (pinned) {
                            scope.launch { snackbarHostState.showSnackbar("Pin request submitted to launcher!") }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("To add, touch & hold your home screen -> tap Widgets -> choose Finance Tracker") }
                        }
                    }
                ) {
                    // Mock Preview of the Net Worth Widget
                    WidgetCardContainer {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL NET WORTH",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9E9EB2),
                                letterSpacing = 1.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF2A2A3C)
                            ) {
                                Text(
                                    text = "${uiState.accounts.size} Accounts",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF82B1FF),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = CurrencyUtils.formatCurrency(uiState.totalBalance, uiState.baseCurrency),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Unified Multi-Currency",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9E9EB2)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF2A2A3C)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color(0xFF69F0AE),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Log Entry",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF69F0AE)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Widget Item 2: 1-Tap Quick Action Bar
            item {
                WidgetPreviewItemCard(
                    title = "1-Tap Quick Action Bar",
                    gridSize = "4 × 1 Size",
                    description = "Compact horizontal speed bar with dedicated triggers for Log Entry, Budgets, Ledger, and Debts.",
                    icon = Icons.Default.DashboardCustomize,
                    accentColor = Color(0xFF00E676),
                    onPinClick = {
                        val pinned = FinanceAppWidgetManager.requestPinWidget(context, QuickActionsWidgetProvider::class.java)
                        if (pinned) {
                            scope.launch { snackbarHostState.showSnackbar("Pin request submitted to launcher!") }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("To add, touch & hold your home screen -> tap Widgets -> choose Finance Tracker") }
                        }
                    }
                ) {
                    // Mock Preview of the Quick Action Bar
                    WidgetCardContainer {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WidgetActionBtn(icon = "➕", label = "Log Entry", modifier = Modifier.weight(1f))
                            WidgetActionBtn(icon = "📊", label = "Budgets", modifier = Modifier.weight(1f))
                            WidgetActionBtn(icon = "📋", label = "Ledger", modifier = Modifier.weight(1f))
                            WidgetActionBtn(icon = "👥", label = "Debts", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Widget Item 3: Budget Burn Pace Meter
            item {
                WidgetPreviewItemCard(
                    title = "Monthly Budget Burn Meter",
                    gridSize = "3 × 2 Size",
                    description = "Real-time burn-rate pace monitor, percentage spent, and remaining monthly spending allowance.",
                    icon = Icons.Default.PieChart,
                    accentColor = Color(0xFFFFB74D),
                    onPinClick = {
                        val pinned = FinanceAppWidgetManager.requestPinWidget(context, BudgetBurnWidgetProvider::class.java)
                        if (pinned) {
                            scope.launch { snackbarHostState.showSnackbar("Pin request submitted to launcher!") }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("To add, touch & hold your home screen -> tap Widgets -> choose Finance Tracker") }
                        }
                    }
                ) {
                    WidgetCardContainer {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MONTHLY BUDGET BURN",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB74D),
                                letterSpacing = 1.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF2A2A3C)
                            ) {
                                Text(
                                    text = "$budgetPct% Spent",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${CurrencyUtils.formatCurrency(remainingBudget, uiState.baseCurrency)} Left",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Velocity: ${CurrencyUtils.formatCurrency(dailyRunRate, uiState.baseCurrency)}/day",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9E9EB2)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Spent: ${CurrencyUtils.formatCompact(totalSpent, uiState.baseCurrency)} / ${CurrencyUtils.formatCompact(totalBudget, uiState.baseCurrency)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFB0BEC5)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF2A2A3C)
                            ) {
                                Text(
                                    text = "Budgets →",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFB74D),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Widget Item 4: Monthly Spending Snapshot
            item {
                WidgetPreviewItemCard(
                    title = "Monthly Spending Trends Snapshot",
                    gridSize = "3 × 2 Size",
                    description = "Trajectory tracker comparing current month's cleared outflows against projected month-end burn rate.",
                    icon = Icons.Default.ShowChart,
                    accentColor = Color(0xFFCE93D8),
                    onPinClick = {
                        val pinned = FinanceAppWidgetManager.requestPinWidget(context, MonthlyTrendsWidgetProvider::class.java)
                        if (pinned) {
                            scope.launch { snackbarHostState.showSnackbar("Pin request submitted to launcher!") }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("To add, touch & hold your home screen -> tap Widgets -> choose Finance Tracker") }
                        }
                    }
                ) {
                    WidgetCardContainer {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MONTHLY SPENDING",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFCE93D8),
                                letterSpacing = 1.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF2A2A3C)
                            ) {
                                Text(
                                    text = currentMonthTrend?.monthLabel ?: "This Month",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE1BEE7),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = CurrencyUtils.formatCurrency(currentMonthExpense, uiState.baseCurrency),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "Proj: ${CurrencyUtils.formatCompact(projectedMonthExpense, uiState.baseCurrency)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFB0BEC5),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Daily Run Rate: ~${CurrencyUtils.formatCurrency(dailyRunRate, uiState.baseCurrency)}/day",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9E9EB2)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${currentMonthTrend?.transactionCount ?: 0} Cleared Entries",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF9E9EB2)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF2A2A3C)
                            ) {
                                Text(
                                    text = "Details →",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFCE93D8),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- 2x2 COMPACT SQUARE WIDGETS SECTION ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "COMPACT 2 × 2 SQUARE WIDGETS",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Square Grid",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Widget Item 5: Compact 2x2 Square Net Worth Widget
            item {
                WidgetPreviewItemCard(
                    title = "Compact Net Worth (2×2)",
                    gridSize = "2 × 2 Square",
                    description = "Compact square widget with live Net Worth, 1-tap 📷 Quick Pic to snap receipts, and + Log for manual entry.",
                    icon = Icons.Default.TouchApp,
                    accentColor = Color(0xFF448AFF),
                    onPinClick = {
                        val pinned = FinanceAppWidgetManager.requestPinWidget(context, BalanceSquareWidgetProvider::class.java)
                        if (pinned) {
                            scope.launch { snackbarHostState.showSnackbar("Pin request submitted to launcher!") }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("To add, touch & hold home screen -> Widgets -> select 2x2 Net Worth") }
                        }
                    }
                ) {
                    WidgetCardContainer {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NET WORTH",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9E9EB2),
                                fontSize = 10.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF2A2A3C)
                            ) {
                                Text(
                                    text = "${uiState.accounts.size} Accs",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF82B1FF),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 9.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyUtils.formatCurrency(uiState.totalBalance, uiState.baseCurrency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Unified Net Worth",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9E9EB2),
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "📷 Quick Pic",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8),
                                    modifier = Modifier
                                        .padding(vertical = 5.dp)
                                        .fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontSize = 10.sp
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF14342B),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "+ Log",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399),
                                    modifier = Modifier
                                        .padding(vertical = 5.dp)
                                        .fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            // Widget Item 6: Compact 2x2 Square Budget Widget
            item {
                WidgetPreviewItemCard(
                    title = "Compact Budget Meter (2×2)",
                    gridSize = "2 × 2 Square",
                    description = "Compact square budget monitor showing current percentage burned and remaining monthly allowance.",
                    icon = Icons.Default.PieChart,
                    accentColor = Color(0xFFFFB74D),
                    onPinClick = {
                        val pinned = FinanceAppWidgetManager.requestPinWidget(context, BudgetSquareWidgetProvider::class.java)
                        if (pinned) {
                            scope.launch { snackbarHostState.showSnackbar("Pin request submitted to launcher!") }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("To add, touch & hold home screen -> Widgets -> select 2x2 Budget") }
                        }
                    }
                ) {
                    WidgetCardContainer {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BUDGET",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB74D),
                                fontSize = 10.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF2A2A3C)
                            ) {
                                Text(
                                    text = "$budgetPct%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 9.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${CurrencyUtils.formatCurrency(remainingBudget, uiState.baseCurrency)} Left",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Spent: ${CurrencyUtils.formatCompact(totalSpent, uiState.baseCurrency)} / ${CurrencyUtils.formatCompact(totalBudget, uiState.baseCurrency)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9E9EB2),
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2A2A3C),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "View Budgets →",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB74D),
                                modifier = Modifier
                                    .padding(vertical = 5.dp)
                                    .fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Widget Item 7: Compact 2x2 Square 4-Grid Quick Actions
            item {
                WidgetPreviewItemCard(
                    title = "Compact 4-Action Grid (2×2)",
                    gridSize = "2 × 2 Square",
                    description = "Four instant quick launcher shortcuts arranged in a 2x2 grid for 1-tap navigation.",
                    icon = Icons.Default.DashboardCustomize,
                    accentColor = Color(0xFF00E676),
                    onPinClick = {
                        val pinned = FinanceAppWidgetManager.requestPinWidget(context, QuickActionsSquareWidgetProvider::class.java)
                        if (pinned) {
                            scope.launch { snackbarHostState.showSnackbar("Pin request submitted to launcher!") }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("To add, touch & hold home screen -> Widgets -> select 2x2 Actions") }
                        }
                    }
                ) {
                    WidgetCardContainer {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF2A2A3C),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("➕", fontSize = 13.sp)
                                        Text("Log", style = MaterialTheme.typography.labelSmall, color = Color(0xFF69F0AE), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF2A2A3C),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("📊", fontSize = 13.sp)
                                        Text("Budgets", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFB74D), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF2A2A3C),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("📋", fontSize = 13.sp)
                                        Text("Ledger", style = MaterialTheme.typography.labelSmall, color = Color(0xFF82B1FF), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF2A2A3C),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("👥", fontSize = 13.sp)
                                        Text("Debts", style = MaterialTheme.typography.labelSmall, color = Color(0xFFCE93D8), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Manual Step-by-Step Instructions Collapsible
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = "How to Add Widgets Manually",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Step-by-step Android launcher guide",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = { showManualGuide = !showManualGuide }) {
                                Icon(
                                    imageVector = if (showManualGuide) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle Guide"
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = showManualGuide,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                GuideStepItem(
                                    step = "1",
                                    title = "Long-Press Home Screen",
                                    description = "Go to your Android phone's Home Screen and touch & hold on any empty area."
                                )
                                GuideStepItem(
                                    step = "2",
                                    title = "Select Widgets",
                                    description = "Tap on the 'Widgets' icon in the pop-up menu that appears at the bottom."
                                )
                                GuideStepItem(
                                    step = "3",
                                    title = "Find Finance Tracker",
                                    description = "Scroll down or search for 'Finance Tracker' to see all 4 available widget styles."
                                )
                                GuideStepItem(
                                    step = "4",
                                    title = "Drag & Drop",
                                    description = "Touch and hold your preferred widget, then drag it to your desired position on your home screen."
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
private fun WidgetPreviewItemCard(
    title: String,
    gridSize: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    onPinClick: () -> Unit,
    modifier: Modifier = Modifier,
    previewContent: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = gridSize,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Live Preview Visual Container
            previewContent()

            // Pin / Add Button
            Button(
                onClick = onPinClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add to Home Screen",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun WidgetCardContainer(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF1E1E2E))
            .border(1.dp, Color(0xFF3A3A4C), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun WidgetActionBtn(
    icon: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF2A2A3C),
        modifier = modifier.height(54.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun GuideStepItem(
    step: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
