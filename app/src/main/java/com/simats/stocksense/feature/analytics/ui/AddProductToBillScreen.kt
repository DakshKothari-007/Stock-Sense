package com.simats.stocksense.feature.analytics.ui

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.simats.stocksense.data.model.NetworkInventoryProduct
import com.simats.stocksense.feature.inventory.icon.ProductAvatar
import com.simats.stocksense.feature.analytics.viewmodel.RecordSaleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductToBillScreen(
    viewModel: RecordSaleViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onDoneClick: () -> Unit = {},
    onSessionExpired: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val primaryColor = MaterialTheme.colorScheme.primary
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Reload products from backend on resume so stock is never stale
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
            viewModel.clearStockNoticeMessage()
        }
    }

    LaunchedEffect(uiState.isSessionExpired) {
        if (uiState.isSessionExpired) {
            onSessionExpired()
        }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = windowInfo.horizontalPadding)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                val productsCountSubtitle = remember(uiState.totalShopProductsCount, uiState.isSearching) {
                    if (uiState.totalShopProductsCount > 0) {
                        "${uiState.totalShopProductsCount} products in your shop"
                    } else if (uiState.isSearching) {
                        "Loading products..."
                    } else {
                        "— products in your shop"
                    }
                }

                // 1. Shared BoldTopBar
                BoldTopBar(
                    title = "Add product",
                    subtitle = productsCountSubtitle,
                    onBackClick = onBackClick
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Search Field (Barcode scanner hidden per requirement 7; typing barcode matches SKU)
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Search name, SKU or barcode",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedInk,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = InkDark
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InkDark,
                        unfocusedBorderColor = InkDark,
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite,
                        focusedTextColor = InkDark,
                        unfocusedTextColor = InkDark,
                        cursorColor = InkDark
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Filter Segmented Chips (All, Fast selling, Low stock)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filterLabels = listOf("ALL", "FAST SELLING", "LOW STOCK")
                    filterLabels.forEachIndexed { index, label ->
                        BoldChip(
                            text = label,
                            selected = uiState.selectedFilterIndex == index,
                            onClick = { viewModel.selectFilter(index) },
                            selectedColor = MainYellow
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Products List Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (uiState.isSearching && uiState.searchResults.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MainBlue, modifier = Modifier.size(36.dp))
                        }
                    } else if (uiState.searchErrorMessage != null && uiState.searchResults.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = uiState.searchErrorMessage ?: "Failed to load products.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MutedInk
                                )
                                BoldButton(
                                    text = "Retry",
                                    onClick = { viewModel.loadProducts() },
                                    isPrimary = true
                                )
                            }
                        }
                    } else if (uiState.searchResults.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No products found matching your search.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedInk
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(uiState.searchResults, key = { it.id?.toString() ?: it.sku ?: "" }) { product ->
                                AddProductRowCard(
                                    product = product,
                                    quantityInBill = uiState.items.find {
                                        val idInt = when (val id = product.id) {
                                            is Number -> id.toInt()
                                            is String -> id.toDoubleOrNull()?.toInt() ?: -1
                                            else -> -1
                                        }
                                        it.productId == idInt
                                    }?.quantity ?: 0,
                                    onAddClick = { viewModel.addProductToBill(product) }
                                )
                            }
                        }
                    }
                }

                // 5. Bottom "Done" Action Button
                val buttonText = if (uiState.totalUnitsCount > 0) {
                    "Done · ${uiState.totalUnitsCount} units in bill"
                } else {
                    "Done"
                }
                BoldButton(
                    text = buttonText,
                    onClick = onDoneClick,
                    isPrimary = true
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AddProductRowCard(
    product: NetworkInventoryProduct,
    quantityInBill: Int,
    onAddClick: () -> Unit
) {
    val availableStock = RecordSaleViewModel.extractProductUnits(product)
    val isOutOfStock = availableStock <= 0
    val isLowStock = !isOutOfStock && (
        (product.minStockThreshold != null && availableStock <= product.minStockThreshold) ||
        product.status.equals("low_stock", ignoreCase = true) ||
        product.status?.contains("Low", ignoreCase = true) == true
    )

    val priceDisplay = when (val p = product.price) {
        is Number -> NumberFormatUtil.formatIndianRupees(p.toDouble())
        is String -> {
            val parsed = p.replace("₹", "").trim().toDoubleOrNull()
            if (parsed != null) NumberFormatUtil.formatIndianRupees(parsed) else p
        }
        else -> product.rawPrice?.let { NumberFormatUtil.formatIndianRupees(it) } ?: "—"
    }

    BoldCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = CardWhite,
        borderColor = InkDark,
        borderWidth = 2.dp,
        shadowOffset = 3.dp,
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Avatar
            ProductAvatar(
                name = product.name,
                category = product.category,
                size = 40.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Middle Column: Name, SKU, Price & Stock
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name ?: "—",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkDark,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val skuText = product.sku ?: "—"
                    Text(
                        text = "$skuText · $priceDisplay",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isLowStock) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SoftLowStock,
                            border = BorderStroke(1.dp, InkDark)
                        ) {
                            Text(
                                text = "Low · $availableStock left",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = InkDark,
                                fontSize = 10.sp
                            )
                        }
                    } else if (!isOutOfStock) {
                        Text(
                            text = "· $availableStock in stock",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Button: Add, Added · N, or Out of stock
            if (isOutOfStock) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PaleBlue.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, InkDark.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Out of stock",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MutedInk,
                        fontSize = 11.sp
                    )
                }
            } else if (quantityInBill > 0) {
                Surface(
                    onClick = {
                        if (quantityInBill < availableStock) {
                            onAddClick()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = SoftLavender,
                    border = BorderStroke(1.5.dp, InkDark)
                ) {
                    Text(
                        text = "Added · $quantityInBill",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkDark,
                        fontSize = 12.sp
                    )
                }
            } else {
                Surface(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(10.dp),
                    color = MainYellow,
                    border = BorderStroke(1.5.dp, InkDark)
                ) {
                    Text(
                        text = "Add",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkDark,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddProductToBillScreenPreview() {
    StockSenseTheme {
        AddProductToBillScreen()
    }
}

