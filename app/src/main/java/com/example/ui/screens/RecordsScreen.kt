package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.model.TransactionType
import com.example.ui.components.AddTransactionSheet
import com.example.ui.components.EmptyListPlaceholder
import com.example.util.CurrencyUtils
import com.example.viewmodels.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecordsScreen(
    viewModel: DashboardViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    val accounts by viewModel.allAccounts.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val baseCurrency = uiState.baseCurrency

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<String?>("All") }
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }

    // Filter transactions based on type, account, and search query
    val filteredTransactions = remember(allTransactions, selectedTypeFilter, selectedAccountId, searchQuery) {
        allTransactions.filter { tx ->
            val matchesType = when (selectedTypeFilter) {
                null, "All" -> true
                else -> tx.type.equals(selectedTypeFilter, ignoreCase = true)
            }

            val matchesAccount = when (selectedAccountId) {
                null -> true
                else -> tx.accountId == selectedAccountId || tx.toAccountId == selectedAccountId
            }

            val matchesQuery = if (searchQuery.isBlank()) {
                true
            } else {
                (tx.note?.contains(searchQuery, ignoreCase = true) == true) ||
                        tx.labels.any { it.contains(searchQuery, ignoreCase = true) }
            }

            matchesType && matchesAccount && matchesQuery
        }
    }

    // Group transactions by date day key
    val groupedTransactions = remember(filteredTransactions) {
        filteredTransactions.groupBy { tx ->
            val cal = Calendar.getInstance().apply { time = tx.date }
            "${cal.get(Calendar.YEAR)}_${cal.get(Calendar.DAY_OF_YEAR)}"
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Master Records Ledger", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Transaction")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by note or #label...") },
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
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Filter Row (Chips)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Type filters
                val typeOptions = listOf("All", "Income", "Expense", "Transfer")
                items(typeOptions) { type ->
                    val isSelected = selectedTypeFilter == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTypeFilter = type },
                        label = { Text(type) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }

                // Account filters
                items(accounts) { account ->
                    val isSelected = selectedAccountId == account.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedAccountId = if (isSelected) null else account.id
                        },
                        label = { Text(account.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            // Transaction Summary Count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredTransactions.size} TRANSACTIONS FOUND",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                if (selectedAccountId != null || selectedTypeFilter != "All" || searchQuery.isNotEmpty()) {
                    Text(
                        text = "Filtered view",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (filteredTransactions.isEmpty()) {
                val hasActiveFilters = searchQuery.isNotBlank() || selectedTypeFilter != "All" || selectedAccountId != null
                EmptyListPlaceholder(
                    icon = Icons.Default.ReceiptLong,
                    title = "No Transactions Found",
                    message = if (hasActiveFilters) {
                        "Try clearing your search query or active filters to view records."
                    } else {
                        "Your master ledger is empty. Start logging transactions to view historical cash movements."
                    },
                    actionLabel = if (hasActiveFilters) "Clear Filters" else null,
                    onAction = {
                        searchQuery = ""
                        selectedTypeFilter = "All"
                        selectedAccountId = null
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    groupedTransactions.forEach { (_, txsInDay) ->
                        val headerDate = txsInDay.firstOrNull()?.date ?: Date()

                        stickyHeader {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = formatHeaderDate(headerDate),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val netDay = txsInDay.sumOf { tx ->
                                        when (tx.type) {
                                            TransactionType.INCOME.displayName -> tx.amount
                                            TransactionType.EXPENSE.displayName -> -tx.amount
                                            else -> 0.0
                                        }
                                    }
                                    Text(
                                        text = String.format(
                                            Locale.getDefault(),
                                            "%s%s",
                                            if (netDay >= 0) "+" else "-",
                                            CurrencyUtils.formatCurrency(kotlin.math.abs(netDay), baseCurrency)
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (netDay >= 0) Color(0xFF00C853) else Color(0xFFFF3B30)
                                    )
                                }
                            }
                        }

                        items(txsInDay, key = { it.id }) { tx ->
                            val sourceAccount = accounts.find { it.id == tx.accountId }
                            val destAccount = if (tx.toAccountId != null) accounts.find { it.id == tx.toAccountId } else null
                            val category = categories.find { it.id == tx.categoryId }

                            MasterRecordItemRow(
                                transaction = tx,
                                sourceAccount = sourceAccount,
                                destAccount = destAccount,
                                category = category,
                                currencyCode = sourceAccount?.currencyCode ?: baseCurrency,
                                onDelete = { viewModel.deleteTransaction(tx) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        val currentAccount = accounts.find { it.id == selectedAccountId }
        AddTransactionSheet(
            accounts = accounts,
            categories = categories,
            baseCurrency = currentAccount?.currencyCode ?: baseCurrency,
            initialAccountId = selectedAccountId,
            onDismissRequest = { showAddSheet = false },
            onSaveTransaction = { amount, type, accId, toAccId, catId, labels, note ->
                viewModel.addQuickTransaction(
                    amount = amount,
                    type = type,
                    accountId = accId,
                    toAccountId = toAccId,
                    categoryId = catId,
                    labels = labels,
                    note = note
                )
                showAddSheet = false
            }
        )
    }
}

@Composable
fun MasterRecordItemRow(
    transaction: TransactionEntity,
    sourceAccount: AccountEntity?,
    destAccount: AccountEntity?,
    category: CategoryEntity?,
    currencyCode: String = "USD",
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type.equals(TransactionType.INCOME.displayName, ignoreCase = true)
    val isExpense = transaction.type.equals(TransactionType.EXPENSE.displayName, ignoreCase = true)
    val isTransfer = transaction.type.equals(TransactionType.TRANSFER.displayName, ignoreCase = true)

    val amountColor = when {
        isIncome -> Color(0xFF00C853)
        isExpense -> Color(0xFFFF3B30)
        else -> MaterialTheme.colorScheme.primary
    }

    val typeIcon = when {
        isIncome -> Icons.Default.ArrowDownward
        isExpense -> Icons.Default.ArrowUpward
        else -> Icons.Default.SwapHoriz
    }

    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type indicator circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(amountColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = null,
                    tint = amountColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.note?.takeIf { it.isNotBlank() }
                        ?: category?.name
                        ?: (if (isTransfer) "Transfer" else transaction.type),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val accountDesc = when {
                        isTransfer && destAccount != null -> "${sourceAccount?.name ?: "Account"} → ${destAccount.name}"
                        else -> sourceAccount?.name ?: "Main Account"
                    }
                    Text(
                        text = accountDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " • ${timeFormatter.format(transaction.date)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                if (transaction.labels.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        transaction.labels.take(3).forEach { label ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "#$label",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Amount & Delete action
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format(
                        Locale.getDefault(),
                        "%s%s",
                        if (isIncome) "+" else if (isExpense) "-" else "",
                        CurrencyUtils.formatCurrency(transaction.amount, currencyCode)
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = amountColor
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun formatHeaderDate(date: Date): String {
    val calNow = Calendar.getInstance()
    val calTx = Calendar.getInstance().apply { time = date }

    return if (calNow.get(Calendar.YEAR) == calTx.get(Calendar.YEAR) &&
        calNow.get(Calendar.DAY_OF_YEAR) == calTx.get(Calendar.DAY_OF_YEAR)
    ) {
        "Today, " + SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
    } else {
        calNow.add(Calendar.DAY_OF_YEAR, -1)
        if (calNow.get(Calendar.YEAR) == calTx.get(Calendar.YEAR) &&
            calNow.get(Calendar.DAY_OF_YEAR) == calTx.get(Calendar.DAY_OF_YEAR)
        ) {
            "Yesterday, " + SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
        } else {
            SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()).format(date)
        }
    }
}
