package com.simats.stocksense.feature.inventory.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.simats.stocksense.data.model.InventoryProduct
import com.simats.stocksense.feature.inventory.icon.ProductAvatar
import com.simats.stocksense.feature.inventory.viewmodel.InventoryUiState
import com.simats.stocksense.feature.inventory.viewmodel.InventoryViewModel

/**
 * Stateful InventoryScreen connected to InventoryViewModel with resume auto-refresh.
 */
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = viewModel(),
    onImportDataClick: () -> Unit = {},
    onAddProductClick: () -> Unit = {},
    onProductClick: (InventoryProduct) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Auto-refresh inventory list on screen resume
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

    InventoryScreenContent(
        uiState = uiState,
        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
        onFilterSelected = { viewModel.selectFilter(it) },
        onRetryClick = { viewModel.refresh() },
        onLoadMore = { viewModel.loadMore() },
        onImportDataClick = onImportDataClick,
        onAddProductClick = onAddProductClick,
        onProductClick = onProductClick
    )
}

/**
 * Renders the UI state for the Inventory screen (loading, error, or content).
 */
@Composable
fun InventoryScreenContent(
    uiState: InventoryUiState,
    onSearchQueryChange: (String) -> Unit = {},
    onFilterSelected: (Int) -> Unit = {},
    onRetryClick: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    onImportDataClick: () -> Unit = {},
    onAddProductClick: () -> Unit = {},
    onProductClick: (InventoryProduct) -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CreamBackground
    ) {
        when {
            uiState.isLoading -> {
                // Initial Loading Spinner
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MainBlue,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "Loading inventory...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedInk
                        )
                    }
                }
            }

            uiState.errorMessage != null && uiState.allProducts.isEmpty() -> {
                // Error State with Retry Button
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BoldCard(
                        modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
                        backgroundColor = CardWhite,
                        borderColor = InkDark,
                        borderWidth = 2.5.dp,
                        shadowOffset = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Unable to load inventory",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkDark
                            )
                            Text(
                                text = uiState.errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedInk,
                                textAlign = TextAlign.Center
                            )
                            BoldButton(
                                text = "Retry",
                                onClick = onRetryClick,
                                modifier = Modifier.fillMaxWidth(),
                                isPrimary = true
                            )
                        }
                    }
                }
            }

            else -> {
                InventoryContent(
                    products = uiState.filteredProducts,
                    allProductsCount = uiState.allProducts.size,
                    shopName = uiState.shopName,
                    totalCount = uiState.totalCount,
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = onFilterSelected,
                    isLoadingMore = uiState.isLoadingMore,
                    onLoadMore = onLoadMore,
                    onImportDataClick = onImportDataClick,
                    onAddProductClick = onAddProductClick,
                    onProductClick = onProductClick
                )
            }
        }
    }
}

