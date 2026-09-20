package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import com.example.ui.components.EmptyListPlaceholder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.model.BudgetPeriod
import com.example.data.local.model.BudgetProgress
import com.example.data.local.model.CategoryExpense
import com.example.util.CurrencyUtils
import com.example.viewmodels.DashboardViewModel
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: DashboardViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryExpenses by viewModel.categoryExpenses.collectAsState()
    val budgetProgressList by viewModel.budgetProgressList.collectAsState()
    val allBudgets by viewModel.allBudgets.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val baseCurrency by viewModel.baseCurrency.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddBudgetSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Budgets & Spending", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddBudgetSheet = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Budget")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Expense Visualizer", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Active Budgets (${budgetProgressList.size})", fontWeight = FontWeight.Bold) }
                )
            }

            Crossfade(
                targetState = selectedTab,
                label = "budget_tabs_crossfade"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> {
                        // PART 1: Expense Category Visualizer
                        ExpenseCategoryVisualizerTab(
                            expenses = categoryExpenses,
                            currencyCode = baseCurrency,
                            onAddBudgetClick = { showAddBudgetSheet = true }
                        )
                    }
                    1 -> {
                        // PART 2: Advanced Budget Management
                        BudgetManagementTab(
                            budgetProgressList = budgetProgressList,
                            allBudgets = allBudgets,
                            currencyCode = baseCurrency,
                            onDeleteBudget = { budget -> viewModel.deleteBudget(budget) },
                            onAddBudgetClick = { showAddBudgetSheet = true }
                        )
                    }
                }
            }
        }
    }

    if (showAddBudgetSheet) {
        AddBudgetBottomSheet(
            categories = allCategories,
            currencyCode = baseCurrency,
            onDismiss = { showAddBudgetSheet = false },
            onSaveBudget = { categoryId, limit, period, startDate, endDate ->
                viewModel.createBudget(
                    categoryId = categoryId,
                    amountLimit = limit,
                    period = period,
                    startDate = startDate,
                    endDate = endDate
                )
                selectedTab = 1
                showAddBudgetSheet = false
            }
        )
    }
}

// -----------------------------------------------------------------------------------------
// PART 1: EXPENSE CATEGORY DONUT CHART & LEGEND
// -----------------------------------------------------------------------------------------

private val PALETTE_COLORS = listOf(
    Color(0xFFFF5722),
    Color(0xFF2196F3),
    Color(0xFF4CAF50),
    Color(0xFFFF9800),
    Color(0xFF9C27B0),
    Color(0xFF00BCD4),
    Color(0xFFE91E63),
    Color(0xFF3F51B5),
    Color(0xFFFFC107),
    Color(0xFF009688)
)

private fun safeParseColor(hex: String, index: Int): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        PALETTE_COLORS[index % PALETTE_COLORS.size]
    }
}

