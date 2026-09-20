package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.model.TransactionType
import com.example.util.CurrencyUtils

/**
 * 1-Tap Quick Entry Modal Bottom Sheet with built-in custom NumericKeypad.
 * Allows rapid logging of Income, Expense, or Transfer with instant balance synchronization.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    baseCurrency: String = "USD",
    initialAccountId: Long? = null,
    onDismissRequest: () -> Unit,
    onSaveTransaction: (
        amount: Double,
        type: TransactionType,
        accountId: Long,
        toAccountId: Long?,
        categoryId: Long?,
        labels: List<String>,
        note: String?
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var amountText by remember { mutableStateOf("") }
    var selectedTypeIndex by remember { mutableIntStateOf(1) } // 0: Income, 1: Expense, 2: Transfer
    val transactionTypes = listOf(TransactionType.INCOME, TransactionType.EXPENSE, TransactionType.TRANSFER)
    val selectedType = transactionTypes[selectedTypeIndex]

    var selectedAccountId by remember(accounts, initialAccountId) {
        val defaultId = if (initialAccountId != null && accounts.any { it.id == initialAccountId }) {
            initialAccountId
        } else {
            accounts.firstOrNull()?.id
        }
        mutableStateOf(defaultId)
    }
    var selectedToAccountId by remember(accounts, initialAccountId) {
        val chosenAccId = initialAccountId ?: accounts.firstOrNull()?.id
        val otherAcc = accounts.firstOrNull { it.id != chosenAccId }
        mutableStateOf(otherAcc?.id ?: accounts.getOrNull(1)?.id)
    }

    val relevantCategories = remember(categories, selectedType) {
        val filtered = categories.filter {
            if (selectedType == TransactionType.INCOME) it.type.equals("Income", ignoreCase = true)
            else it.type.equals("Expense", ignoreCase = true)
        }
        if (filtered.isNotEmpty()) filtered else categories
    }

    var selectedCategoryId by remember(relevantCategories) {
        mutableStateOf(relevantCategories.firstOrNull()?.id)
    }

    var accountDropdownExpanded by remember { mutableStateOf(false) }
    var toAccountDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var labelText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDetailsFields by remember { mutableStateOf(false) }

    val handleSave = {
        val parsedAmount = amountText.toDoubleOrNull()
        if (parsedAmount == null || parsedAmount <= 0) {
            errorMessage = "Please enter an amount greater than 0"
        } else if (selectedAccountId == null) {
            errorMessage = "Please select an account"
        } else {
            val parsedLabels = labelText.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            onSaveTransaction(
                parsedAmount,
                selectedType,
                selectedAccountId!!,
                if (selectedType == TransactionType.TRANSFER) selectedToAccountId else null,
                if (selectedType != TransactionType.TRANSFER) selectedCategoryId else null,
                parsedLabels,
                noteText.ifBlank { null }
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Log Transaction",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Transaction Type Segmented Toggle
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                transactionTypes.forEachIndexed { index, type ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = transactionTypes.size),
                        onClick = { selectedTypeIndex = index },
                        selected = index == selectedTypeIndex,
                        icon = {
                            val icon = when (type) {
                                TransactionType.INCOME -> Icons.Default.ArrowDownward
                                TransactionType.EXPENSE -> Icons.Default.ArrowUpward
                                TransactionType.TRANSFER -> Icons.Default.SwapHoriz
                            }
                            Icon(imageVector = icon, contentDescription = null)
                        },
                        label = { Text(type.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val selectedAccount = accounts.find { it.id == selectedAccountId } ?: accounts.firstOrNull()
            val currencyCode = selectedAccount?.currencyCode ?: baseCurrency
            val currencySymbol = CurrencyUtils.getSymbol(currencyCode)

            val themeColor = when (selectedType) {
                TransactionType.INCOME -> Color(0xFF00C853)
                TransactionType.EXPENSE -> Color(0xFFD50000)
                TransactionType.TRANSFER -> MaterialTheme.colorScheme.primary
            }

            // Big Amount Display Box
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (amountText.isNotEmpty()) themeColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_display_box")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Amount ($currencyCode)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$currencySymbol ",
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                            fontWeight = FontWeight.Bold,
                            color = themeColor
                        )
                        Text(
                            text = amountText.ifEmpty { "0.00" },
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 36.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = if (amountText.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else themeColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Account and Category Selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Source Account Dropdown
                ExposedDropdownMenuBox(
                    expanded = accountDropdownExpanded,
                    onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedAccount?.name ?: "Account",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (selectedType == TransactionType.TRANSFER) "From" else "Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = accountDropdownExpanded,
                        onDismissRequest = { accountDropdownExpanded = false }
                    ) {
                        accounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text("${account.name} (${account.currencyCode})") },
                                onClick = {
                                    selectedAccountId = account.id
                                    accountDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Category or Transfer To Dropdown
                if (selectedType == TransactionType.TRANSFER) {
                    val selectedToAccount = accounts.find { it.id == selectedToAccountId }
                    ExposedDropdownMenuBox(
                        expanded = toAccountDropdownExpanded,
                        onExpandedChange = { toAccountDropdownExpanded = !toAccountDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedToAccount?.name ?: "To Account",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("To") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toAccountDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = toAccountDropdownExpanded,
                            onDismissRequest = { toAccountDropdownExpanded = false }
                        ) {
                            accounts.filter { it.id != selectedAccountId }.forEach { account ->
                                DropdownMenuItem(
                                    text = { Text("${account.name} (${account.currencyCode})") },
                                    onClick = {
                                        selectedToAccountId = account.id
                                        toAccountDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    val selectedCategory = relevantCategories.find { it.id == selectedCategoryId }

                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedCategory?.name ?: "Category",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            relevantCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategoryId = category.id
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Optional note and labels toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDetailsFields = !showDetailsFields }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showDetailsFields) "Hide note & tags" else "+ Add note or tags",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (showDetailsFields) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note / Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = labelText,
                    onValueChange = { labelText = it },
                    label = { Text("Tags / Labels (comma separated)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Built-in Numeric Keypad
            NumericKeypad(
                onDigitClick = { digit ->
                    errorMessage = null
                    if (amountText.contains(".")) {
                        val parts = amountText.split(".")
                        if (parts.size > 1 && parts[1].length < 2) {
                            amountText += digit
                        }
                    } else {
                        if (amountText.length < 9) {
                            if (amountText == "0") amountText = digit else amountText += digit
                        }
                    }
                },
                onDecimalClick = {
                    errorMessage = null
                    if (!amountText.contains(".")) {
                        amountText = if (amountText.isEmpty()) "0." else "$amountText."
                    }
                },
                onBackspaceClick = {
                    errorMessage = null
                    if (amountText.isNotEmpty()) {
                        amountText = amountText.dropLast(1)
                    }
                },
                onClearClick = {
                    amountText = ""
                    errorMessage = null
                },
                onEnterClick = {
                    handleSave()
                }
            )
        }
    }
}
