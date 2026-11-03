package com.simats.stocksense.feature.reorder.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.simats.stocksense.core.theme.StockSenseTheme
import com.simats.stocksense.core.util.NumberFormatUtil
import com.simats.stocksense.data.model.PurchaseOrderDto
import com.simats.stocksense.feature.reorder.viewmodel.PurchaseOrdersUiState
import com.simats.stocksense.feature.reorder.viewmodel.PurchaseOrdersViewModel

@Composable
fun PurchaseOrdersScreen(
    onBackClick: () -> Unit = {},
    onOrderClick: (PurchaseOrderDto) -> Unit = {},
    onSessionExpired: () -> Unit = {},
    viewModel: PurchaseOrdersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val primaryColor = MaterialTheme.colorScheme.primary
    val lifecycleOwner = LocalLifecycleOwner.current

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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Header with Back Button and Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        val path = Path().apply {
                            moveTo(size.width * 0.65f, size.height * 0.15f)
                            lineTo(size.width * 0.25f, size.height * 0.5f)
                            lineTo(size.width * 0.65f, size.height * 0.85f)
                        }
                        drawPath(
                            path = path,
                            color = primaryColor,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Purchase orders",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    val ordersCountText = if (uiState.totalOrdersCount > 0) {
                        "${uiState.totalOrdersCount} ${if (uiState.totalOrdersCount == 1) "order" else "orders"}"
                    } else if (uiState.isLoading) {
                        "Loading orders..."
                    } else {
                        "0 orders"
                    }

                    val headerSubtitle = if (uiState.shopName.isNotBlank() && uiState.shopName != "—") {
                        "${uiState.shopName} · $ordersCountText"
                    } else {
                        ordersCountText
                    }

                    Text(
                        text = headerSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Filter Segmented Chips (All, Placed · N, Received, Cancelled)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val placedLabel = if (uiState.placedOrdersCount > 0) {
                    "Placed · ${uiState.placedOrdersCount}"
                } else {
                    "Placed"
                }

                val filterLabels = listOf("All", placedLabel, "Received", "Cancelled")

                filterLabels.forEachIndexed { index, label ->
                    val isSelected = uiState.selectedFilterIndex == index
                    Surface(
                        onClick = { viewModel.selectFilter(index) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Orders List Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    uiState.isLoading && uiState.orders.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = primaryColor, modifier = Modifier.size(36.dp))
                        }
                    }

                    uiState.errorMessage != null && uiState.orders.isEmpty() -> {
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
                                    text = "Unable to load purchase orders",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = uiState.errorMessage ?: "Unknown error",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { viewModel.loadOrders(resetPage = true) },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }

                    uiState.orders.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "No orders yet",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Orders you place from Reorder appear here.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 12.dp)
                        ) {
                            items(uiState.orders, key = { it.id ?: it.orderId ?: it.orderNo ?: "" }) { order ->
                                PurchaseOrderCard(
                                    order = order,
                                    onClick = {
                                        viewModel.selectOrder(order)
                                        onOrderClick(order)
                                    }
                                )
                            }

                            // Pagination Load More
                            val pagination = uiState.pagination
                            val totalPages = pagination?.totalPages ?: 1
                            if (uiState.currentPage < totalPages) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (uiState.isLoadingMore) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = primaryColor)
                                        } else {
                                            TextButton(onClick = { viewModel.loadNextPage() }) {
                                                Text("Load more orders", color = primaryColor, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Bottom Hint
            Text(
                text = "Tap an order to receive or cancel it.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun PurchaseOrderCard(
    order: PurchaseOrderDto,
    onClick: () -> Unit
) {
    val status = order.status?.lowercase() ?: "placed"

    // Colors for status chip
    val (statusBg, statusFg) = when (status) {
        "received" -> Color(0xFF10B981).copy(alpha = 0.15f) to Color(0xFF10B981)
        "cancelled" -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        else -> Color(0xFF0284C7).copy(alpha = 0.18f) to Color(0xFF38BDF8) // placed
    }

    val firstItem = order.items?.firstOrNull()
    val extraItemsCount = (order.items?.size ?: 1) - 1
    val productTitle = if (extraItemsCount > 0 && firstItem != null) {
        "${firstItem.displayName} + $extraItemsCount more"
    } else {
        firstItem?.displayName ?: "—"
    }

    val totalItems = order.totalItems ?: order.items?.size ?: 0
    val totalUnits = order.totalUnits ?: order.items?.sumOf { it.quantity ?: 0 } ?: 0
    val itemsUnitsStr = "$totalItems ${if (totalItems == 1) "item" else "items"} · $totalUnits ${if (totalUnits == 1) "unit" else "units"}"

    val dateFormatted = PurchaseOrdersViewModel.formatOrderCardDate(
        status = order.status,
        createdAt = order.createdAt,
        receivedAt = order.receivedAt
    )

    val metaText = "$itemsUnitsStr · $dateFormatted"

    val totalLabel = if (status == "placed") "Estimated total" else "Total"
    val totalAmountFormatted = NumberFormatUtil.formatIndianRupees(order.totalEstimatedCost ?: 0.0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Order Number and Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderNo ?: "—",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusBg
                ) {
                    Text(
                        text = order.status?.uppercase() ?: "PLACED",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusFg,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Row 2: Product Name (+ N more)
            Text(
                text = productTitle,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Row 3: Meta details (items · units · date)
            Text(
                text = metaText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Row 4: Total Label & Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = totalLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )

                Text(
                    text = totalAmountFormatted,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 17.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PurchaseOrdersScreenPreview() {
    StockSenseTheme {
        PurchaseOrdersScreen()
    }
}

