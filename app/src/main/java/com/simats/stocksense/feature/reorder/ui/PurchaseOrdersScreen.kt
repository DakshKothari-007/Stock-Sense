package com.simats.stocksense.feature.reorder.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.simats.stocksense.core.components.*
import com.simats.stocksense.core.theme.*
import com.simats.stocksense.core.util.NumberFormatUtil
import com.simats.stocksense.core.util.ResponsiveContainer
import com.simats.stocksense.core.util.rememberWindowSizeInfo
import com.simats.stocksense.data.model.PurchaseOrderDto
import com.simats.stocksense.feature.inventory.icon.ProductAvatar
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
        color = CreamBackground
    ) {
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

                // 1. Shared BoldTopBar
                BoldTopBar(
                    title = "Purchase orders",
                    subtitle = headerSubtitle,
                    onBackClick = onBackClick
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Filter Segmented Chips (All, Placed · N, Received, Cancelled)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
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
                        BoldChip(
                            text = label,
                            isSelected = isSelected,
                            onClick = { viewModel.selectFilter(index) }
                        )
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
                            CircularProgressIndicator(color = MainBlue, modifier = Modifier.size(36.dp))
                        }
                    }

                    uiState.errorMessage != null && uiState.orders.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            BoldCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = SoftPink,
                                borderColor = InkDark,
                                borderWidth = 2.dp,
                                shadowOffset = 3.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Unable to load purchase orders",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusDangerRed
                                    )
                                    Text(
                                        text = uiState.errorMessage ?: "Unknown error",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = InkDark,
                                        textAlign = TextAlign.Center
                                    )
                                    BoldButton(
                                        text = "Retry",
                                        onClick = { viewModel.loadOrders(resetPage = true) },
                                        isPrimary = true
                                    )
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
                            BoldCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = CardWhite,
                                borderColor = InkDark,
                                borderWidth = 2.dp,
                                shadowOffset = 3.dp
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
                                        color = InkDark,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Orders you place from Reorder appear here.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedInk,
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
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MainBlue)
                                        } else {
                                            BoldButton(
                                                text = "Load more orders",
                                                onClick = { viewModel.loadNextPage() },
                                                isPrimary = false,
                                                borderWidth = 2.dp,
                                                shadowOffset = 2.dp
                                            )
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
                color = MutedInk,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
        }
    }
}
}

@Composable
private fun PurchaseOrderCard(
    order: PurchaseOrderDto,
    onClick: () -> Unit
) {
    val status = order.status?.lowercase() ?: "placed"

    // Colors for status chip (Bold Outline tokens)
    val (statusBg, statusFg) = when (status) {
        "received" -> SoftLavender to OnPastelInk
        "cancelled" -> SoftPink to boldContentColorFor(SoftPink)
        else -> SoftLightBlue to OnPastelInk // placed
    }

    val firstItem = order.firstItem
    val totalItemsCount = order.totalItems ?: order.items?.size ?: 0
    val extraItemsCount = if (totalItemsCount > 1) {
        totalItemsCount - 1
    } else {
        maxOf(0, (order.items?.size ?: 1) - 1)
    }

    val baseName = order.cardProductName
    val productTitle = if (extraItemsCount > 0 && baseName != "—") {
        "$baseName + $extraItemsCount more"
    } else {
        baseName
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

    BoldCard(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 76.dp),
        backgroundColor = CardWhite,
        borderColor = InkDark,
        borderWidth = 2.dp,
        shadowOffset = 3.dp,
        onClick = onClick
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
                    color = InkDark,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(end = 8.dp)
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusBg,
                    border = BorderStroke(1.5.dp, InkDark),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = order.status?.uppercase() ?: "PLACED",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusFg,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // Row 2: Product Avatar + Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProductAvatar(
                    name = baseName,
                    category = order.cardCategory,
                    iconKey = order.cardIconKey,
                    size = 40.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = productTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkDark,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = metaText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

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
                    color = MutedInk,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = totalAmountFormatted,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = InkDark,
                    fontSize = 17.sp,
                    maxLines = 1
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

