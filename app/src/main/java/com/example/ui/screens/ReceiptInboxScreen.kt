package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.local.entity.ReceiptEntity
import com.example.data.local.model.TransactionType
import com.example.util.CurrencyUtils
import com.example.util.ReceiptStorageHelper
import com.example.viewmodels.DashboardEvent
import com.example.viewmodels.DashboardViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptInboxScreen(
    viewModel: DashboardViewModel,
    onNavigateBack: () -> Unit,
    initialTriggerCamera: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val pendingReceipts by viewModel.pendingReceipts.collectAsState()
    val allReceipts by viewModel.allReceipts.collectAsState()
    val pendingCount by viewModel.pendingReceiptCount.collectAsState()
    val accounts by viewModel.allAccounts.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val baseCurrency by viewModel.baseCurrency.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Review Queue (One-by-One), 1: All Receipts Archive
    var selectedQueueIndex by remember { mutableIntStateOf(0) }
    var fullImagePreviewPath by remember { mutableStateOf<String?>(null) }

    // Camera Capture State
    var currentTempPhotoFile by remember { mutableStateOf<File?>(null) }
    var currentTempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentTempPhotoFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    viewModel.saveCapturedReceipt(
                        imagePath = file.absolutePath,
                        note = "Quick Pic Receipt"
                    )
                    Toast.makeText(context, "Receipt snapped to Inbox!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = ReceiptStorageHelper.createTempReceiptFile(context)
            val uri = ReceiptStorageHelper.getUriForFile(context, file)
            currentTempPhotoFile = file
            currentTempPhotoUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission needed to snap receipts", Toast.LENGTH_SHORT).show()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedPath = ReceiptStorageHelper.saveUriToReceiptFile(context, uri)
            if (savedPath != null) {
                viewModel.saveCapturedReceipt(
                    imagePath = savedPath,
                    note = "Imported Receipt Photo"
                )
                Toast.makeText(context, "Receipt imported to Inbox!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun launchCamera() {
        val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            val file = ReceiptStorageHelper.createTempReceiptFile(context)
            val uri = ReceiptStorageHelper.getUriForFile(context, file)
            currentTempPhotoFile = file
            currentTempPhotoUri = uri
            cameraLauncher.launch(uri)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Auto-trigger camera if launched via Quick Pic intent
    LaunchedEffect(initialTriggerCamera) {
        if (initialTriggerCamera) {
            launchCamera()
        }
    }

    // Collect ViewModel snackbar events
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is DashboardEvent.ReceiptSnapped -> snackbarHostState.showSnackbar(event.message)
                is DashboardEvent.ReceiptConverted -> snackbarHostState.showSnackbar(event.message)
                is DashboardEvent.ReceiptDiscarded -> snackbarHostState.showSnackbar(event.message)
                is DashboardEvent.Error -> snackbarHostState.showSnackbar("Error: ${event.error}")
                else -> Unit
            }
        }
    }

    // Keep queue index within bounds
    LaunchedEffect(pendingReceipts.size) {
        if (selectedQueueIndex >= pendingReceipts.size && pendingReceipts.isNotEmpty()) {
            selectedQueueIndex = pendingReceipts.size - 1
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Receipt Inbox",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (pendingCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "$pendingCount pending",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Snap receipts fast, review & classify at your own pace",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back_receipt_inbox")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Dashboard"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.testTag("btn_import_gallery_receipt")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = "Import from Photos"
                        )
                    }
                    IconButton(
                        onClick = { launchCamera() },
                        modifier = Modifier.testTag("btn_snap_top_bar")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Take Receipt Picture",
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
                onClick = { launchCamera() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_quick_snap_receipt")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Snap Receipt")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Quick Pic", fontWeight = FontWeight.Bold)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Mode Tabs: One-by-One Review Queue vs All Archive
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ViewCarousel, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Review Queue (${pendingReceipts.size})")
                        }
                    }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ViewModule, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Receipt Archive (${allReceipts.size})")
                        }
                    }
                )
            }

            AnimatedContent(
                targetState = activeTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ReceiptTabTransition"
            ) { tab ->
                when (tab) {
                    0 -> {
                        // Dedicated One-By-One Review Flow
                        if (pendingReceipts.isEmpty()) {
                            EmptyReceiptQueueView(
                                onSnapClick = { launchCamera() },
                                onImportClick = {
                                    photoPickerLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                        } else {
                            val currentReceipt = pendingReceipts.getOrNull(selectedQueueIndex) ?: pendingReceipts.first()
                            SingleReceiptReviewView(
                                receipt = currentReceipt,
                                currentIndex = selectedQueueIndex,
                                totalCount = pendingReceipts.size,
                                accounts = accounts,
                                categories = categories,
                                baseCurrency = baseCurrency,
                                onPrevious = {
                                    if (selectedQueueIndex > 0) selectedQueueIndex--
                                },
                                onNext = {
                                    if (selectedQueueIndex < pendingReceipts.size - 1) selectedQueueIndex++
                                },
                                onConvert = { accountId, amount, type, categoryId, date, note ->
                                    viewModel.convertReceiptToTransaction(
                                        receiptId = currentReceipt.id,
                                        accountId = accountId,
                                        amount = amount,
                                        type = type,
                                        categoryId = categoryId,
                                        date = date,
                                        note = note
                                    )
                                },
                                onDiscard = {
                                    viewModel.discardReceipt(currentReceipt.id)
                                },
                                onDelete = {
                                    viewModel.deleteReceiptPermanently(currentReceipt)
                                },
                                onImageClick = {
                                    fullImagePreviewPath = currentReceipt.imagePath
                                }
                            )
                        }
                    }
                    1 -> {
                        // All Receipts & History Grid
                        ReceiptsArchiveGridView(
                            allReceipts = allReceipts,
                            baseCurrency = baseCurrency,
                            onSelectReceipt = { receipt ->
                                val idx = pendingReceipts.indexOfFirst { it.id == receipt.id }
                                if (idx >= 0) {
                                    selectedQueueIndex = idx
                                    activeTab = 0
                                } else {
                                    fullImagePreviewPath = receipt.imagePath
                                }
                            },
                            onDeleteReceipt = { receipt ->
                                viewModel.deleteReceiptPermanently(receipt)
                            }
                        )
                    }
                }
            }
        }
    }

    // Full Screen Zoom/Image Dialog
    if (fullImagePreviewPath != null) {
        Dialog(
            onDismissRequest = { fullImagePreviewPath = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
                    .clickable { fullImagePreviewPath = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = File(fullImagePreviewPath!!),
                    contentDescription = "Full Screen Receipt",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { fullImagePreviewPath = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Single Receipt Review View: The dedicated one-by-one examination desk
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleReceiptReviewView(
    receipt: ReceiptEntity,
    currentIndex: Int,
    totalCount: Int,
    accounts: List<com.example.data.local.entity.AccountEntity>,
    categories: List<com.example.data.local.entity.CategoryEntity>,
    baseCurrency: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onConvert: (accountId: Long, amount: Double, type: TransactionType, categoryId: Long?, date: Date, note: String?) -> Unit,
    onDiscard: () -> Unit,
    onDelete: () -> Unit,
    onImageClick: () -> Unit
) {
    var chosenType by remember(receipt.id) {
        mutableStateOf(
            if (receipt.suggestedType.equals("Income", ignoreCase = true)) TransactionType.INCOME else TransactionType.EXPENSE
        )
    }
    var amountInput by remember(receipt.id) {
        mutableStateOf(receipt.suggestedAmount?.let { if (it > 0) String.format(Locale.US, "%.2f", it) else "" } ?: "")
    }
    var selectedAccountId by remember(receipt.id, accounts) {
        mutableStateOf(accounts.firstOrNull()?.id ?: 0L)
    }
    var selectedCategoryId by remember(receipt.id, categories, chosenType) {
        val filtered = categories.filter { it.type.equals(chosenType.displayName, ignoreCase = true) }
        mutableStateOf(filtered.firstOrNull()?.id)
    }
    var noteInput by remember(receipt.id) {
        mutableStateOf(receipt.note ?: "")
    }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }
    val currencySymbol = CurrencyUtils.getSymbol(baseCurrency)

    val relevantCategories = remember(categories, chosenType) {
        categories.filter { it.type.equals(chosenType.displayName, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Queue Progress Bar & Arrows
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrevious,
                    enabled = currentIndex > 0,
                    modifier = Modifier.testTag("btn_prev_receipt")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Receipt"
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Receipt ${currentIndex + 1} of $totalCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Snapped: ${dateFormat.format(receipt.capturedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onNext,
                    enabled = currentIndex < totalCount - 1,
                    modifier = Modifier.testTag("btn_next_receipt")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Receipt"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Receipt Image Card (Tap to zoom)
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.25f)
                .clickable { onImageClick() }
                .testTag("receipt_image_preview")
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val imageFile = File(receipt.imagePath)
                if (imageFile.exists()) {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = "Snapped Receipt Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Image file unavailable", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // Overlay hint
                Surface(
                    color = Color.Black.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "🔍 Tap to Zoom",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Classification & Entry Form
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Classify Receipt",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Segmented Toggle: Expense vs Income
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    val isExpense = chosenType == TransactionType.EXPENSE
                    Button(
                        onClick = {
                            chosenType = TransactionType.EXPENSE
                            val firstExp = categories.firstOrNull { it.type.equals("Expense", true) }
                            selectedCategoryId = firstExp?.id
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isExpense) MaterialTheme.colorScheme.errorContainer else Color.Transparent,
                            contentColor = if (isExpense) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_classify_expense")
                    ) {
                        Text(
                            text = "💸 Expense",
                            fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    val isIncome = chosenType == TransactionType.INCOME
                    Button(
                        onClick = {
                            chosenType = TransactionType.INCOME
                            val firstInc = categories.firstOrNull { it.type.equals("Income", true) }
                            selectedCategoryId = firstInc?.id
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isIncome) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            contentColor = if (isIncome) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_classify_income")
                    ) {
                        Text(
                            text = "💰 Income",
                            fontWeight = if (isIncome) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount Input
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) {
                            amountInput = input
                        }
                    },
                    label = { Text("Receipt Amount") },
                    leadingIcon = {
                        Text(
                            text = currencySymbol,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                        )
                    },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_receipt_amount")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Account Selector Chips
                Text(
                    text = if (chosenType == TransactionType.EXPENSE) "Paid With (Account)" else "Deposit To (Account)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { acc ->
                        val isSelected = selectedAccountId == acc.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedAccountId = acc.id },
                            label = { Text(acc.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selector Chips
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyColumn(
                    modifier = Modifier.height(110.dp)
                ) {
                    items(relevantCategories.chunked(3).size) { rowIndex ->
                        val rowItems = relevantCategories.chunked(3)[rowIndex]
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            rowItems.forEach { cat ->
                                val isSelected = selectedCategoryId == cat.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategoryId = cat.id },
                                    label = { Text(cat.name, fontSize = 12.sp, maxLines = 1) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Note / Description Field
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Note / Merchant / Description") },
                    placeholder = { Text("e.g. Grocery Store, Dinner with team") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_receipt_note")
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons: Convert / Discard / Delete
                val parsedAmount = amountInput.toDoubleOrNull() ?: 0.0
                val canConvert = parsedAmount > 0 && selectedAccountId > 0

                Button(
                    onClick = {
                        if (canConvert) {
                            onConvert(
                                selectedAccountId,
                                parsedAmount,
                                chosenType,
                                selectedCategoryId,
                                receipt.capturedAt,
                                noteInput.ifBlank { "Receipt #${receipt.id}" }
                            )
                        }
                    },
                    enabled = canConvert,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (chosenType == TransactionType.EXPENSE) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_convert_receipt_to_ledger")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (chosenType == TransactionType.EXPENSE) {
                            "Convert to Expense (${CurrencyUtils.formatCurrency(parsedAmount, baseCurrency)})"
                        } else {
                            "Convert to Income (${CurrencyUtils.formatCurrency(parsedAmount, baseCurrency)})"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDiscard,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_discard_receipt")
                    ) {
                        Text("Skip / Archive")
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_delete_receipt")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

/**
 * Empty View when all receipts are processed
 */
@Composable
private fun EmptyReceiptQueueView(
    onSnapClick: () -> Unit,
    onImportClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(52.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Receipt Queue is Clean!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You've reviewed and classified all your quick receipt snaps. Take quick pics whenever you make a purchase and review them here anytime.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSnapClick,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(48.dp)
                .testTag("btn_empty_snap_receipt")
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Snap a Receipt Photo", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onImportClick,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Icon(Icons.Default.Collections, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Import from Gallery")
        }
    }
}

/**
 * Grid View of All Historical & Pending Receipts
 */
@Composable
private fun ReceiptsArchiveGridView(
    allReceipts: List<ReceiptEntity>,
    baseCurrency: String,
    onSelectReceipt: (ReceiptEntity) -> Unit,
    onDeleteReceipt: (ReceiptEntity) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, PENDING, CONVERTED

    val filteredList = remember(allReceipts, selectedFilter) {
        when (selectedFilter) {
            "PENDING" -> allReceipts.filter { it.status == "PENDING" }
            "CONVERTED" -> allReceipts.filter { it.status == "CONVERTED" }
            else -> allReceipts
        }
    }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text("All (${allReceipts.size})") }
            )
            FilterChip(
                selected = selectedFilter == "PENDING",
                onClick = { selectedFilter = "PENDING" },
                label = { Text("Pending (${allReceipts.count { it.status == "PENDING" }})") }
            )
            FilterChip(
                selected = selectedFilter == "CONVERTED",
                onClick = { selectedFilter = "CONVERTED" },
                label = { Text("Classified (${allReceipts.count { it.status == "CONVERTED" }})") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No receipts found in this filter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.id }) { receipt ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (receipt.status == "PENDING") MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectReceipt(receipt) }
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            ) {
                                val file = File(receipt.imagePath)
                                if (file.exists()) {
                                    AsyncImage(
                                        model = file,
                                        contentDescription = "Receipt",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Image, contentDescription = null)
                                    }
                                }

                                // Status Badge
                                val isPending = receipt.status == "PENDING"
                                Surface(
                                    color = if (isPending) MaterialTheme.colorScheme.errorContainer else Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(bottomStart = 8.dp),
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Text(
                                        text = if (isPending) "To Review" else "Classified",
                                        color = if (isPending) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF2E7D32),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = dateFormat.format(receipt.capturedAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (receipt.suggestedAmount != null && receipt.suggestedAmount > 0) {
                                    Text(
                                        text = CurrencyUtils.formatCurrency(receipt.suggestedAmount, baseCurrency),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (receipt.suggestedType.equals("Income", true)) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                    )
                                }

                                if (!receipt.note.isNullOrBlank()) {
                                    Text(
                                        text = receipt.note,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
