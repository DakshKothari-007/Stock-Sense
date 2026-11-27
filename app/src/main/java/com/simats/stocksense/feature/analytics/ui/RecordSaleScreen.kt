package com.simats.stocksense.feature.analytics.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.stocksense.core.components.*
import com.simats.stocksense.core.network.NetworkClient
import com.simats.stocksense.core.theme.*
import com.simats.stocksense.core.util.NumberFormatUtil
import com.simats.stocksense.core.util.ResponsiveContainer
import com.simats.stocksense.core.util.rememberWindowSizeInfo
import com.simats.stocksense.feature.analytics.model.BillReceiptData
import com.simats.stocksense.feature.analytics.model.BillReceiptItem
import com.simats.stocksense.feature.analytics.viewmodel.BillItem
import com.simats.stocksense.feature.inventory.icon.ProductAvatar
import com.simats.stocksense.feature.analytics.viewmodel.RecordSaleViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecordSaleScreen(
    viewModel: RecordSaleViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onAddProductClick: () -> Unit = {},
    onSessionExpired: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val primaryColor = MaterialTheme.colorScheme.primary
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Reload products and sync bill item stock from backend every time screen resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshStock()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshStock()
    }

    LaunchedEffect(uiState.stockNoticeMessage) {
        uiState.stockNoticeMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    var showDiscardDialog by remember { mutableStateOf(false) }

    // Intercept back click if there are items in the bill to prevent accidental loss
    val handleBack = {
        if (uiState.items.isNotEmpty()) {
            showDiscardDialog = true
        } else {
            onBackClick()
        }
    }

    BackHandler(enabled = uiState.items.isNotEmpty()) {
        showDiscardDialog = true
    }

    LaunchedEffect(uiState.isSessionExpired) {
        if (uiState.isSessionExpired) {
            onSessionExpired()
        }
    }

    val todayFormattedDate = remember {
        SimpleDateFormat("EEE, d MMM yyyy", Locale.ENGLISH).format(Date())
    }
    val windowInfo = rememberWindowSizeInfo()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CreamBackground
    ) {
        ResponsiveContainer(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            maxWidth = 640.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = windowInfo.horizontalPadding)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    val headerSubtitle = remember(uiState.storeName, todayFormattedDate) {
                        if (uiState.storeName.isNotBlank() && uiState.storeName != "—") {
                            "${uiState.storeName} · $todayFormattedDate"
                        } else {
                            todayFormattedDate
                        }
                    }

                    // 1. Shared BoldTopBar
                    BoldTopBar(
                        title = "Record sale",
                        subtitle = headerSubtitle,
                        onBackClick = { handleBack() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Clickable Search Bar Row (Navigates directly to Add Product screen)
                    Surface(
                        onClick = onAddProductClick,
                        shape = RoundedCornerShape(14.dp),
                        color = CardWhite,
                        border = BorderStroke(2.dp, InkDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = InkDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Search name, SKU or barcode",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedInk,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3. Section Title Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Items in this bill",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 14.sp
                        )

                        Text(
                            text = "${uiState.totalItemsCount} items",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Inline Error Banner if save failed (shows real backend message e.g. insufficient stock)
                    if (uiState.errorMessage != null) {
                        BoldCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            backgroundColor = SoftPink,
                            borderColor = InkDark,
                            borderWidth = 2.dp,
                            shadowOffset = 2.dp,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = uiState.errorMessage ?: "Failed to save sale.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusDangerRed,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { viewModel.clearErrorMessage() }) {
                                    Text("Dismiss", color = InkDark, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Inline Stock Adjustment Notice Banner
                    if (uiState.stockNoticeMessage != null) {
                        BoldCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            backgroundColor = if (ThemeState.isDark) SoftLightBlue else SoftPeach,
                            borderColor = InkDark,
                            borderWidth = 2.dp,
                            shadowOffset = 2.dp,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = uiState.stockNoticeMessage ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OnPastelInk,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { viewModel.clearStockNoticeMessage() }) {
                                    Text("Dismiss", color = OnPastelInk, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 4. Bill Items List or Empty State
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        if (uiState.items.isEmpty()) {
                            BoldCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                backgroundColor = CardWhite,
                                borderColor = InkDark,
                                borderWidth = 2.dp,
                                shadowOffset = 3.dp,
                                contentPadding = PaddingValues(vertical = 32.dp, horizontal = 20.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Add a product to start a bill",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = InkDark,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Tap search or the button below to browse products and build your sales receipt.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedInk,
                                        textAlign = TextAlign.Center,
                                        fontSize = 13.sp
                                    )
                                    BoldButton(
                                        text = "Add Product",
                                        onClick = onAddProductClick,
                                        isPrimary = true
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(uiState.items, key = { it.productId }) { item ->
                                    BillItemCard(
                                        item = item,
                                        onIncrement = { viewModel.incrementQuantity(item.productId) },
                                        onDecrement = { viewModel.decrementQuantity(item.productId) }
                                    )
                                }
                            }
                        }
                    }

                    // 5. Bottom Total Summary & Save Sale Button
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Total Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedInk,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${uiState.totalItemsCount} items · ${uiState.totalUnitsCount} units",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedInk,
                                    fontSize = 12.sp
                                )
                            }

                            Text(
                                text = NumberFormatUtil.formatIndianRupees(uiState.totalAmount),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkDark,
                                fontSize = 26.sp
                            )
                        }

                        // Save Sale Button (Disabled when bill is empty or saving)
                        BoldButton(
                            text = "Save sale",
                            onClick = { viewModel.saveSale() },
                            enabled = uiState.items.isNotEmpty() && !uiState.isSaving,
                            isLoading = uiState.isSaving,
                            isPrimary = true
                        )

                        Text(
                            text = "Stock goes down when you save. Date and time are added automatically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

            // Bill Receipt Screen (Requirement 1)
            if (uiState.savedSaleData != null) {
                val savedData = uiState.savedSaleData!!
                val shopName = if (uiState.storeName.isNotBlank() && uiState.storeName != "—") {
                    uiState.storeName
                } else {
                    "StockSense Store"
                }
                val formattedDate = if (!savedData.saleDate.isNullOrBlank()) {
                    savedData.saleDate
                } else {
                    SimpleDateFormat("d MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date())
                }
                val receiptItems = savedData.items.orEmpty().map { itemDto ->
                    BillReceiptItem(
                        name = itemDto.displayName,
                        quantity = itemDto.quantity ?: 1,
                        unitPrice = itemDto.unitPrice ?: 0.0,
                        lineTotal = itemDto.lineTotal ?: ((itemDto.quantity ?: 1) * (itemDto.unitPrice ?: 0.0))
                    )
                }
                val gstin = NetworkClient.getSessionManager()?.getGstin()
                    ?: NetworkClient.getSessionManager()?.getStoreProfile()?.gstin
                val billReceiptData = BillReceiptData(
                    shopName = shopName,
                    billNo = savedData.billNo ?: "—",
                    dateFormatted = formattedDate,
                    items = receiptItems,
                    totalAmount = savedData.totalAmount ?: 0.0,
                    totalQuantity = savedData.totalQuantity ?: receiptItems.sumOf { it.quantity },
                    totalItems = savedData.totalItems ?: receiptItems.size,
                    gstin = gstin
                )

                BillReceiptScreen(
                    bill = billReceiptData,
                    onRecordAnotherSaleClick = {
                        viewModel.dismissSuccessState()
                    },
                    onDoneClick = {
                        viewModel.dismissSuccessState()
                        onBackClick()
                    }
                )
            }

            // Discard Confirmation Dialog (Requirement 10)
            if (showDiscardDialog) {
                BoldDialog(
                    onDismissRequest = { showDiscardDialog = false },
                    title = "Discard current bill?",
                    text = "You have ${uiState.totalItemsCount} item(s) in this bill. If you discard, all added items will be cleared.",
                    confirmButton = {
                        BoldButton(
                            text = "Discard",
                            onClick = {
                                showDiscardDialog = false
                                viewModel.clearBill()
                                onBackClick()
                            },
                            isPrimary = false,
                            containerColor = StatusDangerRed,
                            contentColor = Color.White
                        )
                    },
                    dismissButton = {
                        BoldButton(
                            text = "Keep editing",
                            onClick = { showDiscardDialog = false },
                            isPrimary = false
                        )
                    }
                )
            }
        }
    }
}
}

@Composable
private fun BillItemCard(
    item: BillItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val isAtStockLimit = item.quantity >= item.availableStock

    BoldCard(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 76.dp),
        backgroundColor = CardWhite,
        borderColor = InkDark,
        borderWidth = 2.dp,
        shadowOffset = 3.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Product Avatar, Name & Line Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProductAvatar(
                    name = item.name,
                    category = null,
                    size = 40.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 15.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = NumberFormatUtil.formatIndianRupees(item.lineTotal),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 16.sp,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.wrapContentWidth()
                        )
                    }

                    // SKU · ₹Price each
                    Text(
                        text = "${item.sku} · ${NumberFormatUtil.formatIndianRupees(item.unitPrice)} each",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Bottom Row: Stepper [-] Qty [+] and Stock Hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stepper Capsule
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CardWhite,
                    border = BorderStroke(1.5.dp, InkDark),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDecrement,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease",
                                tint = InkDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = item.quantity.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        IconButton(
                            onClick = onIncrement,
                            enabled = !isAtStockLimit,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = if (isAtStockLimit) MutedInk.copy(alpha = 0.35f) else InkDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Stock Hint
                if (isAtStockLimit) {
                    Text(
                        text = "Only ${item.availableStock} in stock",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = StatusLowStock,
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.wrapContentWidth()
                    )
                } else {
                    Text(
                        text = "${item.availableStock} in stock",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk,
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.wrapContentWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecordSaleScreenPreview() {
    StockSenseTheme {
        RecordSaleScreen()
    }
}

