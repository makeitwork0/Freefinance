package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Standardized TopAppBar with maximum 1-2 primary actions and a structured 3-dot overflow menu.
 * Prevents action button overcrowding and keeps titles unobstructed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceTopAppBar(
    title: String,
    subtitle: String? = null,
    onNavigateBack: (() -> Unit)? = null,
    primaryAction: (@Composable () -> Unit)? = null,
    onNavigateToBudgets: (() -> Unit)? = null,
    onNavigateToSubscriptions: (() -> Unit)? = null,
    onNavigateToRecords: (() -> Unit)? = null,
    onNavigateToCommission: (() -> Unit)? = null,
    onNavigateToDebts: (() -> Unit)? = null,
    onNavigateToCustomize: (() -> Unit)? = null,
    onNavigateToSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val hasOverflowItems = onNavigateToBudgets != null ||
            onNavigateToSubscriptions != null ||
            onNavigateToRecords != null ||
            onNavigateToCommission != null ||
            onNavigateToDebts != null ||
            onNavigateToCustomize != null ||
            onNavigateToSettings != null

    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        actions = {
            if (primaryAction != null) {
                primaryAction()
            }

            if (hasOverflowItems) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More actions",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        if (onNavigateToBudgets != null) {
                            DropdownMenuItem(
                                text = { Text("Budgets & Visualizer") },
                                leadingIcon = { Icon(Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToBudgets()
                                }
                            )
                        }

                        if (onNavigateToSubscriptions != null) {
                            DropdownMenuItem(
                                text = { Text("Subscriptions & Bills") },
                                leadingIcon = { Icon(Icons.Default.EventRepeat, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToSubscriptions()
                                }
                            )
                        }

                        if (onNavigateToRecords != null) {
                            DropdownMenuItem(
                                text = { Text("Master Records Ledger") },
                                leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToRecords()
                                }
                            )
                        }

                        if (onNavigateToCommission != null) {
                            DropdownMenuItem(
                                text = { Text("Freelance Commissions") },
                                leadingIcon = { Icon(Icons.Default.BusinessCenter, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToCommission()
                                }
                            )
                        }

                        if (onNavigateToDebts != null) {
                            DropdownMenuItem(
                                text = { Text("Lent & Borrowed Debts") },
                                leadingIcon = { Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToDebts()
                                }
                            )
                        }

                        if (onNavigateToCustomize != null || onNavigateToSettings != null) {
                            HorizontalDivider()
                        }

                        if (onNavigateToCustomize != null) {
                            DropdownMenuItem(
                                text = { Text("Customize Layout") },
                                leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToCustomize()
                                }
                            )
                        }

                        if (onNavigateToSettings != null) {
                            DropdownMenuItem(
                                text = { Text("Settings & Sync") },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToSettings()
                                }
                            )
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    )
}
