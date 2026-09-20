package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.model.RecurrenceRule
import com.example.data.local.model.TransactionType
import com.example.ui.components.EmptyListPlaceholder
import com.example.util.CurrencyUtils
import com.example.viewmodels.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class RecurringFilterType(val displayName: String) {
    ALL("All"),
    EXPENSES("Bills & Subscriptions"),
    INCOMES("Recurring Receivables")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen(
    viewModel: DashboardViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subscriptions by viewModel.recurringTransactions.collectAsState()
    val accounts by viewModel.allAccounts.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val baseCurrency = uiState.baseCurrency

    var showAddSubscriptionSheet by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<TransactionEntity?>(null) }
    var selectedFilter by remember { mutableStateOf(RecurringFilterType.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var itemToStop by remember { mutableStateOf<TransactionEntity?>(null) }

    // Split Outflows and Inflows
    val expenseSubscriptions = remember(subscriptions) {
        subscriptions.filter { it.type.equals(TransactionType.EXPENSE.displayName, ignoreCase = true) }
    }
    val incomeSubscriptions = remember(subscriptions) {
        subscriptions.filter { it.type.equals(TransactionType.INCOME.displayName, ignoreCase = true) }
    }

    // Monthly commitments
    val monthlyBurn = remember(expenseSubscriptions) {
        expenseSubscriptions.sumOf { calculateMonthlyEquivalent(it.amount, it.recurrenceRule) }
    }
    val monthlyInflow = remember(incomeSubscriptions) {
        incomeSubscriptions.sumOf { calculateMonthlyEquivalent(it.amount, it.recurrenceRule) }
    }
    val netMonthly = monthlyInflow - monthlyBurn

    // Filtered list
    val filteredList = remember(subscriptions, selectedFilter, searchQuery) {
        subscriptions.filter { item ->
            val matchesFilter = when (selectedFilter) {
                RecurringFilterType.ALL -> true
                RecurringFilterType.EXPENSES -> item.type.equals(TransactionType.EXPENSE.displayName, ignoreCase = true)
                RecurringFilterType.INCOMES -> item.type.equals(TransactionType.INCOME.displayName, ignoreCase = true)
            }
            val matchesQuery = if (searchQuery.isBlank()) true else {
                val query = searchQuery.trim().lowercase(Locale.getDefault())
                (item.note?.lowercase(Locale.getDefault())?.contains(query) == true) ||
                        (item.labels.any { it.lowercase(Locale.getDefault()).contains(query) }) ||
                        (item.recurrenceRule.lowercase(Locale.getDefault()).contains(query))
            }
            matchesFilter && matchesQuery
        }.sortedBy { calculateNextDueDate(it.date, it.recurrenceRule).time }
    }

    // Soonest upcoming item
    val nextUpcoming = remember(subscriptions) {
        subscriptions
            .map { it to calculateNextDueDate(it.date, it.recurrenceRule) }
            .minByOrNull { it.second.time }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Subscriptions & Bills",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Automated Recurring Schedules",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddSubscriptionSheet = true },
                        modifier = Modifier.testTag("add_subscription_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Schedule",
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
            FloatingActionButton(
                onClick = { showAddSubscriptionSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("subscriptions_fab_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Recurring Schedule")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Hero Financial Health & Monthly Commitment Card
            item {
                RecurringOverviewHeroCard(
                    monthlyBurn = monthlyBurn,
                    monthlyInflow = monthlyInflow,
                    netMonthly = netMonthly,
                    activeCount = subscriptions.size,
                    baseCurrency = baseCurrency,
                    nextUpcoming = nextUpcoming
                )
            }

            // 2. Search & Cadence Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search subscriptions, bills, or notes...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(RecurringFilterType.entries.toTypedArray()) { filter ->
                            val isSelected = selectedFilter == filter
                            val count = when (filter) {
                                RecurringFilterType.ALL -> subscriptions.size
                                RecurringFilterType.EXPENSES -> expenseSubscriptions.size
                                RecurringFilterType.INCOMES -> incomeSubscriptions.size
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = filter },
                                label = { Text("${filter.displayName} ($count)") },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // 3. Section Title with Count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SCHEDULED RECURRING ITEMS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${filteredList.size} Showing",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 4. List of Items or Empty Placeholder
            if (filteredList.isEmpty()) {
                item {
                    EmptyListPlaceholder(
                        icon = Icons.Default.NotificationsActive,
                        title = if (searchQuery.isNotEmpty()) "No Matching Schedules" else "No Recurring Schedules",
                        message = if (searchQuery.isNotEmpty()) "Try a different search keyword." else "Add fixed commitments like Google AI Pro, YouTube Premium, rent, or recurring freelance retainers.",
                        actionLabel = if (searchQuery.isNotEmpty()) "Clear Search" else "Add Schedule",
                        onAction = {
                            if (searchQuery.isNotEmpty()) searchQuery = "" else showAddSubscriptionSheet = true
                        }
                    )
                }
            } else {
                items(filteredList, key = { it.id }) { subscription ->
                    val account = accounts.find { it.id == subscription.accountId }
                    val category = categories.find { it.id == subscription.categoryId }

                    RevampedSubscriptionCard(
                        subscription = subscription,
                        currencyCode = baseCurrency,
                        account = account,
                        category = category,
                        onMarkPaid = {
                            viewModel.markRecurringPaidOrReceived(subscription)
                        },
                        onEdit = {
                            editingItem = subscription
                        },
                        onStopRecurrence = {
                            itemToStop = subscription
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Stop / Cancel Confirmation Dialog
    itemToStop?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToStop = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Stop Recurring Schedule?") },
            text = {
                Text(
                    "Are you sure you want to cancel the recurring schedule for '${item.note ?: item.type}'? It will no longer automatically project future cash-flow."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.stopSubscription(item.id)
                        itemToStop = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Stop Recurrence")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToStop = null }) {
                    Text("Keep Active")
                }
            }
        )
    }

    // Add Subscription Sheet
    if (showAddSubscriptionSheet) {
        RevampedAddSubscriptionBottomSheet(
            currencyCode = baseCurrency,
            accounts = accounts,
            categories = categories,
            onDismiss = { showAddSubscriptionSheet = false },
            onSaveSubscription = { amount, type, accountId, categoryId, recurrence, date, note, labels ->
                viewModel.addRecurringItem(
                    amount = amount,
                    type = type,
                    accountId = accountId,
                    categoryId = categoryId,
                    recurrenceRule = recurrence,
                    startDate = date,
                    note = note,
                    labels = labels
                )
                showAddSubscriptionSheet = false
            }
        )
    }

    // Edit Subscription Sheet
    editingItem?.let { item ->
        RevampedAddSubscriptionBottomSheet(
            currencyCode = baseCurrency,
            accounts = accounts,
            categories = categories,
            existingItem = item,
            onDismiss = { editingItem = null },
            onSaveSubscription = { amount, type, accountId, categoryId, recurrence, date, note, labels ->
                val updated = item.copy(
                    amount = amount,
                    type = type.displayName,
                    accountId = accountId,
                    categoryId = categoryId,
                    recurrenceRule = recurrence.displayName,
                    date = date,
                    note = note,
                    labels = labels
                )
                viewModel.updateRecurringItem(updated)
                editingItem = null
            }
        )
    }
}

/**
 * Modern Hero Card displaying Monthly Burn, Monthly Inflow, and Upcoming Due Alert.
 */
@Composable
fun RecurringOverviewHeroCard(
    monthlyBurn: Double,
    monthlyInflow: Double,
    netMonthly: Double,
    activeCount: Int,
    baseCurrency: String,
    nextUpcoming: Pair<TransactionEntity, Date>?,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventRepeat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "MONTHLY RECURRING RUN-RATE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$activeCount Active Plans",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = baseCurrency,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // 2-Column Metrics: Monthly Bills Outflow vs Recurring Receivables
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Outflow Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Monthly Bills",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyUtils.formatCurrency(monthlyBurn, baseCurrency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFE53935)
                        )
                    }
                }

                // Inflow Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Receivables",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyUtils.formatCurrency(monthlyInflow, baseCurrency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            // Soonest Due Alert Banner
            if (nextUpcoming != null) {
                val item = nextUpcoming.first
                val dueDate = nextUpcoming.second
                val days = daysUntil(dueDate)
                val isIncome = item.type.equals(TransactionType.INCOME.displayName, ignoreCase = true)

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(
                                    text = "Next: ${item.note ?: item.type}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${dateFormatter.format(dueDate)} (${when(days) { 0L -> "Today"; 1L -> "Tomorrow"; else -> "in $days days" }})",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "${if (isIncome) "+" else "-"}${CurrencyUtils.formatCurrency(item.amount, baseCurrency)}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isIncome) Color(0xFF2E7D32) else Color(0xFFE53935)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Revamped Item Card with avatar, cadence pill, relative due badge, and quick-action buttons.
 */
@Composable
fun RevampedSubscriptionCard(
    subscription: TransactionEntity,
    currencyCode: String,
    account: AccountEntity?,
    category: CategoryEntity?,
    onMarkPaid: () -> Unit,
    onEdit: () -> Unit,
    onStopRecurrence: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = subscription.type.equals(TransactionType.INCOME.displayName, ignoreCase = true)
    val nextDueDate = remember(subscription.date, subscription.recurrenceRule) {
        calculateNextDueDate(subscription.date, subscription.recurrenceRule)
    }
    val daysUntilDue = remember(nextDueDate) {
        daysUntil(nextDueDate)
    }
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    // Derive avatar color and label
    val title = subscription.note ?: category?.name ?: if (isIncome) "Receivable" else "Subscription"
    val avatarBg = when {
        title.contains("Google", ignoreCase = true) -> Color(0xFF4285F4)
        title.contains("YouTube", ignoreCase = true) -> Color(0xFFFF0000)
        title.contains("Netflix", ignoreCase = true) -> Color(0xFFE50914)
        title.contains("Spotify", ignoreCase = true) -> Color(0xFF1DB954)
        isIncome -> Color(0xFF2E7D32)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("subscription_card_${subscription.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Row: Avatar + Title & Account + Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(avatarBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title.take(2).uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = subscription.recurrenceRule,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            account?.let { acc ->
                                Text(
                                    text = "• ${acc.name}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Amount
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isIncome) "+" else "-"}${CurrencyUtils.formatCurrency(subscription.amount, currencyCode)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isIncome) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isIncome) "Inflow" else "Outflow",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Middle Row: Next Due Date & Days Countdown Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Next: ${dateFormatter.format(nextDueDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                val badgeColor = when {
                    daysUntilDue == 0L -> Color(0xFFE53935)
                    daysUntilDue <= 3L -> Color(0xFFFF9500)
                    else -> MaterialTheme.colorScheme.primary
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = when (daysUntilDue) {
                            0L -> "Due Today"
                            1L -> "Due Tomorrow"
                            else -> "In $daysUntilDue days"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Bottom Row: Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Schedule",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onStopRecurrence,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Stop Schedule",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Log cleared transaction for current cycle
                Button(
                    onClick = onMarkPaid,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isIncome) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isIncome) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isIncome) "Mark Received" else "Mark as Paid",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Revamped Modal Bottom Sheet for adding or editing recurring subscriptions and receivables.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevampedAddSubscriptionBottomSheet(
    currencyCode: String,
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    existingItem: TransactionEntity? = null,
    onDismiss: () -> Unit,
    onSaveSubscription: (
        amount: Double,
        type: TransactionType,
        accountId: Long,
        categoryId: Long?,
        recurrence: RecurrenceRule,
        startDate: Date,
        note: String,
        labels: List<String>
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var itemType by remember {
        mutableStateOf(
            if (existingItem?.type.equals(TransactionType.INCOME.displayName, ignoreCase = true)) {
                TransactionType.INCOME
            } else {
                TransactionType.EXPENSE
            }
        )
    }

    var nameInput by remember { mutableStateOf(existingItem?.note ?: "") }
    var amountInput by remember { mutableStateOf(existingItem?.let { String.format(Locale.getDefault(), "%.2f", it.amount) } ?: "") }
    var selectedAccountId by remember { mutableStateOf(existingItem?.accountId ?: accounts.firstOrNull()?.id) }
    var selectedCategoryId by remember { mutableStateOf(existingItem?.categoryId ?: categories.firstOrNull()?.id) }
    var selectedRecurrence by remember {
        mutableStateOf(
            existingItem?.recurrenceRule?.let { ruleStr ->
                RecurrenceRule.entries.find { it.displayName.equals(ruleStr, ignoreCase = true) }
            } ?: RecurrenceRule.MONTHLY
        )
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val currencySymbol = CurrencyUtils.getSymbol(currencyCode)

    // Quick suggestion presets
    val expensePresets = listOf("Google AI Pro", "YouTube Premium", "Netflix", "Spotify", "Rent & Utilities", "iCloud", "Gym")
    val incomePresets = listOf("Jonrelle Retainer", "JV Receivable", "Allisa Monthly", "JB Retainer", "Salary", "Freelance Client")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (existingItem != null) "Edit Recurring Schedule" else "Add Recurring Schedule",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // 1. Type Switch: Expense (Bill/Subscription) vs Income (Receivable)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = { itemType = TransactionType.EXPENSE },
                    shape = RoundedCornerShape(12.dp),
                    color = if (itemType == TransactionType.EXPENSE) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (itemType == TransactionType.EXPENSE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Bill / Subscription",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (itemType == TransactionType.EXPENSE) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    onClick = { itemType = TransactionType.INCOME },
                    shape = RoundedCornerShape(12.dp),
                    color = if (itemType == TransactionType.INCOME) Color(0xFF2E7D32).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = if (itemType == TransactionType.INCOME) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Recurring Inflow",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (itemType == TransactionType.INCOME) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Smart Preset Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val presets = if (itemType == TransactionType.EXPENSE) expensePresets else incomePresets
                items(presets) { preset ->
                    Surface(
                        onClick = { nameInput = preset },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 2. Name Input
            OutlinedTextField(
                value = nameInput,
                onValueChange = {
                    nameInput = it
                    errorMessage = null
                },
                label = { Text("Service / Plan / Person Name") },
                placeholder = { Text(if (itemType == TransactionType.EXPENSE) "e.g. Google AI Pro, YT Premium, Rent" else "e.g. Jonrelle, Retainer Client") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 3. Amount Input
            OutlinedTextField(
                value = amountInput,
                onValueChange = {
                    amountInput = it
                    errorMessage = null
                },
                label = { Text("Recurring Amount") },
                prefix = { Text("$currencySymbol ") },
                placeholder = { Text("e.g. 1100.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 4. Billing Cadence
            Text(
                text = "Cadence Frequency",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(RecurrenceRule.DAILY, RecurrenceRule.WEEKLY, RecurrenceRule.MONTHLY).forEach { rule ->
                    FilterChip(
                        selected = selectedRecurrence == rule,
                        onClick = { selectedRecurrence = rule },
                        label = { Text(rule.displayName) }
                    )
                }
            }

            // 5. Target Account
            if (accounts.isNotEmpty()) {
                Text(
                    text = if (itemType == TransactionType.EXPENSE) "Deduct From Mode of Cash" else "Deposit Into Mode of Cash",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(accounts) { account ->
                        val isSelected = selectedAccountId == account.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedAccountId = account.id },
                            label = { Text("${account.name} (${account.type})") },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }

            AnimatedVisibility(visible = errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val amount = amountInput.toDoubleOrNull()
                        val accountId = selectedAccountId
                        if (nameInput.isBlank()) {
                            errorMessage = "Please enter a name for this schedule."
                            return@Button
                        }
                        if (amount == null || amount <= 0.0) {
                            errorMessage = "Please enter a valid recurring amount."
                            return@Button
                        }
                        if (accountId == null) {
                            errorMessage = "Please select a target account."
                            return@Button
                        }

                        val labels = if (itemType == TransactionType.EXPENSE) {
                            listOf("subscription", "recurring")
                        } else {
                            listOf("receivable", "recurring")
                        }

                        onSaveSubscription(
                            amount,
                            itemType,
                            accountId,
                            selectedCategoryId,
                            selectedRecurrence,
                            existingItem?.date ?: Date(),
                            nameInput.trim(),
                            labels
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (existingItem != null) "Update Schedule" else "Save Schedule")
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// RECURRENCE & DATE HELPERS
// -----------------------------------------------------------------------------------------

private fun calculateMonthlyEquivalent(amount: Double, recurrenceRule: String): Double {
    return when (recurrenceRule.lowercase(Locale.getDefault())) {
        "daily" -> amount * 30.0
        "weekly" -> amount * 4.333
        "monthly" -> amount
        "yearly", "annual" -> amount / 12.0
        else -> amount
    }
}

private fun calculateNextDueDate(startDate: Date, frequency: String): Date {
    val cal = Calendar.getInstance()
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    cal.time = startDate
    if (cal.after(today) || cal == today) return cal.time

    while (cal.before(today)) {
        when (frequency.lowercase(Locale.getDefault())) {
            "daily" -> cal.add(Calendar.DAY_OF_YEAR, 1)
            "weekly" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            "monthly" -> cal.add(Calendar.MONTH, 1)
            "yearly", "annual" -> cal.add(Calendar.YEAR, 1)
            else -> {
                cal.add(Calendar.MONTH, 1)
                break
            }
        }
    }
    return cal.time
}

private fun daysUntil(targetDate: Date): Long {
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val diff = targetDate.time - today
    return (diff / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
}
