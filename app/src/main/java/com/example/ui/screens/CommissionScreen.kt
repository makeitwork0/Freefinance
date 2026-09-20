package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.FreelanceProjectEntity
import com.example.data.local.entity.MilestoneEntity
import com.example.data.local.model.ProjectWithMilestones
import com.example.ui.components.EmptyListPlaceholder
import com.example.util.CurrencyUtils
import com.example.viewmodels.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommissionScreen(
    viewModel: DashboardViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.freelanceProjects.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val baseCurrency = uiState.baseCurrency

    var showAddProjectDialog by remember { mutableStateOf(false) }
    var selectedProjectForMilestone by remember { mutableStateOf<FreelanceProjectEntity?>(null) }
    var milestoneToPay by remember { mutableStateOf<MilestoneEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Freelance Commissions", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddProjectDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Project")
            }
        }
    ) { innerPadding ->
        if (projects.isEmpty()) {
            EmptyListPlaceholder(
                icon = Icons.Default.BusinessCenter,
                title = "No Freelance Projects",
                message = "Track client initiatives, payment milestones, and inject pending fees directly into your cash flow forecast.",
                actionLabel = "New Freelance Project",
                onAction = { showAddProjectDialog = true },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(projects, key = { it.project.id }) { item ->
                    ProjectCard(
                        projectWithMilestones = item,
                        currencyCode = baseCurrency,
                        onAddMilestone = { selectedProjectForMilestone = item.project },
                        onMarkMilestonePaid = { milestone -> milestoneToPay = milestone },
                        onDeleteProject = { viewModel.deleteFreelanceProject(item.project) },
                        onDeleteMilestone = { milestone -> viewModel.deleteMilestone(milestone) }
                    )
                }
            }
        }
    }

    // Add Project Dialog
    if (showAddProjectDialog) {
        AddProjectDialog(
            currencyCode = baseCurrency,
            onDismiss = { showAddProjectDialog = false },
            onConfirm = { name, client, fee ->
                viewModel.addFreelanceProject(name, client, fee)
                showAddProjectDialog = false
            }
        )
    }

    // Add Milestone Dialog
    selectedProjectForMilestone?.let { project ->
        AddMilestoneDialog(
            project = project,
            currencyCode = baseCurrency,
            onDismiss = { selectedProjectForMilestone = null },
            onConfirm = { title, amount, date ->
                viewModel.addMilestone(project.id, title, amount, date)
                selectedProjectForMilestone = null
            }
        )
    }

    // Mark Milestone Paid (Select Deposit Account) Dialog
    milestoneToPay?.let { milestone ->
        MarkMilestonePaidDialog(
            milestone = milestone,
            currencyCode = baseCurrency,
            accounts = uiState.accounts,
            onDismiss = { milestoneToPay = null },
            onConfirm = { accountId ->
                viewModel.markMilestoneAsPaid(milestone.id, accountId)
                milestoneToPay = null
            }
        )
    }
}