@Composable
fun ExpenseCategoryVisualizerTab(
    expenses: List<CategoryExpense>,
    currencyCode: String = "USD",
    onAddBudgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalExpense = expenses.sumOf { it.totalAmount }
    val animatedChartProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "donut_chart_animation"
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Donut Chart Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "EXPENSE BREAKDOWN",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Compose Canvas Donut Chart
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(230.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 32.dp.toPx()
                            val diameter = size.minDimension - strokeWidth
                            val arcSize = Size(diameter, diameter)
                            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                            if (totalExpense <= 0.0 || expenses.isEmpty()) {
                                drawArc(
                                    color = Color.Gray.copy(alpha = 0.25f),
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth)
                                )
                            } else {
                                var currentAngle = -90f
                                expenses.forEachIndexed { index, exp ->
                                    val sweep = (exp.totalAmount / totalExpense * 360.0).toFloat()
                                    val color = safeParseColor(exp.categoryColor, index)
                                    val animatedSweep = (sweep - 1.5f).coerceAtLeast(0f) * animatedChartProgress
                                    drawArc(
                                        color = color,
                                        startAngle = currentAngle,
                                        sweepAngle = animatedSweep, // Subtle gap between slices
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                    currentAngle += sweep
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Total Spending",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyUtils.formatCurrency(totalExpense, currencyCode),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${expenses.size} Categories",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Legend & Breakdown List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CATEGORY BREAKDOWN",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${expenses.size} tracked",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (expenses.isEmpty()) {
            item {
                EmptyListPlaceholder(
                    icon = Icons.Default.PieChart,
                    title = "No Cleared Expenses Yet",
                    message = "Add transactions to see your circular category breakdown and spending insights."
                )
            }
        } else {
            items(expenses) { item ->
                val index = expenses.indexOf(item)
                val color = safeParseColor(item.categoryColor, index)
                val percent = if (totalExpense > 0) (item.totalAmount / totalExpense * 100) else 0.0

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.categoryName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            LinearProgressIndicator(
                                progress = { (percent / 100f).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = color,
                                trackColor = color.copy(alpha = 0.2f),
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = CurrencyUtils.formatCurrency(item.totalAmount, currencyCode),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f%%", percent),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// PART 2: ADVANCED BUDGET MANAGEMENT TAB
// -----------------------------------------------------------------------------------------

@Composable
fun BudgetManagementTab(
    budgetProgressList: List<BudgetProgress>,
    allBudgets: List<BudgetEntity>,
    currencyCode: String = "USD",
    onDeleteBudget: (BudgetEntity) -> Unit,
    onAddBudgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Spending Limits & Budgets",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Set limits for specific categories across daily, weekly, or monthly intervals.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = onAddBudgetClick) {
                        Text("+ New Budget", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (budgetProgressList.isEmpty()) {
            item {
                EmptyListPlaceholder(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "No Budgets Configured",
                    message = "Prevent overspending by defining monthly limits for dining, bills, or shopping.",
                    actionLabel = "Create First Budget",
                    onAction = onAddBudgetClick
                )
            }
        } else {
            items(budgetProgressList, key = { it.budgetId }) { progress ->
                val associatedBudget = allBudgets.find { it.id == progress.budgetId }
                val isOverBudget = progress.isOverBudget
                val ratio = progress.progressRatio.coerceAtLeast(0.0)
                val animatedRatio by animateFloatAsState(
                    targetValue = ratio.toFloat().coerceIn(0f, 1f),
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                    label = "budget_item_anim"
                )

                val indicatorColor = when {
                    isOverBudget -> Color(0xFFFF3B30)
                    ratio > 0.8 -> Color(0xFFFF9500)
                    else -> MaterialTheme.colorScheme.primary
                }

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOverBudget) {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(indicatorColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = progress.categoryName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = progress.period,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            if (associatedBudget != null) {
                                IconButton(
                                    onClick = { onDeleteBudget(associatedBudget) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Budget",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { animatedRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = indicatorColor,
                            trackColor = indicatorColor.copy(alpha = 0.2f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Spent: " + CurrencyUtils.formatCurrency(progress.currentSpent, currencyCode),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Limit: " + CurrencyUtils.formatCurrency(progress.amountLimit, currencyCode),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                if (isOverBudget) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = Color(0xFFFF3B30),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Over by " + CurrencyUtils.formatCurrency(-progress.remainingAmount, currencyCode),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF3B30)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = CurrencyUtils.formatCurrency(progress.remainingAmount, currencyCode) + " left",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00C853)
                                    )
                                }
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f%% used", ratio * 100),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// ADD BUDGET MODAL BOTTOM SHEET
// -----------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetBottomSheet(
    categories: List<com.example.data.local.entity.CategoryEntity>,
    currencyCode: String = "USD",
    onDismiss: () -> Unit,
    onSaveBudget: (categoryId: Long, limit: Double, period: BudgetPeriod, startDate: Date, endDate: Date) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val relevantCategories = remember(categories) {
        val expense = categories.filter { it.type.equals("Expense", ignoreCase = true) }
        if (expense.isNotEmpty()) expense else categories
    }

    var selectedCategoryId by remember(relevantCategories) {
        mutableStateOf(relevantCategories.firstOrNull()?.id)
    }
    var limitInput by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf(BudgetPeriod.MONTHLY) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val currencySymbol = CurrencyUtils.getSymbol(currencyCode)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Create Spending Budget",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Category Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                if (relevantCategories.isEmpty()) {
                    Text(
                        text = "No categories available.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(relevantCategories, key = { it.id }) { category ->
                            val isSelected = selectedCategoryId == category.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCategoryId = category.id
                                    errorMessage = null
                                },
                                label = { Text(category.name) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else {
                                    {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color(category.color))
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Limit Input
            OutlinedTextField(
                value = limitInput,
                onValueChange = {
                    limitInput = it
                    errorMessage = null
                },
                label = { Text("Amount Limit") },
                prefix = { Text("$currencySymbol ") },
                placeholder = { Text("e.g. 500.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Period Selector
            Text(
                text = "Budget Period",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                BudgetPeriod.entries.forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        label = { Text(period.displayName) },
                        colors = FilterChipDefaults.filterChipColors()
                    )
                }
            }

            AnimatedVisibility(visible = errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val categoryId = selectedCategoryId
                        val limit = limitInput.toDoubleOrNull()
                        if (categoryId == null) {
                            errorMessage = "Please select a category."
                            return@Button
                        }
                        if (limit == null || limit <= 0.0) {
                            errorMessage = "Please enter a valid limit greater than 0."
                            return@Button
                        }

                        // Compute date boundaries based on selected period
                        val cal = Calendar.getInstance()
                        val startDate: Date
                        val endDate: Date

                        when (selectedPeriod) {
                            BudgetPeriod.DAILY -> {
                                cal.set(Calendar.HOUR_OF_DAY, 0)
                                cal.set(Calendar.MINUTE, 0)
                                cal.set(Calendar.SECOND, 0)
                                cal.set(Calendar.MILLISECOND, 0)
                                startDate = cal.time
                                cal.set(Calendar.HOUR_OF_DAY, 23)
                                cal.set(Calendar.MINUTE, 59)
                                cal.set(Calendar.SECOND, 59)
                                cal.set(Calendar.MILLISECOND, 999)
                                endDate = cal.time
                            }
                            BudgetPeriod.WEEKLY -> {
                                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                                cal.set(Calendar.HOUR_OF_DAY, 0)
                                cal.set(Calendar.MINUTE, 0)
                                cal.set(Calendar.SECOND, 0)
                                cal.set(Calendar.MILLISECOND, 0)
                                startDate = cal.time
                                cal.add(Calendar.DAY_OF_WEEK, 6)
                                cal.set(Calendar.HOUR_OF_DAY, 23)
                                cal.set(Calendar.MINUTE, 59)
                                cal.set(Calendar.SECOND, 59)
                                cal.set(Calendar.MILLISECOND, 999)
                                endDate = cal.time
                            }
                            BudgetPeriod.MONTHLY -> {
                                cal.set(Calendar.DAY_OF_MONTH, 1)
                                cal.set(Calendar.HOUR_OF_DAY, 0)
                                cal.set(Calendar.MINUTE, 0)
                                cal.set(Calendar.SECOND, 0)
                                cal.set(Calendar.MILLISECOND, 0)
                                startDate = cal.time
                                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                                cal.set(Calendar.HOUR_OF_DAY, 23)
                                cal.set(Calendar.MINUTE, 59)
                                cal.set(Calendar.SECOND, 59)
                                cal.set(Calendar.MILLISECOND, 999)
                                endDate = cal.time
                            }
                            BudgetPeriod.CUSTOM -> {
                                cal.set(Calendar.HOUR_OF_DAY, 0)
                                cal.set(Calendar.MINUTE, 0)
                                cal.set(Calendar.SECOND, 0)
                                cal.set(Calendar.MILLISECOND, 0)
                                startDate = cal.time
                                cal.add(Calendar.DAY_OF_YEAR, 30)
                                cal.set(Calendar.HOUR_OF_DAY, 23)
                                cal.set(Calendar.MINUTE, 59)
                                cal.set(Calendar.SECOND, 59)
                                cal.set(Calendar.MILLISECOND, 999)
                                endDate = cal.time
                            }
                        }

                        onSaveBudget(categoryId, limit, selectedPeriod, startDate, endDate)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Budget")
                }
            }
        }
    }
}
