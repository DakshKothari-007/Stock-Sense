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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.stocksense.data.model.InventoryProduct

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryContent(
    products: List<InventoryProduct> = emptyList(),
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    selectedFilter: Int = 0,
    onFilterSelected: (Int) -> Unit = {},
    onImportDataClick: () -> Unit = {},
    onMilkProductClick: () -> Unit = {},
    onBiscuitsProductClick: () -> Unit = {},
    onAttaProductClick: () -> Unit = {},
    onTeaProductClick: () -> Unit = {},
    onParleGProductClick: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    val displayProducts = if (products.isNotEmpty()) {
        products
    } else {
        listOf(
            InventoryProduct(
                id = "1",
                name = "Amul Milk · 500 ml",
                sku = "DAI-001",
                price = "₹30",
                status = "Low stock · Minimum 24",
                statusColorHex = 0xFF98621A,
                units = "8",
                iconType = 0
            ),
            InventoryProduct(
                id = "2",
                name = "Britannia Marie · 150 g",
                sku = "SNK-014",
                price = "₹30",
                status = "Slow moving · 6 sold in 30 days",
                statusColorHex = 0xFF98621A,
                units = "96",
                iconType = 1
            ),
            InventoryProduct(
                id = "3",
                name = "Aashirvaad Atta - 5 kg",
                sku = "STP-003",
                price = "₹260",
                status = null,
                statusColorHex = 0x00000000,
                units = "24",
                iconType = 2
            ),
            InventoryProduct(
                id = "4",
                name = "Tata Tea - 250 g",
                sku = "BEV-008",
                price = "₹120",
                status = null,
                statusColorHex = 0x00000000,
                units = "42",
                iconType = 3
            ),
            InventoryProduct(
                id = "5",
                name = "Parle-G · 80 g",
                sku = "SNK-002",
                price = "₹10",
                status = null,
                statusColorHex = 0x00000000,
                units = "120",
                iconType = 1
            )
        )
    }

    val filteredProducts = remember(displayProducts, searchQuery, selectedFilter) {
        displayProducts.filter { item ->
            val matchesSearch = searchQuery.isEmpty() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.sku.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                1 -> item.status?.contains("Low stock", ignoreCase = true) == true
                2 -> item.status?.contains("Slow moving", ignoreCase = true) == true
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header
        Column {
            Text(
                text = "Inventory",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Text(
                text = "Green Basket · 128 products · 15 Oct 2026",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }

        // Find a Product Section
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

            // Search Box
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

        // Your Products List
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Your products",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp
            )

            filteredProducts.forEach { item ->
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
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${item.sku} · ${item.price}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                                if (item.status != null) {
                                    Text(
                                        text = item.status,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(item.statusColorHex),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = item.units,
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
                                    drawPath(path = path, color = Color.Gray, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Import Data Button (Hidden while using search bar)
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
