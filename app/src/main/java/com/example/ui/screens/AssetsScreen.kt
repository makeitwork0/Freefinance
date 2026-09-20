package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AssetEntity
import com.example.ui.components.ASSET_CATEGORIES
import com.example.ui.components.AddAssetSheet
import com.example.util.CurrencyUtils
import com.example.viewmodels.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsScreen(
    viewModel: DashboardViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val allAssets by viewModel.allAssets.collectAsState()
    val includeAssetsInHero by viewModel.includeAssetsInHero.collectAsState()
    val hideMoney by viewModel.hideMoney.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    var isAddAssetSheetOpen by remember { mutableStateOf(false) }
    var editingAsset by remember { mutableStateOf<AssetEntity?>(null) }

    // Filter assets by search query and category
    val filteredAssets = remember(allAssets, searchQuery, selectedCategoryFilter) {
        allAssets.filter { asset ->
            val matchesCategory = selectedCategoryFilter == "All" || asset.category.equals(selectedCategoryFilter, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    asset.name.contains(searchQuery, ignoreCase = true) ||
                    (asset.notes?.contains(searchQuery, ignoreCase = true) == true) ||
                    asset.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    // Valuation calculations converted into base currency
    val totalAssetsValueConverted = remember(allAssets, uiState.baseCurrency, uiState.exchangeRates) {
        allAssets.sumOf { asset ->
            CurrencyUtils.convert(
                amount = asset.estimatedValue,
                fromCurrency = asset.currencyCode,
                toCurrency = uiState.baseCurrency,
                exchangeRates = uiState.exchangeRates
            )
        }
    }

    val totalPurchaseCostConverted = remember(allAssets, uiState.baseCurrency, uiState.exchangeRates) {
        allAssets.filter { it.purchasePrice > 0 }.sumOf { asset ->
            CurrencyUtils.convert(
                amount = asset.purchasePrice,
                fromCurrency = asset.currencyCode,
                toCurrency = uiState.baseCurrency,
                exchangeRates = uiState.exchangeRates
            )
        }
    }

    val totalGainLoss = totalAssetsValueConverted - totalPurchaseCostConverted
    val gainLossPercentage = if (totalPurchaseCostConverted > 0) {
        (totalGainLoss / totalPurchaseCostConverted) * 100
    } else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Assets & Valuables",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Dashboard"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            editingAsset = null
                            isAddAssetSheetOpen = true
                        },
                        modifier = Modifier.testTag("top_add_asset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Asset"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingAsset = null
                    isAddAssetSheetOpen = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Asset") },
                modifier = Modifier.testTag("fab_add_asset")
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Hero Summary Card
            item {
                AssetsHeroValuationCard(
                    totalAssetsValue = totalAssetsValueConverted,
                    assetCount = allAssets.size,
                    totalPurchaseCost = totalPurchaseCostConverted,
                    gainLossAmount = totalGainLoss,
                    gainLossPercentage = gainLossPercentage,
                    baseCurrency = uiState.baseCurrency,
                    includeInHero = includeAssetsInHero,
                    hideMoney = hideMoney,
                    onToggleIncludeInHero = { viewModel.toggleIncludeAssetsInHero(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            // 2. Search & Category Filter Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search assets, serials, notes...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("asset_search_field")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Filter Chips
                    val filterCategories = listOf("All") + ASSET_CATEGORIES.map { it.name }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filterCategories) { category ->
                            val isSelected = selectedCategoryFilter == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategoryFilter = category },
                                label = { Text(category, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // 3. Assets Inventory List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedCategoryFilter == "All") "All Assets (${filteredAssets.size})" else "$selectedCategoryFilter (${filteredAssets.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap to edit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 4. Asset List Items / Empty State
            if (filteredAssets.isEmpty()) {
                item {
                    AssetsEmptyStateCard(
                        isFiltered = searchQuery.isNotBlank() || selectedCategoryFilter != "All",
                        onAddAssetClick = {
                            editingAsset = null
                            isAddAssetSheetOpen = true
                        },
                        onClearFilters = {
                            searchQuery = ""
                            selectedCategoryFilter = "All"
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            } else {
                items(filteredAssets, key = { it.id }) { asset ->
                    AssetItemCard(
                        asset = asset,
                        baseCurrency = uiState.baseCurrency,
                        exchangeRates = uiState.exchangeRates,
                        hideMoney = hideMoney,
                        onClick = {
                            editingAsset = asset
                            isAddAssetSheetOpen = true
                        },
                        onToggleInclusion = { include ->
                            viewModel.toggleAssetInclusion(asset, include)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }

    // Add / Edit Asset Sheet
    if (isAddAssetSheetOpen) {
        AddAssetSheet(
            defaultCurrency = uiState.baseCurrency,
            initialAsset = editingAsset,
            onDismissRequest = {
                isAddAssetSheetOpen = false
                editingAsset = null
            },
            onSaveAsset = { name, category, estimatedValue, purchasePrice, purchaseDate, currencyCode, notes, color, includeInNetWorth ->
                if (editingAsset != null) {
                    viewModel.updateAsset(
                        editingAsset!!.copy(
                            name = name,
                            category = category,
                            estimatedValue = estimatedValue,
                            purchasePrice = purchasePrice,
                            purchaseDate = purchaseDate,
                            currencyCode = currencyCode,
                            notes = notes,
                            color = color,
                            includeInNetWorth = includeInNetWorth
                        )
                    )
                } else {
                    viewModel.addAsset(
                        name = name,
                        category = category,
                        estimatedValue = estimatedValue,
                        purchasePrice = purchasePrice,
                        purchaseDate = purchaseDate,
                        currencyCode = currencyCode,
                        notes = notes,
                        color = color,
                        includeInNetWorth = includeInNetWorth
                    )
                }
            },
            onDeleteAsset = { assetToDelete ->
                viewModel.deleteAsset(assetToDelete)
            }
        )
    }
}

/**
 * Hero Valuation Card summarizing total valuables worth, appreciation/depreciation, and inclusion toggle.
 */
@Composable
fun AssetsHeroValuationCard(
    totalAssetsValue: Double,
    assetCount: Int,
    totalPurchaseCost: Double,
    gainLossAmount: Double,
    gainLossPercentage: Double,
    baseCurrency: String,
    includeInHero: Boolean,
    hideMoney: Boolean = false,
    onToggleIncludeInHero: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        modifier = modifier.testTag("assets_hero_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "TOTAL VALUABLES WORTH",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyUtils.formatCurrency(totalAssetsValue, baseCurrency, hideMoney),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "$assetCount Items",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            if (totalPurchaseCost > 0 && !hideMoney) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isGain = gainLossAmount >= 0
                    Icon(
                        imageVector = if (isGain) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = if (isGain) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${if (isGain) "+" else ""}${CurrencyUtils.formatCurrency(gainLossAmount, baseCurrency, hideMoney)} (${String.format(Locale.getDefault(), "%.1f", gainLossPercentage)}%) vs purchase cost",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isGain) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Dashboard Hero Net Worth Inclusion Toggle Button / Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                    .clickable { onToggleIncludeInHero(!includeInHero) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (includeInHero) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (includeInHero) "Included in Hero Net Worth" else "Excluded from Hero Net Worth",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (includeInHero) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (includeInHero) "Showing Cash + Valuables on Dashboard" else "Showing Cash Balance only on Dashboard",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = includeInHero,
                    onCheckedChange = { onToggleIncludeInHero(it) },
                    modifier = Modifier.testTag("hero_inclusion_toggle_switch")
                )
            }
        }
    }
}

/**
 * Individual Asset Card representation.
 */
@Composable
fun AssetItemCard(
    asset: AssetEntity,
    baseCurrency: String,
    exchangeRates: List<com.example.data.local.entity.ExchangeRateEntity>,
    hideMoney: Boolean = false,
    onClick: () -> Unit,
    onToggleInclusion: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryIcon = remember(asset.category) {
        ASSET_CATEGORIES.find { it.name.equals(asset.category, ignoreCase = true) }?.icon ?: Icons.Default.Inventory2
    }
    val accentColor = remember(asset.color) { Color(asset.color) }
    val dateFormat = remember { SimpleDateFormat("MMM yyyy", Locale.getDefault()) }

    val convertedValue = remember(asset.estimatedValue, asset.currencyCode, baseCurrency, exchangeRates) {
        CurrencyUtils.convert(
            amount = asset.estimatedValue,
            fromCurrency = asset.currencyCode,
            toCurrency = baseCurrency,
            exchangeRates = exchangeRates
        )
    }

    OutlinedCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier.testTag("asset_item_${asset.id}")
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = asset.category,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = asset.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = asset.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (asset.purchaseDate != null) {
                                Text(
                                    text = "Acquired ${dateFormat.format(asset.purchaseDate)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = CurrencyUtils.formatCurrency(convertedValue, baseCurrency, hideMoney),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (asset.currencyCode != baseCurrency) {
                        Text(
                            text = "(${CurrencyUtils.formatCurrency(asset.estimatedValue, asset.currencyCode, hideMoney)})",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Appreciation / Depreciation and Net Worth Tag
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (asset.purchasePrice > 0 && !hideMoney) {
                    val diff = asset.estimatedValue - asset.purchasePrice
                    val percent = (diff / asset.purchasePrice) * 100
                    val isGain = diff >= 0
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isGain) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = if (isGain) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${if (isGain) "+" else ""}${String.format(Locale.getDefault(), "%.1f", percent)}% (${CurrencyUtils.formatCurrency(diff, asset.currencyCode, hideMoney)})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isGain) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                        )
                    }
                } else {
                    Text(
                        text = if (asset.notes.isNullOrBlank()) "Tangible asset" else asset.notes,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (asset.includeInNetWorth)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clickable { onToggleInclusion(!asset.includeInNetWorth) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (asset.includeInNetWorth) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (asset.includeInNetWorth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (asset.includeInNetWorth) "In Net Worth" else "Excluded",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Medium,
                            color = if (asset.includeInNetWorth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Clean Empty State Card with quick starter suggestions.
 */
@Composable
fun AssetsEmptyStateCard(
    isFiltered: Boolean,
    onAddAssetClick: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isFiltered) "No matching assets found" else "No Assets Logged Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isFiltered)
                    "Try changing your search term or category filters."
                else
                    "Track the value of your vehicles, tech gear, property, jewelry, and valuables to see your complete true net worth.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (isFiltered) {
                Button(
                    onClick = onClearFilters
                ) {
                    Text("Clear Filters")
                }
            } else {
                ExtendedFloatingActionButton(
                    onClick = onAddAssetClick,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add Your First Asset") }
                )
            }
        }
    }
}