@Composable
private fun ProjectCard(
    projectWithMilestones: ProjectWithMilestones,
    currencyCode: String = "USD",
    onAddMilestone: () -> Unit,
    onMarkMilestonePaid: (MilestoneEntity) -> Unit,
    onDeleteProject: () -> Unit,
    onDeleteMilestone: (MilestoneEntity) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    val project = projectWithMilestones.project
    val milestones = projectWithMilestones.milestones
    val totalPaid = projectWithMilestones.totalPaid
    val progress = projectWithMilestones.progressRatio
    val percentInt = (progress * 100).toInt()

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
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.projectName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Client: ${project.client}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDeleteProject) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Project",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar & Fee Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Paid: " + CurrencyUtils.formatCurrency(totalPaid, currencyCode) + " ($percentInt%)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00C853),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total Fee: " + CurrencyUtils.formatCurrency(project.totalExpectedFee, currencyCode),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF00C853),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Expandable Milestones List
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MILESTONES (${milestones.size})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        TextButton(onClick = onAddMilestone) {
                            Text("+ Add Milestone", fontSize = 12.sp)
                        }
                    }

                    if (milestones.isEmpty()) {
                        Text(
                            text = "No milestones added yet. Tap '+ Add Milestone' to schedule section payouts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        milestones.forEach { milestone ->
                            MilestoneItemRow(
                                milestone = milestone,
                                currencyCode = currencyCode,
                                onMarkPaid = { onMarkMilestonePaid(milestone) },
                                onDelete = { onDeleteMilestone(milestone) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MilestoneItemRow(
    milestone: MilestoneEntity,
    currencyCode: String = "USD",
    onMarkPaid: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val isPaid = milestone.status.equals("Paid", ignoreCase = true)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPaid) Color(0xFF00C853).copy(alpha = 0.15f) else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPaid) Icons.Default.CheckCircle else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (isPaid) Color(0xFF00C853) else MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = milestone.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = String.format(
                        Locale.getDefault(),
                        "%s • %s",
                        dateFormat.format(milestone.expectedDate),
                        milestone.status
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = CurrencyUtils.formatCurrency(milestone.amount, currencyCode),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isPaid) Color(0xFF00C853) else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(6.dp))

            if (!isPaid) {
                Button(
                    onClick = onMarkPaid,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Mark Paid", fontSize = 11.sp)
                }
            } else {
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AddProjectDialog(
    currencyCode: String = "USD",
    onDismiss: () -> Unit,
    onConfirm: (name: String, client: String, totalFee: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var client by remember { mutableStateOf("") }
    var feeText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val currencySymbol = CurrencyUtils.getSymbol(currencyCode)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Freelance Project") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name (e.g. Website Redesign)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = client,
                    onValueChange = { client = it },
                    label = { Text("Client / Company Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = feeText,
                    onValueChange = { feeText = it },
                    label = { Text("Total Contract Fee") },
                    prefix = { Text("$currencySymbol ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                    val fee = feeText.toDoubleOrNull()
                    if (name.isBlank() || client.isBlank()) {
                        errorMessage = "Please enter project and client names"
                        return@Button
                    }
                    if (fee == null || fee <= 0) {
                        errorMessage = "Please enter a valid total fee"
                        return@Button
                    }
                    onConfirm(name.trim(), client.trim(), fee)
                }
            ) {
                Text("Create Project")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddMilestoneDialog(
    project: FreelanceProjectEntity,
    currencyCode: String = "USD",
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, expectedDate: Date) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var daysAheadText by remember { mutableStateOf("14") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val currencySymbol = CurrencyUtils.getSymbol(currencyCode)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Milestone for ${project.projectName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Milestone Title (e.g. 50% Deposit, Final Delivery)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Milestone Amount") },
                    prefix = { Text("$currencySymbol ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = daysAheadText,
                    onValueChange = { daysAheadText = it },
                    label = { Text("Expected in (Days from now)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    val days = daysAheadText.toIntOrNull() ?: 14
                    if (title.isBlank()) {
                        errorMessage = "Please enter milestone title"
                        return@Button
                    }
                    if (amount == null || amount <= 0) {
                        errorMessage = "Please enter a valid amount"
                        return@Button
                    }
                    val cal = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, days)
                    }
                    onConfirm(title.trim(), amount, cal.time)
                }
            ) {
                Text("Add Milestone")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarkMilestonePaidDialog(
    milestone: MilestoneEntity,
    currencyCode: String = "USD",
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (accountId: Long) -> Unit
) {
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 0L) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Receive Milestone Payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Deposit " + CurrencyUtils.formatCurrency(milestone.amount, currencyCode) + " for '${milestone.title}' into account:",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (accounts.isEmpty()) {
                    Text(
                        text = "No accounts available. Please add an account first.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    val currentSelectedName = accounts.find { it.id == selectedAccountId }?.name ?: accounts.first().name

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = currentSelectedName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Deposit Account") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            accounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text("${acc.name} (${acc.type})") },
                                    onClick = {
                                        selectedAccountId = acc.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedAccountId != 0L) {
                        onConfirm(selectedAccountId)
                    }
                },
                enabled = accounts.isNotEmpty()
            ) {
                Text("Confirm Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
