package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AssetEntity
import com.example.util.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class AssetCategoryInfo(
    val name: String,
    val icon: ImageVector,
    val defaultColor: Color
)

val ASSET_CATEGORIES = listOf(
    AssetCategoryInfo("Vehicles", Icons.Default.DirectionsCar, Color(0xFF1E88E5)),
    AssetCategoryInfo("Electronics & Tech", Icons.Default.Computer, Color(0xFF7C4DFF)),
    AssetCategoryInfo("Real Estate & Land", Icons.Default.Home, Color(0xFF43A047)),
    AssetCategoryInfo("Jewelry & Luxury", Icons.Default.Diamond, Color(0xFFFFB300)),
    AssetCategoryInfo("Collectibles & Art", Icons.Default.Palette, Color(0xFFE91E63)),
    AssetCategoryInfo("Tools & Equipment", Icons.Default.Work, Color(0xFFFF7043)),
    AssetCategoryInfo("Other Valuables", Icons.Default.Inventory2, Color(0xFF00ACC1))
)

val ASSET_PALETTE_COLORS = listOf(
    Color(0xFF1E88E5), // Blue
    Color(0xFF7C4DFF), // Deep Purple
    Color(0xFF43A047), // Emerald Green
    Color(0xFFFFB300), // Amber Gold
    Color(0xFFE91E63), // Pink/Magenta
    Color(0xFFFF7043), // Coral
    Color(0xFF00ACC1), // Cyan Teal
    Color(0xFF5E35B1), // Indigo
    Color(0xFF6D4C41), // Bronze Brown
    Color(0xFF546E7A)  // Slate Grey
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddAssetSheet(
    defaultCurrency: String = "USD",
    initialAsset: AssetEntity? = null,
    onDismissRequest: () -> Unit,
    onSaveAsset: (
        name: String,
        category: String,
        estimatedValue: Double,
        purchasePrice: Double,
        purchaseDate: Date?,
        currencyCode: String,
        notes: String?,
        color: Int,
        includeInNetWorth: Boolean
    ) -> Unit,
    onDeleteAsset: ((AssetEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isEditMode = initialAsset != null
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(initialAsset?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(initialAsset?.category ?: ASSET_CATEGORIES.first().name) }
    var estimatedValueStr by remember {
        mutableStateOf(
            if (initialAsset != null && initialAsset.estimatedValue > 0) initialAsset.estimatedValue.toString() else ""
        )
    }
    var purchasePriceStr by remember {
        mutableStateOf(
            if (initialAsset != null && initialAsset.purchasePrice > 0) initialAsset.purchasePrice.toString() else ""
        )
    }
    var currencyCode by remember { mutableStateOf(initialAsset?.currencyCode ?: defaultCurrency) }
    var notes by remember { mutableStateOf(initialAsset?.notes ?: "") }
    var selectedColor by remember {
        mutableIntStateOf(
            initialAsset?.color ?: ASSET_CATEGORIES.first().defaultColor.toArgb()
        )
    }
    var includeInNetWorth by remember { mutableStateOf(initialAsset?.includeInNetWorth ?: true) }
    var purchaseDate by remember { mutableStateOf(initialAsset?.purchaseDate) }

    var currencyDropdownExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var valueError by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier.testTag("add_asset_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditMode) "Edit Asset / Valuable" else "Add Asset / Valuable",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isEditMode && onDeleteAsset != null && initialAsset != null) {
                    TextButton(
                        onClick = { showDeleteConfirmDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Asset",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selection Chips
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ASSET_CATEGORIES.forEach { cat ->
                    val isSelected = selectedCategory == cat.name
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategory = cat.name
                            if (!isEditMode) {
                                selectedColor = cat.defaultColor.toArgb()
                            }
                        },
                        label = { Text(cat.name, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else cat.defaultColor
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Asset Name
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (nameError) nameError = false
                },
                label = { Text("Asset / Item Name *") },
                placeholder = { Text("e.g. 2022 Honda Civic, MacBook Pro M3, Rolex") },
                isError = nameError,
                supportingText = if (nameError) {
                    { Text("Name cannot be empty", color = MaterialTheme.colorScheme.error) }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("asset_name_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Estimated Market Value & Currency
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = estimatedValueStr,
                    onValueChange = {
                        estimatedValueStr = it
                        if (valueError) valueError = false
                    },
                    label = { Text("Current Market Value *") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = valueError,
                    supportingText = if (valueError) {
                        { Text("Please enter a valid value", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.8f)
                        .testTag("asset_estimated_value_input")
                )

                // Currency Dropdown
                ExposedDropdownMenuBox(
                    expanded = currencyDropdownExpanded,
                    onExpandedChange = { currencyDropdownExpanded = it },
                    modifier = Modifier.weight(1.2f)
                ) {
                    OutlinedTextField(
                        value = currencyCode,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Currency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = currencyDropdownExpanded,
                        onDismissRequest = { currencyDropdownExpanded = false }
                    ) {
                        CurrencyUtils.SUPPORTED_CURRENCIES.forEach { curr ->
                            DropdownMenuItem(
                                text = { Text("${curr.code} (${curr.symbol})") },
                                onClick = {
                                    currencyCode = curr.code
                                    currencyDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Original Purchase Price (Optional)
            OutlinedTextField(
                value = purchasePriceStr,
                onValueChange = { purchasePriceStr = it },
                label = { Text("Original Purchase Price (Optional)") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                supportingText = { Text("Used to calculate appreciation / depreciation") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("asset_purchase_price_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Purchase Date Picker
            OutlinedCard(
                onClick = { showDatePicker = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Purchase / Acquisition Date",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = purchaseDate?.let { dateFormat.format(it) } ?: "Not specified (Tap to set)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (purchaseDate != null) FontWeight.Medium else FontWeight.Normal,
                            color = if (purchaseDate != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Date",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Net Worth Inclusion Switch
            OutlinedCard(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (includeInNetWorth) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (includeInNetWorth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Include in Net Worth",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Add value to dashboard total balance",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = includeInNetWorth,
                        onCheckedChange = { includeInNetWorth = it },
                        modifier = Modifier.testTag("asset_net_worth_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Color Palette Selector
            Text(
                text = "Accent Color",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ASSET_PALETTE_COLORS.forEach { color ->
                    val colorArgb = color.toArgb()
                    val isSelected = selectedColor == colorArgb
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { selectedColor = colorArgb }
                            .then(
                                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes / Serial number / Details
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Serial Number / Details (Optional)") },
                placeholder = { Text("e.g. Serial #, Insurance policy, Storage unit 4B") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save & Cancel Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val parsedVal = estimatedValueStr.toDoubleOrNull()
                        val isValidName = name.isNotBlank()
                        val isValidVal = parsedVal != null && parsedVal >= 0

                        if (!isValidName) nameError = true
                        if (!isValidVal) valueError = true

                        if (isValidName && isValidVal && parsedVal != null) {
                            val parsedPurchase = purchasePriceStr.toDoubleOrNull() ?: 0.0
                            onSaveAsset(
                                name,
                                selectedCategory,
                                parsedVal,
                                parsedPurchase,
                                purchaseDate,
                                currencyCode,
                                notes,
                                selectedColor,
                                includeInNetWorth
                            )
                            onDismissRequest()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_asset_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isEditMode) "Update Asset" else "Save Asset")
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = purchaseDate?.time ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            purchaseDate = Date(millis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Select")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog && initialAsset != null && onDeleteAsset != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Asset?") },
            text = { Text("Are you sure you want to remove \"${initialAsset.name}\" from your valuables inventory?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteAsset(initialAsset)
                        onDismissRequest()
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