/**
 * Main layout for Inventory products list, search, filter chips, and empty states.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryContent(
    products: List<InventoryProduct> = emptyList(),
    allProductsCount: Int = products.size,
    shopName: String = "",
    totalCount: Int = products.size,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    selectedFilter: Int = 0,
    onFilterSelected: (Int) -> Unit = {},
    isLoadingMore: Boolean = false,
    onLoadMore: () -> Unit = {},
    onImportDataClick: () -> Unit = {},
    onAddProductClick: () -> Unit = {},
    onProductClick: (InventoryProduct) -> Unit = {}
) {
    val scrollState = rememberScrollState()

    // Pagination: Trigger loadMore when scrolling near bottom
    LaunchedEffect(scrollState.value, scrollState.maxValue) {
        if (scrollState.maxValue > 0 && scrollState.value >= scrollState.maxValue - 200) {
            onLoadMore()
        }
    }

    val displayShopName = shopName.ifBlank { "StockSense Store" }
    val displayDate = NumberFormatUtil.formatCurrentHeaderDate()
    val headerSubtitle = "$displayShopName · $totalCount products · $displayDate"

    val isShopEmpty = allProductsCount == 0 && searchQuery.isEmpty() && selectedFilter == 0
    val isNoSearchResults = products.isEmpty() && (searchQuery.isNotEmpty() || selectedFilter != 0)
    val showBottomImportButton = !isShopEmpty && searchQuery.isEmpty()
    val windowInfo = rememberWindowSizeInfo()

    ResponsiveContainer(
        modifier = Modifier.fillMaxSize(),
        maxWidth = 720.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = windowInfo.horizontalPadding)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Header (Fixed at top)
            Column {
                Text(
                    text = "Inventory",
                    style = MaterialTheme.typography.headlineSmall,
                    color = InkDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = headerSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Find a Product Section (Search & Filter - Fixed at top)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Find a product",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = InkDark,
                    fontSize = 12.sp
                )

                var isFocused by remember { mutableStateOf(false) }
                val showBack = isFocused || searchQuery.isNotEmpty()

                val focusManager = LocalFocusManager.current
                val softwareKeyboardController = LocalSoftwareKeyboardController.current

                val handleBack: () -> Unit = {
                    onSearchQueryChange("")
                    focusManager.clearFocus()
                    softwareKeyboardController?.hide()
                }

                BackHandler(enabled = showBack) {
                    handleBack()
                }

                // Search Box (Debounced typing supported by ViewModel)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused },
                    placeholder = {
                        Text(
                            text = "Search name or SKU",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedInk.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite,
                        focusedBorderColor = InkDark,
                        unfocusedBorderColor = InkDark,
                        focusedTextColor = InkDark,
                        unfocusedTextColor = InkDark,
                        cursorColor = InkDark
                    ),
                    leadingIcon = {
                        if (showBack) {
                            IconButton(onClick = handleBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = InkDark
                                )
                            }
                        } else {
                            Canvas(modifier = Modifier.size(18.dp)) {
                                drawCircle(
                                    color = InkDark,
                                    radius = size.width * 0.35f,
                                    center = Offset(size.width * 0.4f, size.height * 0.4f),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                                drawLine(
                                    color = InkDark,
                                    start = Offset(size.width * 0.65f, size.height * 0.65f),
                                    end = Offset(size.width * 0.95f, size.height * 0.95f),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Filter Chips Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val filters = listOf("All", "Low stock", "Slow moving")
                    filters.forEachIndexed { index, label ->
                        val isSelected = selectedFilter == index
                        BoldChip(
                            text = label,
                            isSelected = isSelected,
                            onClick = { onFilterSelected(index) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Product List Area with Fixed Bottom Import Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Empty State for New Stores
                    if (isShopEmpty) {
                        BoldCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = SoftLightBlue,
                            borderColor = InkDark,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No products in inventory",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Your store has no inventory yet. Import your stock spreadsheet to start tracking products, stock levels, and sales.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkDark,
                                    textAlign = TextAlign.Center,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    BoldButton(
                                        text = "Import Stock",
                                        onClick = onImportDataClick,
                                        modifier = Modifier.weight(1f),
                                        isPrimary = false
                                    )
                                    BoldButton(
                                        text = "Add product",
                                        onClick = onAddProductClick,
                                        modifier = Modifier.weight(1f),
                                        isPrimary = true
                                    )
                                }
                            }
                        }
                    } else if (isNoSearchResults) {
                        // Empty State for Filter/Search with 0 matches
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No products found",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Try searching for a different item or clear filters",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedInk,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        // 4. Your Products List
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Your products",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkDark,
                                fontSize = 16.sp
                            )

                            products.forEach { item ->
                                BoldCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 76.dp),
                                    backgroundColor = CardWhite,
                                    borderColor = InkDark,
                                    borderWidth = 2.dp,
                                    shadowOffset = 3.dp,
                                    shape = RoundedCornerShape(14.dp),
                                    onClick = {
                                        onProductClick(item)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 8.dp)
                                        ) {
                                            ProductAvatar(
                                                name = item.name,
                                                category = item.category,
                                                iconKey = item.iconKey,
                                                size = 40.dp
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = item.name.ifBlank { "—" },
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = InkDark,
                                                    fontSize = 14.sp,
                                                    lineHeight = 18.sp,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                val skuDisplay = item.sku.ifBlank { "—" }
                                                val priceDisplay = item.price.ifBlank { "—" }
                                                Text(
                                                    text = "$skuDisplay · $priceDisplay",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MutedInk,
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (item.status != null) {
                                                    BoldStatusChip(status = item.status)
                                                }
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.wrapContentWidth()
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.End,
                                                modifier = Modifier.wrapContentWidth()
                                            ) {
                                                Text(
                                                    text = item.units.ifBlank { "0" },
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = InkDark,
                                                    fontSize = 15.sp,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                                Text(
                                                    text = "units",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MutedInk,
                                                    fontSize = 10.sp,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Canvas(modifier = Modifier.size(10.dp)) {
                                                val path = Path().apply {
                                                    moveTo(size.width * 0.2f, size.height * 0.1f)
                                                    lineTo(size.width * 0.8f, size.height * 0.5f)
                                                    lineTo(size.width * 0.2f, size.height * 0.9f)
                                                }
                                                drawPath(
                                                    path = path,
                                                    color = InkDark,
                                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Loading More Indicator
                            if (isLoadingMore) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MainBlue,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }

                    // Bottom padding so the last product is not hidden behind the button or the FAB
                    if (showBottomImportButton) {
                        Spacer(modifier = Modifier.height(156.dp))
                    } else {
                        Spacer(modifier = Modifier.height(96.dp))
                    }
                }

                // Fixed Bottom Area (Import Data and Add product buttons side by side)
                if (showBottomImportButton) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(CreamBackground)
                            .padding(bottom = 16.dp, top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BoldButton(
                                text = "Import Data",
                                onClick = onImportDataClick,
                                modifier = Modifier.weight(1f),
                                isPrimary = false
                            )

                            BoldButton(
                                text = "Add product",
                                onClick = onAddProductClick,
                                modifier = Modifier.weight(1f),
                                isPrimary = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryScreenPreview() {
    StockSenseTheme {
        InventoryContent(
            products = listOf(
                InventoryProduct(
                    id = "1",
                    name = "Amul Milk · 500 ml",
                    sku = "DAI-001",
                    price = "₹30",
                    status = "Low stock · Minimum 24",
                    statusColorHex = 0xFFFF9F5A,
                    units = "8",
                    iconType = 0
                )
            )
        )
    }
}

@Preview(name = "Inventory Small 360dp Large Font", showBackground = true, widthDp = 360, fontScale = 1.3f)
@Composable
fun InventoryScreenSmallDevicePreview() {
    StockSenseTheme(themeMode = ThemeMode.LIGHT) {
        InventoryContent(
            products = listOf(
                InventoryProduct(
                    id = "1",
                    name = "Amul Taaza Homogenised Toned Milk 500ml",
                    sku = "DAI-001",
                    price = "₹30",
                    status = "Low stock · Minimum 24",
                    statusColorHex = 0xFFFF9F5A,
                    units = "8",
                    iconType = 0
                )
            )
        )
    }
}

@Preview(name = "Inventory Dark Mode", showBackground = true)
@Composable
fun InventoryScreenDarkPreview() {
    StockSenseTheme(themeMode = ThemeMode.DARK) {
        InventoryContent(
            products = listOf(
                InventoryProduct(
                    id = "1",
                    name = "Amul Taaza Homogenised Toned Milk 500ml",
                    sku = "DAI-001",
                    price = "₹30",
                    status = "Low stock · Minimum 24",
                    statusColorHex = 0xFFFF9F5A,
                    units = "8",
                    iconType = 0
                )
            )
        )
    }
}
