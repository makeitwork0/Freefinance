package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DebtEntity
import com.example.data.local.model.DebtType
import com.example.ui.components.EmptyListPlaceholder
import com.example.util.CurrencyUtils
import com.example.viewmodels.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtScreen(
    viewModel: DashboardViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: "Owe Me" (Lent), 1: "I Owe" (Borrowed)
    val uiState by viewModel.uiState.collectAsState()
    val baseCurrency = uiState.baseCurrency
    val lentDebts by viewModel.activeLentDebts.collectAsState()
    val borrowedDebts by viewModel.activeBorrowedDebts.collectAsState()
    val totalLent by viewModel.totalActiveLent.collectAsState()
    val totalBorrowed by viewModel.totalActiveBorrowed.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var debtToEdit by remember { mutableStateOf<DebtEntity?>(null) }
    var debtToSettle by remember { mutableStateOf<DebtEntity?>(null) }
    var settleAmountText by remember { mutableStateOf("") }

    val currentList = if (selectedTabIndex == 0) lentDebts else borrowedDebts
    val currentTotal = if (selectedTabIndex == 0) totalLent else totalBorrowed

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Lent & Borrowed", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Debt")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row: "Owe Me" / "I Owe"
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            "Owe Me (${lentDebts.size})",
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            "I Owe (${borrowedDebts.size})",
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            // Overview Metric Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedTabIndex == 0) Color(0xFF00C853).copy(alpha = 0.12f) else Color(0xFFD50000).copy(alpha = 0.12f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (selectedTabIndex == 0) "TOTAL MONEY OWED TO YOU" else "TOTAL YOU OWE OTHERS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIndex == 0) Color(0xFF00C853) else Color(0xFFD50000)
                        )
                        Text(
                            text = CurrencyUtils.formatCurrency(currentTotal, baseCurrency),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (selectedTabIndex == 0) Color(0xFF00C853) else Color(0xFFD50000)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (selectedTabIndex == 0) Color(0xFF00C853).copy(alpha = 0.2f) else Color(0xFFD50000).copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = if (selectedTabIndex == 0) Color(0xFF00C853) else Color(0xFFD50000)
                        )
                    }
                }
            }

            // List of Debts
            if (currentList.isEmpty()) {
                EmptyListPlaceholder(
                    icon = Icons.Default.People,
                    title = if (selectedTabIndex == 0) "No Pending Receivables" else "No Outstanding Payables",
                    message = if (selectedTabIndex == 0) "No one currently owes you money." else "You don't owe any money to anyone right now.",
                    actionLabel = "Record Person / Debt",
                    onAction = { showAddDialog = true },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(currentList, key = { it.id }) { debt ->
                        DebtItemCard(
                            debt = debt,
                            isLent = selectedTabIndex == 0,
                            currencyCode = baseCurrency,
                            onEditClicked = {
                                debtToEdit = debt
                            },
                            onSettleClicked = {
                                debtToSettle = debt
                                settleAmountText = String.format(Locale.getDefault(), "%.2f", debt.remainingAmount)
                            },
                            onDeleteClicked = {
                                viewModel.deleteDebt(debt)
                            }
                        )
                    }
                }
            }
        }
    }

    // Edit Debt Dialog
    debtToEdit?.let { debt ->
        EditDebtDialog(
            debt = debt,
            currencyCode = baseCurrency,
            onDismiss = { debtToEdit = null },
            onConfirm = { name, type, totalAmount, remainingAmount, notes ->
                viewModel.updateDebt(
                    debtId = debt.id,
                    personName = name,
                    type = type,
                    totalAmount = totalAmount,
                    remainingAmount = remainingAmount,
                    dueDate = debt.dueDate,
                    notes = notes
                )
                debtToEdit = null
            }
        )
    }

    // Add Debt Dialog
    if (showAddDialog) {
        AddDebtDialog(
            initialType = if (selectedTabIndex == 0) DebtType.LENT else DebtType.BORROWED,
            currencyCode = baseCurrency,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type, amount, notes ->
                viewModel.addDebt(name, type, amount, null, notes)
                showAddDialog = false
            }
        )
    }

    // Settle / Pay Dialog
    debtToSettle?.let { debt ->
        val currencySymbol = CurrencyUtils.getSymbol(baseCurrency)
        AlertDialog(
            onDismissRequest = { debtToSettle = null },
            title = { Text("Settle / Pay: ${debt.personName}") },
            text = {
                Column {
                    Text(
                        text = "Remaining balance: " + CurrencyUtils.formatCurrency(debt.remainingAmount, baseCurrency),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = settleAmountText,
                        onValueChange = { settleAmountText = it },
                        label = { Text("Payment Amount") },
                        prefix = { Text("$currencySymbol ") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = settleAmountText.toDoubleOrNull() ?: debt.remainingAmount
                        if (amount >= debt.remainingAmount) {
                            viewModel.settleDebt(debt.id)
                        } else {
                            viewModel.payDebtPortion(debt.id, amount)
                        }
                        debtToSettle = null
                    }
                ) {
                    Text("Confirm Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { debtToSettle = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DebtItemCard(
    debt: DebtEntity,
    isLent: Boolean,
    currencyCode: String = "USD",
    onEditClicked: () -> Unit,
    onSettleClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                Column {
                    Text(
                        text = debt.personName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Created ${dateFormat.format(debt.createdAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyUtils.formatCurrency(debt.remainingAmount, currencyCode),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isLent) Color(0xFF00C853) else Color(0xFFD50000)
                    )
                    if (debt.remainingAmount < debt.totalAmount) {
                        Text(
                            text = "of " + CurrencyUtils.formatCurrency(debt.totalAmount, currencyCode),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (!debt.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = debt.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEditClicked) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onDeleteClicked) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onSettleClicked,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLent) Color(0xFF00C853) else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isLent) "Receive / Settle" else "Pay / Settle")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDebtDialog(
    debt: DebtEntity,
    currencyCode: String = "USD",
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: DebtType, totalAmount: Double, remainingAmount: Double, notes: String?) -> Unit
) {
    var personName by remember { mutableStateOf(debt.personName) }
    var totalAmountText by remember { mutableStateOf(String.format(Locale.getDefault(), "%.2f", debt.totalAmount)) }
    var remainingAmountText by remember { mutableStateOf(String.format(Locale.getDefault(), "%.2f", debt.remainingAmount)) }
    var selectedType by remember {
        mutableStateOf(if (debt.type.equals("Borrowed", ignoreCase = true)) DebtType.BORROWED else DebtType.LENT)
    }
    var notes by remember { mutableStateOf(debt.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val currencySymbol = CurrencyUtils.getSymbol(currencyCode)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Debt Record") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = selectedType == DebtType.LENT,
                        onClick = { selectedType = DebtType.LENT },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("I Lent")
                    }
                    SegmentedButton(
                        selected = selectedType == DebtType.BORROWED,
                        onClick = { selectedType = DebtType.BORROWED },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("I Borrowed")
                    }
                }

                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text("Person's Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = totalAmountText,
                    onValueChange = { totalAmountText = it },
                    label = { Text("Total Initial Amount") },
                    prefix = { Text("$currencySymbol ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = remainingAmountText,
                    onValueChange = { remainingAmountText = it },
                    label = { Text("Remaining Unpaid Amount") },
                    prefix = { Text("$currencySymbol ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Reason") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val total = totalAmountText.toDoubleOrNull()
                    val remaining = remainingAmountText.toDoubleOrNull()
                    if (personName.isBlank()) {
                        errorMessage = "Please enter a name"
                        return@Button
                    }
                    if (total == null || total <= 0) {
                        errorMessage = "Please enter a valid total amount"
                        return@Button
                    }
                    if (remaining == null || remaining < 0) {
                        errorMessage = "Please enter a valid remaining amount"
                        return@Button
                    }
                    if (remaining > total) {
                        errorMessage = "Remaining cannot exceed total amount"
                        return@Button
                    }
                    onConfirm(personName.trim(), selectedType, total, remaining, notes.ifBlank { null })
                }
            ) {
                Text("Update Debt")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDebtDialog(
    initialType: DebtType,
    currencyCode: String = "USD",
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: DebtType, amount: Double, notes: String?) -> Unit
) {
    var personName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(initialType) }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val currencySymbol = CurrencyUtils.getSymbol(currencyCode)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Lent / Borrowed") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = selectedType == DebtType.LENT,
                        onClick = { selectedType = DebtType.LENT },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("I Lent (Owed to Me)")
                    }
                    SegmentedButton(
                        selected = selectedType == DebtType.BORROWED,
                        onClick = { selectedType = DebtType.BORROWED },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("I Borrowed")
                    }
                }

                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text("Person's Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    prefix = { Text("$currencySymbol ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (e.g. Dinner, Concert ticket)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (personName.isBlank()) {
                        errorMessage = "Please enter a name"
                        return@Button
                    }
                    if (amount == null || amount <= 0) {
                        errorMessage = "Please enter a valid positive amount"
                        return@Button
                    }
                    onConfirm(personName.trim(), selectedType, amount, notes.ifBlank { null })
                }
            ) {
                Text("Save Debt")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
