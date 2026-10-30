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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.stocksense.core.theme.StockSenseTheme
import com.simats.stocksense.core.util.NumberFormatUtil
import com.simats.stocksense.data.model.InventoryProduct
import com.simats.stocksense.feature.inventory.viewmodel.InventoryUiState
import com.simats.stocksense.feature.inventory.viewmodel.InventoryViewModel

/**
 * Stateful InventoryScreen connected to InventoryViewModel with resume auto-refresh.
 */
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = viewModel(),
    onImportDataClick: () -> Unit = {},
    onMilkProductClick: () -> Unit = {},
    onBiscuitsProductClick: () -> Unit = {},
    onAttaProductClick: () -> Unit = {},
    onTeaProductClick: () -> Unit = {},
    onParleGProductClick: () -> Unit = {}
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
        onMilkProductClick = onMilkProductClick,
        onBiscuitsProductClick = onBiscuitsProductClick,
        onAttaProductClick = onAttaProductClick,
        onTeaProductClick = onTeaProductClick,
        onParleGProductClick = onParleGProductClick
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
    onMilkProductClick: () -> Unit = {},
    onBiscuitsProductClick: () -> Unit = {},
    onAttaProductClick: () -> Unit = {},
    onTeaProductClick: () -> Unit = {},
    onParleGProductClick: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.fillMaxSize()) {
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
                            color = primaryColor,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "Loading inventory...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Unable to load inventory",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onRetryClick,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry")
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
                    onMilkProductClick = onMilkProductClick,
                    onBiscuitsProductClick = onBiscuitsProductClick,
                    onAttaProductClick = onAttaProductClick,
                    onTeaProductClick = onTeaProductClick,
                    onParleGProductClick = onParleGProductClick
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
    onMilkProductClick: () -> Unit = {},
    onBiscuitsProductClick: () -> Unit = {},
    onAttaProductClick: () -> Unit = {},
    onTeaProductClick: () -> Unit = {},
    onParleGProductClick: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val scrollState = rememberScrollState()

    // Pagination: Trigger loadMore when scrolling near bottom
    LaunchedEffect(scrollState.value, scrollState.maxValue) {
        if (scrollState.maxValue > 0 && scrollState.value >= scrollState.maxValue - 200) {
            onLoadMore()
        }
    }

    val displayShopName = shopName.ifBlank { "Green Basket" }
    val displayDate = NumberFormatUtil.formatCurrentHeaderDate()
    val headerSubtitle = "$displayShopName · $totalCount products · $displayDate"

    val isShopEmpty = allProductsCount == 0 && searchQuery.isEmpty() && selectedFilter == 0
    val isNoSearchResults = products.isEmpty() && (searchQuery.isNotEmpty() || selectedFilter != 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // 1. Header
        Column {
            Text(
                text = "Inventory",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Text(
                text = headerSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }

        // 2. Find a Product Section (Search & Filter)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Find a product",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
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
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                leadingIcon = {
                    if (showBack) {
                        IconButton(onClick = handleBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Canvas(modifier = Modifier.size(18.dp)) {
                            drawCircle(
                                color = Color.Gray,
                                radius = size.width * 0.35f,
                                center = Offset(size.width * 0.4f, size.height * 0.4f),
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                            drawLine(
                                color = Color.Gray,
                                start = Offset(size.width * 0.65f, size.height * 0.65f),
                                end = Offset(size.width * 0.95f, size.height * 0.95f),
                                strokeWidth = 1.8.dp.toPx()
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
                    Surface(
                        onClick = { onFilterSelected(index) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.surface,
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // 3. Empty State for New Stores
        if (isShopEmpty) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No products in inventory",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your store has no inventory yet. Import your stock spreadsheet to start tracking products, stock levels, and sales.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onImportDataClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Import Stock", fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try searching for a different item or clear filters",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
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
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )

                products.forEach { item ->
                    val isMarie = item.name.contains("Marie", ignoreCase = true)
                    val isAtta = item.name.contains("Atta", ignoreCase = true)
                    val isTea = item.name.contains("Tea", ignoreCase = true)
                    val isParleG = item.name.contains("Parle", ignoreCase = true)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                when {
                                    isMarie -> onBiscuitsProductClick()
                                    isAtta -> onAttaProductClick()
                                    isTea -> onTeaProductClick()
                                    isParleG -> onParleGProductClick()
                                    else -> onMilkProductClick()
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp)
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
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.size(20.dp)) {
                                        when (item.iconType) {
                                            0 -> { // Milk Bottle
                                                drawRect(
                                                    color = primaryColor,
                                                    topLeft = Offset(size.width * 0.35f, 0f),
                                                    size = Size(size.width * 0.3f, size.height * 0.3f)
                                                )
                                                drawRect(
                                                    color = primaryColor,
                                                    topLeft = Offset(size.width * 0.2f, size.height * 0.3f),
                                                    size = Size(size.width * 0.6f, size.height * 0.7f)
                                                )
                                            }
                                            1 -> { // Cookie / Biscuit
                                                drawCircle(
                                                    color = primaryColor,
                                                    radius = size.width * 0.45f,
                                                    center = Offset(size.width / 2f, size.height / 2f),
                                                    style = Stroke(width = 1.5.dp.toPx())
                                                )
                                                drawCircle(color = primaryColor, radius = size.width * 0.08f, center = Offset(size.width * 0.35f, size.height * 0.35f))
                                                drawCircle(color = primaryColor, radius = size.width * 0.08f, center = Offset(size.width * 0.65f, size.height * 0.45f))
                                                drawCircle(color = primaryColor, radius = size.width * 0.08f, center = Offset(size.width * 0.45f, size.height * 0.65f))
                                            }
                                            2 -> { // Box / Package
                                                drawRect(
                                                    color = primaryColor,
                                                    topLeft = Offset(size.width * 0.1f, size.height * 0.15f),
                                                    size = Size(size.width * 0.8f, size.height * 0.75f),
                                                    style = Stroke(width = 1.5.dp.toPx())
                                                )
                                                drawLine(
                                                    color = primaryColor,
                                                    start = Offset(size.width * 0.1f, size.height * 0.4f),
                                                    end = Offset(size.width * 0.9f, size.height * 0.4f),
                                                    strokeWidth = 1.5.dp.toPx()
                                                )
                                            }
                                            3 -> { // Tea Cup
                                                drawArc(
                                                    color = primaryColor,
                                                    startAngle = 0f,
                                                    sweepAngle = 180f,
                                                    useCenter = false,
                                                    topLeft = Offset(size.width * 0.15f, size.height * 0.3f),
                                                    size = Size(size.width * 0.7f, size.height * 0.6f),
                                                    style = Stroke(width = 1.5.dp.toPx())
                                                )
                                                drawLine(
                                                    color = primaryColor,
                                                    start = Offset(size.width * 0.15f, size.height * 0.3f),
                                                    end = Offset(size.width * 0.85f, size.height * 0.3f),
                                                    strokeWidth = 1.5.dp.toPx()
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = item.name.ifBlank { "—" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                    val skuDisplay = item.sku.ifBlank { "—" }
                                    val priceDisplay = item.price.ifBlank { "—" }
                                    Text(
                                        text = "$skuDisplay · $priceDisplay",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                    if (item.status != null) {
                                        Text(
                                            text = item.status,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(item.statusColorHex),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = item.units.ifBlank { "0" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "units",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                                if (item.status != null) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Canvas(modifier = Modifier.size(10.dp)) {
                                        val path = Path().apply {
                                            moveTo(size.width * 0.2f, size.height * 0.1f)
                                            lineTo(size.width * 0.8f, size.height * 0.5f)
                                            lineTo(size.width * 0.2f, size.height * 0.9f)
                                        }
                                        drawPath(
                                            path = path,
                                            color = Color.Gray,
                                            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                        )
                                    }
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
                            color = primaryColor,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }

        // 5. Import Data Button (Hidden while using search bar)
        if (searchQuery.isEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onImportDataClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Import Data",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
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
                    statusColorHex = 0xFF98621A,
                    units = "8",
                    iconType = 0
                )
            )
        )
    }
}
