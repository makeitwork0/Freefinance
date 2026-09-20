package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.model.TransactionType
import com.example.ui.components.AddAccountSheet
import com.example.ui.components.AddTransactionSheet
import com.example.ui.components.EmptyListPlaceholder
import com.example.util.BankAppLauncher
import com.example.util.CurrencyUtils
import com.example.viewmodels.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AccountLogScreen: Dedicated transaction ledger for a specific Account / Card mode.
 * Supports viewing history, logging transactions, and editing account properties after creation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountLogScreen(
    accountId: Long,
    viewModel: DashboardViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val accounts by viewModel.allAccounts.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val hideMoney by viewModel.hideMoney.collectAsState()
    val account = accounts.find { it.id == accountId }

    val accountTransactionsFlow = remember(accountId) {
        viewModel.getTransactionsForAccount(accountId)
    }
    val transactions by accountTransactionsFlow.collectAsState(initial = emptyList())

    var showAddSheet by remember { mutableStateOf(false) }
    var showEditAccountSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val matchingBankApp = remember(account) {
        account?.let { BankAppLauncher.findMatchingBankApp(it.name, it.linkedAppPackage) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = account?.name ?: "Account Ledger",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("account_log_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (account != null) {
                        IconButton(
                            onClick = {
                                BankAppLauncher.launchBankApp(context, account.name, account.linkedAppPackage)
                            },
                            modifier = Modifier.testTag("account_log_quick_launch_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Launch,
                                contentDescription = "Open ${matchingBankApp?.displayName ?: "Bank App"}",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleHideMoney(!hideMoney) },
                            modifier = Modifier.testTag("account_log_hide_money_button")
                        ) {
                            Icon(
                                imageVector = if (hideMoney) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (hideMoney) "Show Balances" else "Hide Balances",
                                tint = if (hideMoney) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { showEditAccountSheet = true },
                            modifier = Modifier.testTag("account_log_edit_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Account Properties",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier.testTag("account_log_delete_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Account",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_account_transaction_fab")
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
            if (account != null) {
                AccountHeaderCard(
                    account = account,
                    hideMoney = hideMoney,
                    onEditClick = { showEditAccountSheet = true },
                    onOpenBankApp = {
                        BankAppLauncher.launchBankApp(context, account.name, account.linkedAppPackage)
                    }
                )
            }

            Text(
                text = "Transaction History (${transactions.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (transactions.isEmpty()) {
                EmptyListPlaceholder(
                    icon = Icons.Default.ReceiptLong,
                    title = "No Transactions Found",
                    message = "No transactions logged for this account yet. Tap + to add one.",
                    actionLabel = "Log Entry",
                    onAction = { showAddSheet = true },
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = transactions,
                        key = { it.id },
                        contentType = { "account_tx_item" }
                    ) { tx ->
                        AccountTransactionItem(
                            transaction = tx,
                            currencyCode = account?.currencyCode ?: "USD",
                            hideMoney = hideMoney,
                            onDeleteClick = {
                                viewModel.deleteTransaction(tx)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddTransactionSheet(
            accounts = accounts,
            categories = categories,
            baseCurrency = account?.currencyCode ?: "USD",
            initialAccountId = accountId,
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

    if (showEditAccountSheet && account != null) {
        AddAccountSheet(
            defaultCurrency = account.currencyCode,
            initialAccount = account,
            onDismissRequest = { showEditAccountSheet = false },
            onSaveAccount = { name, type, balance, color, interestRate, compounding, currencyCode, isLocked, lockedUntil, linkedAppPackage ->
                val updatedAccount = account.copy(
                    name = name,
                    type = type,
                    currentBalance = balance,
                    color = color,
                    interestRatePa = interestRate,
                    compoundingFrequency = compounding,
                    currencyCode = currencyCode,
                    isLocked = isLocked,
                    lockedUntil = lockedUntil,
                    linkedAppPackage = linkedAppPackage
                )
                viewModel.updateAccount(updatedAccount)
                showEditAccountSheet = false
            },
            onDeleteAccount = { accToDelete ->
                viewModel.deleteAccount(accToDelete)
                showEditAccountSheet = false
                onNavigateBack()
            }
        )
    }

    if (showDeleteConfirmDialog && account != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Account / Mode?") },
            text = { Text("Are you sure you want to delete '${account.name}'? Existing transactions will keep their records.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccount(account)
                        showDeleteConfirmDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AccountHeaderCard(
    account: AccountEntity,
    hideMoney: Boolean = false,
    onEditClick: () -> Unit,
    onOpenBankApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accountColor = Color(account.color)
    val matchingApp = remember(account) {
        BankAppLauncher.findMatchingBankApp(account.name, account.linkedAppPackage)
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = accountColor
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onEditClick() }
            .testTag("account_header_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = account.type.uppercase(Locale.ROOT),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (account.isLocked || account.lockedUntil != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.35f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked Account",
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(12.dp)
                                )
                                val isPastMaturity = account.lockedUntil?.before(Date()) == true
                                val lockLabel = if (account.lockedUntil != null) {
                                    if (isPastMaturity) {
                                        "Matured (${SimpleDateFormat("MMM d", Locale.getDefault()).format(account.lockedUntil)})"
                                    } else {
                                        "Locked till ${SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(account.lockedUntil)}"
                                    }
                                } else {
                                    "Locked"
                                }
                                Text(
                                    text = lockLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = account.currencyCode,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.25f),
                        onClick = onEditClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Account",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Current Balance",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f)
            )

            Text(
                text = CurrencyUtils.formatCurrency(account.currentBalance, account.currencyCode, hideMoney = hideMoney),
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            if (account.interestRatePa > 0f) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Yield: +${String.format(Locale.getDefault(), "%.2f", account.interestRatePa)}% p.a. (${account.compoundingFrequency.displayName})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.3f),
                onClick = onOpenBankApp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_header_open_bank_app")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Launch,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (matchingApp != null) "Open ${matchingApp.displayName} App" else "Quick Go-To-App (${account.name})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Launch ↗",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountTransactionItem(
    transaction: TransactionEntity,
    currencyCode: String,
    hideMoney: Boolean = false,
    onDeleteClick: () -> Unit
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
        SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault()).format(transaction.date)
    }

    val formattedAmount = remember(transaction.amount, currencyCode, hideMoney, isIncome, isTransfer) {
        val amt = CurrencyUtils.formatCurrency(transaction.amount, currencyCode, hideMoney = hideMoney)
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

    val noteTitle = remember(transaction.note, transaction.type) {
        if (transaction.note.isNullOrBlank()) transaction.type else transaction.note
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
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
                    .size(38.dp)
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
                    text = noteTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = formattedAmount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = typeColor
            )

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete entry",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
