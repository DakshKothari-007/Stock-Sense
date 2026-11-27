package com.simats.stocksense.feature.reorder.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.simats.stocksense.core.theme.*
import com.simats.stocksense.core.util.NumberFormatUtil
import com.simats.stocksense.core.util.ResponsiveContainer
import com.simats.stocksense.core.util.rememberWindowSizeInfo
import com.simats.stocksense.data.model.PurchaseOrderDto
import com.simats.stocksense.data.model.PurchaseOrderItemDto
import com.simats.stocksense.feature.inventory.icon.ProductAvatar
import com.simats.stocksense.feature.reorder.viewmodel.PurchaseOrdersViewModel

@Composable
fun OrderDetailScreen(
    onBackClick: () -> Unit = {},
    onViewInventoryClick: () -> Unit = {},
    onSessionExpired: () -> Unit = {},
    viewModel: PurchaseOrdersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val order = uiState.selectedOrder
    val primaryColor = MaterialTheme.colorScheme.primary
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var showReceiveDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteRefusedDialog by remember { mutableStateOf(false) }

    // Refresh on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uiState.isSessionExpired) {
        if (uiState.isSessionExpired) {
            onSessionExpired()
        }
    }

    LaunchedEffect(uiState.actionSuccessMessage) {
        uiState.actionSuccessMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearActionMessages()
        }
    }

    LaunchedEffect(uiState.actionErrorMessage) {
        uiState.actionErrorMessage?.let { err ->
            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
            viewModel.clearActionMessages()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CreamBackground
    ) {
        if (order == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Order not found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkDark
                    )
                    BoldButton(
                        text = "Back to Orders",
                        onClick = onBackClick,
                        isPrimary = true
                    )
                }
            }
            return@Surface
        }

        val status = order.status?.lowercase() ?: "placed"
        val isPlaced = status == "placed"
        val isReceived = status == "received" || status == "partially_received" || !order.receivedAt.isNullOrBlank()
        val isCancelled = status == "cancelled"

        val subtitleDate = PurchaseOrdersViewModel.formatOrderDetailDate(
            status = order.status,
            createdAt = order.createdAt,
            receivedAt = order.receivedAt
        )

        val totalUnits = order.totalUnits ?: order.items?.sumOf { it.quantity ?: 0 } ?: 0
        val totalItems = order.totalItems ?: order.items?.size ?: 0
        val totalCostFormatted = NumberFormatUtil.formatIndianRupees(order.totalEstimatedCost ?: 0.0)
        val windowInfo = rememberWindowSizeInfo()

        ResponsiveContainer(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            maxWidth = 640.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = windowInfo.horizontalPadding)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // 1. Header with Shared BoldTopBar
                BoldTopBar(
                    title = order.orderNo ?: "—",
                    subtitle = subtitleDate,
                    onBackClick = onBackClick
                )

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 2. Status Banner
                when {
                    uiState.isSelectedOrderDeleted -> {
                        BoldCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = SoftPink,
                            borderColor = InkDark,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            contentPadding = PaddingValues(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "ORDER DELETED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusDangerRed,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "Order deleted",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "This purchase order has been permanently deleted.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedInk,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    status == "placed" -> {
                        val firstItem = order.items?.firstOrNull()
                        val bannerDetail = if (order.items?.size == 1 && firstItem != null && firstItem.currentStock != null) {
                            val nextStock = (firstItem.currentStock ?: 0) + (firstItem.quantity ?: 0)
                            "When the delivery arrives, tap Receive. ${firstItem.displayName} goes from ${firstItem.currentStock} to $nextStock units."
                        } else {
                            "When the delivery arrives, tap Receive to add $totalUnits units to your stock."
                        }

                        BoldCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = SoftLightBlue,
                            borderColor = InkDark,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            contentPadding = PaddingValues(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "AWAITING DELIVERY",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ThemeState.isDark) OnPastelInk else MainBlue,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "$totalUnits units on the way",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = OnPastelInk,
                                    fontSize = 19.sp
                                )
                                Text(
                                    text = bannerDetail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnPastelMuted,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    status == "received" -> {
                        BoldCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = SoftLavender,
                            borderColor = InkDark,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            contentPadding = PaddingValues(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "DELIVERY RECEIVED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OnPastelInk,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "$totalUnits units added to stock",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = OnPastelInk,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "This purchase order was received. All items have been updated in your inventory.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnPastelMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    else -> { // cancelled
                        BoldCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = SoftPink,
                            borderColor = InkDark,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            contentPadding = PaddingValues(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "ORDER CANCELLED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusDangerRed,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "Order cancelled",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "This purchase order was cancelled. No units were added to your stock.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedInk,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // 3. Section Title: Items in this order
                Text(
                    text = "Items in this order",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkDark,
                    fontSize = 14.sp
                )

                // Items Cards
                order.items?.forEach { item ->
                    OrderDetailItemCard(item = item)
                }

                // 4. Totals Card
                BoldCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = CardWhite,
                    borderColor = InkDark,
                    borderWidth = 2.dp,
                    shadowOffset = 3.dp,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Items",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 13.sp
                            )
                            Text(
                                text = totalItems.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = InkDark,
                                fontSize = 13.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Units",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 13.sp
                            )
                            Text(
                                text = totalUnits.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = InkDark,
                                fontSize = 13.sp
                            )
                        }

                        HorizontalDivider(
                            color = InkDark.copy(alpha = 0.15f),
                            thickness = 1.dp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isPlaced) "Estimated total" else "Total",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 13.sp
                            )
                            Text(
                                text = totalCostFormatted,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = InkDark,
                                fontSize = 20.sp
                            )
                        }
                    }
                }

                // 5. Post-action View Inventory & Actions (Enforce delete & cancel rules)
                val completedMessage = uiState.completedActionMessage
                val isTaskDone = completedMessage != null

                if (completedMessage != null) {
                    BoldCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = SoftLavender,
                        borderColor = InkDark,
                        borderWidth = 2.dp,
                        shadowOffset = 3.dp,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = completedMessage,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnPastelInk,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )

                            BoldButton(
                                modifier = Modifier.fillMaxWidth(),
                                text = "View Inventory",
                                onClick = onViewInventoryClick,
                                isPrimary = true
                            )
                        }
                    }
                }

                when {
                    isPlaced -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BoldButton(
                                text = "Receive order",
                                onClick = { showReceiveDialog = true },
                                enabled = !uiState.isActionRunning && !isTaskDone,
                                isPrimary = true,
                                isLoading = uiState.isActionRunning
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                BoldButton(
                                    modifier = Modifier.weight(1f),
                                    text = "Cancel order",
                                    onClick = { showCancelDialog = true },
                                    enabled = !uiState.isActionRunning && !isTaskDone,
                                    isPrimary = false,
                                    containerColor = SoftPink,
                                    contentColor = StatusDangerRed
                                )

                                BoldButton(
                                    modifier = Modifier.weight(1f),
                                    text = "Delete order",
                                    onClick = { showDeleteDialog = true },
                                    enabled = !uiState.isActionRunning && !isTaskDone,
                                    isPrimary = false,
                                    containerColor = CardWhite,
                                    contentColor = StatusDangerRed
                                )
                            }

                            Text(
                                text = "Receiving adds units to your stock. Open orders with 0 received can be deleted.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    isReceived -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                BoldButton(
                                    modifier = Modifier.weight(1f),
                                    text = "Cancel order",
                                    onClick = { showCancelDialog = true },
                                    enabled = !uiState.isActionRunning && !isTaskDone,
                                    isPrimary = false,
                                    containerColor = SoftPink,
                                    contentColor = StatusDangerRed
                                )

                                BoldButton(
                                    modifier = Modifier.weight(1f),
                                    text = "Delete order",
                                    onClick = { showDeleteRefusedDialog = true },
                                    enabled = !uiState.isActionRunning && !isTaskDone,
                                    isPrimary = false,
                                    containerColor = SoftPink.copy(alpha = 0.5f),
                                    contentColor = MutedInk
                                )
                            }

                            Text(
                                text = "Received orders cannot be deleted to keep stock correct. Cancel instead if needed.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    isCancelled -> {
                        if (order.receivedAt.isNullOrBlank()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                BoldButton(
                                    text = "Delete order",
                                    onClick = { showDeleteDialog = true },
                                    enabled = !uiState.isActionRunning && !isTaskDone,
                                    isPrimary = false,
                                    containerColor = CardWhite,
                                    contentColor = StatusDangerRed
                                )

                                Text(
                                    text = "Permanently removes this cancelled order record.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedInk,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            Text(
                                text = "This order had received items. Cannot be deleted to keep stock correct.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        }

        // Receive Confirm Dialog (Requirement 3)
        if (showReceiveDialog) {
            BoldDialog(
                onDismissRequest = { if (!uiState.isActionRunning) showReceiveDialog = false },
                title = "Receive purchase order?",
                text = "Add $totalUnits units to your stock?",
                confirmButton = {
                    BoldButton(
                        text = "Receive",
                        onClick = {
                            showReceiveDialog = false
                            viewModel.receiveOrder(order)
                        },
                        enabled = !uiState.isActionRunning,
                        isPrimary = true
                    )
                },
                dismissButton = {
                    BoldButton(
                        text = "Cancel",
                        onClick = { showReceiveDialog = false },
                        enabled = !uiState.isActionRunning,
                        isPrimary = false
                    )
                }
            )
        }

        // Cancel Confirm Dialog (Requirement 4)
        if (showCancelDialog) {
            val cancelText = if (isReceived) {
                "Are you sure you want to cancel order ${order.orderNo ?: ""}? Received stock will remain in your inventory."
            } else {
                "Are you sure you want to cancel order ${order.orderNo ?: ""}? This action cannot be undone."
            }

            BoldDialog(
                onDismissRequest = { if (!uiState.isActionRunning) showCancelDialog = false },
                title = "Cancel purchase order?",
                text = cancelText,
                confirmButton = {
                    BoldButton(
                        text = "Cancel order",
                        onClick = {
                            showCancelDialog = false
                            viewModel.cancelOrder(order)
                        },
                        enabled = !uiState.isActionRunning,
                        isPrimary = false,
                        containerColor = StatusDangerRed,
                        contentColor = Color.White
                    )
                },
                dismissButton = {
                    BoldButton(
                        text = "Keep order",
                        onClick = { showCancelDialog = false },
                        enabled = !uiState.isActionRunning,
                        isPrimary = false
                    )
                }
            )
        }

        // Delete Confirm Dialog (For open unreceived orders)
        if (showDeleteDialog) {
            BoldDialog(
                onDismissRequest = { if (!uiState.isActionRunning) showDeleteDialog = false },
                title = "Delete purchase order?",
                text = "Are you sure you want to permanently delete order ${order.orderNo ?: ""}? This action cannot be undone.",
                confirmButton = {
                    BoldButton(
                        text = "Delete",
                        onClick = {
                            showDeleteDialog = false
                            viewModel.deleteOrder(order)
                        },
                        enabled = !uiState.isActionRunning,
                        isPrimary = false,
                        containerColor = StatusDangerRed,
                        contentColor = Color.White
                    )
                },
                dismissButton = {
                    BoldButton(
                        text = "Keep order",
                        onClick = { showDeleteDialog = false },
                        enabled = !uiState.isActionRunning,
                        isPrimary = false
                    )
                }
            )
        }

        // Delete Refused Dialog (For partly received or received orders)
        if (showDeleteRefusedDialog) {
            BoldDialog(
                onDismissRequest = { showDeleteRefusedDialog = false },
                title = "Delete refused",
                text = "Cannot delete purchase order with received items. Cancel the order instead to keep stock correct.",
                confirmButton = {
                    BoldButton(
                        text = "Cancel order instead",
                        onClick = {
                            showDeleteRefusedDialog = false
                            showCancelDialog = true
                        },
                        isPrimary = true
                    )
                },
                dismissButton = {
                    BoldButton(
                        text = "Close",
                        onClick = { showDeleteRefusedDialog = false },
                        isPrimary = false
                    )
                }
            )
        }
    }
}

@Composable
private fun OrderDetailItemCard(item: PurchaseOrderItemDto) {
    val qty = item.quantity ?: 0
    val costPrice = item.costPrice ?: 0.0
    val lineCost = item.lineEstimatedCost ?: (qty * costPrice)
    val lineCostFormatted = NumberFormatUtil.formatIndianRupees(lineCost)
    val unitCostFormatted = NumberFormatUtil.formatIndianRupees(costPrice)

    val stockNowText = item.currentStock?.let { "Stock now: $it" } ?: "Stock now: —"

    BoldCard(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 76.dp),
        backgroundColor = CardWhite,
        borderColor = InkDark,
        borderWidth = 2.dp,
        shadowOffset = 3.dp,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Row 1: Product Avatar, Name & Line Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProductAvatar(
                    name = item.displayName,
                    category = item.category,
                    iconKey = item.resolvedIconKey,
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
                            text = item.displayName,
                            style = MaterialTheme.typography.titleMedium,
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
                            text = lineCostFormatted,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 16.sp,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.wrapContentWidth()
                        )
                    }

                    // SKU · Category
                    val skuCategory = "${item.sku ?: "—"} · ${item.category ?: "—"}"
                    Text(
                        text = skuCategory,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Row 3: Units × Cost & Current Stock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$qty units × $unitCostFormatted",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(end = 8.dp)
                )

                Text(
                    text = stockNowText,
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

@Preview(showBackground = true)
@Composable
fun OrderDetailScreenPreview() {
    StockSenseTheme {
        OrderDetailScreen()
    }
}

