package com.simats.stocksense.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.stocksense.ui.theme.*

@Composable
fun MainScreen(
    onBackClick: () -> Unit = {},
    onProductClick: () -> Unit = {},
    onReorderRecommendationClick: () -> Unit = {},
    onSalesAnalyticsClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            bottomBar = {
                StockSenseBottomNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> DashboardHomeContent(
                        onProductClick = onProductClick,
                        onAnalyticsClick = onSalesAnalyticsClick
                    )
                    1 -> InventoryContent(onProductClick = onProductClick)
                    2 -> InsightsContent(onRecommendationClick = onReorderRecommendationClick)
                    3 -> ProfileContent(onLogoutClick = onBackClick)
                }
            }
        }
    }
}

@Composable
fun DashboardHomeContent(
    onProductClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val accentColor = Teal500

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // 1. Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Green Basket",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = "Indiranagar, Bengaluru · Thu, 15 Oct 2026",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            // Bell Notification Icon Box
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(20.dp)) {
                    drawCircle(
                        color = primaryColor,
                        radius = size.width * 0.35f,
                        center = Offset(size.width * 0.5f, size.height * 0.45f),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xFF20A486),
                        radius = size.width * 0.15f,
                        center = Offset(size.width * 0.75f, size.height * 0.2f)
                    )
                }
            }
        }

        // 2. Metric Cards (Today's Sales & Inventory Value)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Today's Sales Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Today's sales",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹18,450",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "96 bills · +12% vs yesterday",
                        style = MaterialTheme.typography.bodySmall,
                        color = primaryColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    )
                }
            }

            // Inventory Value Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Inventory value",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹1,48,620",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "128 active products",
                        style = MaterialTheme.typography.bodySmall,
                        color = primaryColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 3. Alerts Row (Low Stock & Slow Moving)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Low Stock Alert Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onProductClick() },
                colors = CardDefaults.cardColors(containerColor = Amber50),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "LOW STOCK",
                        style = MaterialTheme.typography.labelSmall,
                        color = Amber700,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Amul Milk · 8 left ›",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Below 24-unit minimum",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            // Slow Moving Alert Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SLOW MOVING",
                        style = MaterialTheme.typography.labelSmall,
                        color = Amber700,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Marie Biscuits ›",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "96 left · 6 sold / 30 days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 4. Sales This Week Card (Chart)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onAnalyticsClick() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sales this week",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onAnalyticsClick() }
                    ) {
                        Text(
                            text = "Analytics ›",
                            style = MaterialTheme.typography.bodySmall,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "9–15 Oct",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "₹1,20,750",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bar Chart
                val barData = listOf(
                    "9" to 0.45f,
                    "10" to 0.52f,
                    "11" to 0.45f,
                    "12" to 0.60f,
                    "13" to 0.82f,
                    "14" to 0.55f,
                    "15" to 0.70f
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    barData.forEachIndexed { index, (day, ratio) ->
                        val isLast = index == barData.lastIndex
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .fillMaxHeight(ratio)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isLast) primaryColor else accentColor)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = day,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "9–15 Oct 2026 · Daily sales in ₹",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }

        // 5. Your AI Priorities Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your AI priorities",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                Text(
                    text = "All insights ›",
                    style = MaterialTheme.typography.bodySmall,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            // AI Priority Card 1
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onProductClick() },
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(18.dp)) {
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val path = Path().apply {
                                    moveTo(cx, 0f)
                                    quadraticTo(cx, cy, size.width, cy)
                                    quadraticTo(cx, cy, cx, size.height)
                                    quadraticTo(cx, cy, 0f, cy)
                                    quadraticTo(cx, cy, cx, 0f)
                                }
                                drawPath(
                                    path = path,
                                    color = primaryColor,
                                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Reorder 60 Amul Milk units",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "12 units/day · estimated purchase ₹1,620",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Canvas(modifier = Modifier.size(12.dp)) {
                        val path = Path().apply {
                            moveTo(size.width * 0.2f, size.height * 0.1f)
                            lineTo(size.width * 0.8f, size.height * 0.5f)
                            lineTo(size.width * 0.2f, size.height * 0.9f)
                        }
                        drawPath(path = path, color = primaryColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }
            }

            // AI Priority Card 2
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(18.dp)) {
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val path = Path().apply {
                                    moveTo(cx, 0f)
                                    quadraticTo(cx, cy, size.width, cy)
                                    quadraticTo(cx, cy, cx, size.height)
                                    quadraticTo(cx, cy, 0f, cy)
                                    quadraticTo(cx, cy, cx, 0f)
                                }
                                drawPath(
                                    path = path,
                                    color = primaryColor,
                                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Free up slow-moving stock",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Marie Biscuits · try a 10% clearance offer",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Canvas(modifier = Modifier.size(12.dp)) {
                        val path = Path().apply {
                            moveTo(size.width * 0.2f, size.height * 0.1f)
                            lineTo(size.width * 0.8f, size.height * 0.5f)
                            lineTo(size.width * 0.2f, size.height * 0.9f)
                        }
                        drawPath(path = path, color = primaryColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }
            }
        }

        Text(
            text = "Sample store data · Updated 15 Oct, 9:30 am",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}

// Inventory Screen Tab Content
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryContent(
    onImportDataClick: () -> Unit = {},
    onProductClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableIntStateOf(0) } // 0: All, 1: Low stock, 2: Slow moving

    val primaryColor = MaterialTheme.colorScheme.primary

    // Product List Data
    val allProducts = listOf(
        InventoryItem(
            name = "Amul Milk · 500 ml",
            sku = "DAI-001",
            price = "₹30",
            status = "Low stock · Minimum 24",
            statusColor = Amber700,
            units = "8",
            iconType = 0 // Milk
        ),
        InventoryItem(
            name = "Britannia Marie · 150 g",
            sku = "SNK-014",
            price = "₹30",
            status = "Slow moving · 6 sold in 30 days",
            statusColor = Amber700,
            units = "96",
            iconType = 1 // Cookie
        ),
        InventoryItem(
            name = "Aashirvaad Atta - 5 kg",
            sku = "STP-003",
            price = "₹260",
            status = null,
            statusColor = Color.Unspecified,
            units = "24",
            iconType = 2 // Box
        ),
        InventoryItem(
            name = "Tata Tea - 250 g",
            sku = "BEV-008",
            price = "₹120",
            status = null,
            statusColor = Color.Unspecified,
            units = "42",
            iconType = 3 // Cup
        ),
        InventoryItem(
            name = "Parle-G · 80 g",
            sku = "SNK-002",
            price = "₹10",
            status = null,
            statusColor = Color.Unspecified,
            units = "120",
            iconType = 1 // Cookie
        )
    )

    val filteredProducts = remember(searchQuery, selectedFilter) {
        allProducts.filter { item ->
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

            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
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
                        onClick = { selectedFilter = index },
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onProductClick() },
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
                                        color = item.statusColor,
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

        Spacer(modifier = Modifier.height(8.dp))

        // Import Data Button
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

        Spacer(modifier = Modifier.height(16.dp))
    }
}

data class InventoryItem(
    val name: String,
    val sku: String,
    val price: String,
    val status: String?,
    val statusColor: Color,
    val units: String,
    val iconType: Int
)

// AI Insights Content Tab
@Composable
fun InsightsContent(
    onRecommendationClick: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary

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
                text = "AI recommendations",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Text(
                text = "Green Basket - 15 Oct 2026",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }

        // Summary Opportunities Banner
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp)),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas(modifier = Modifier.size(20.dp)) {
                    val path = Path().apply {
                        moveTo(size.width * 0.2f, size.height * 0.52f)
                        lineTo(size.width * 0.42f, size.height * 0.74f)
                        lineTo(size.width * 0.8f, size.height * 0.26f)
                    }
                    drawCircle(color = primaryColor, radius = size.width / 2f, style = Stroke(width = 1.5.dp.toPx()))
                    drawPath(path = path, color = primaryColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "3 opportunities for your shop",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Based on stock levels and sales through 15 Oct.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Recommendation Cards List
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Recommendation Card 1: Reorder Amul Milk
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onRecommendationClick() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    drawRect(
                                        color = primaryColor,
                                        topLeft = Offset(size.width * 0.15f, size.height * 0.2f),
                                        size = Size(size.width * 0.7f, size.height * 0.7f),
                                        style = Stroke(width = 1.5.dp.toPx())
                                    )
                                    drawLine(
                                        color = primaryColor,
                                        start = Offset(size.width * 0.5f, size.height * 0.35f),
                                        end = Offset(size.width * 0.5f, size.height * 0.75f),
                                        strokeWidth = 1.5.dp.toPx()
                                    )
                                    drawLine(
                                        color = primaryColor,
                                        start = Offset(size.width * 0.3f, size.height * 0.55f),
                                        end = Offset(size.width * 0.7f, size.height * 0.55f),
                                        strokeWidth = 1.5.dp.toPx()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Reorder Amul Milk",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "HIGH PRIORITY · REORDER",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = primaryColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Canvas(modifier = Modifier.size(12.dp)) {
                            val path = Path().apply {
                                moveTo(size.width * 0.2f, size.height * 0.1f)
                                lineTo(size.width * 0.8f, size.height * 0.5f)
                                lineTo(size.width * 0.2f, size.height * 0.9f)
                            }
                            drawPath(path = path, color = primaryColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }
                    }

                    Text(
                        text = "8 units left, 12 sold per day. Add 60 units for ₹1,620 to avoid an empty shelf.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            // Recommendation Card 2: Move Marie Biscuits faster
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onRecommendationClick() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Move Marie Biscuits faster",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "SLOW-MOVING INVENTORY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = primaryColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Canvas(modifier = Modifier.size(12.dp)) {
                            val path = Path().apply {
                                moveTo(size.width * 0.2f, size.height * 0.1f)
                                lineTo(size.width * 0.8f, size.height * 0.5f)
                                lineTo(size.width * 0.2f, size.height * 0.9f)
                            }
                            drawPath(path = path, color = primaryColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }
                    }

                    Text(
                        text = "96 packs left; only 6 sold in 30 days. Try 10% off or a tea-time bundle.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            // Recommendation Card 3: A smarter tea-time pair
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onRecommendationClick() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    drawLine(color = primaryColor, start = Offset(size.width * 0.2f, size.height * 0.3f), end = Offset(size.width * 0.8f, size.height * 0.3f), strokeWidth = 1.8.dp.toPx())
                                    drawLine(color = primaryColor, start = Offset(size.width * 0.2f, size.height * 0.5f), end = Offset(size.width * 0.8f, size.height * 0.5f), strokeWidth = 1.8.dp.toPx())
                                    drawLine(color = primaryColor, start = Offset(size.width * 0.2f, size.height * 0.7f), end = Offset(size.width * 0.8f, size.height * 0.7f), strokeWidth = 1.8.dp.toPx())
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "A smarter tea-time pair",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "FREQUENTLY BOUGHT TOGETHER",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = primaryColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Canvas(modifier = Modifier.size(12.dp)) {
                            val path = Path().apply {
                                moveTo(size.width * 0.2f, size.height * 0.1f)
                                lineTo(size.width * 0.8f, size.height * 0.5f)
                                lineTo(size.width * 0.2f, size.height * 0.9f)
                            }
                            drawPath(path = path, color = primaryColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }
                    }

                    Text(
                        text = "Tata Tea + Parle-G appear together in 40% of tea baskets. Offer the pair at ₹125.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Footer Note
        Text(
            text = "AI suggests. You decide. Review the evidence before taking action.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
    }
}

// 6. Bottom Navigation Bar Component
@Composable
fun StockSenseBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf("Home", "Inventory", "Insights", "Profile")

            tabs.forEachIndexed { index, label ->
                val isSelected = selectedTab == index
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onTabSelected(index) }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(20.dp)) {
                            when (index) {
                                0 -> { // Home Icon
                                    val path = Path().apply {
                                        moveTo(size.width * 0.5f, 0f)
                                        lineTo(size.width, size.height * 0.45f)
                                        lineTo(size.width * 0.85f, size.height * 0.45f)
                                        lineTo(size.width * 0.85f, size.height)
                                        lineTo(size.width * 0.15f, size.height)
                                        lineTo(size.width * 0.15f, size.height * 0.45f)
                                        lineTo(0f, size.height * 0.45f)
                                        close()
                                    }
                                    drawPath(
                                        path = path,
                                        color = if (isSelected) primaryColor else Color.Gray,
                                        style = Stroke(width = 1.8.dp.toPx())
                                    )
                                }
                                1 -> { // Inventory Boxes Icon
                                    drawRect(
                                        color = if (isSelected) primaryColor else Color.Gray,
                                        topLeft = Offset(0f, 0f),
                                        size = Size(size.width * 0.42f, size.height * 0.42f),
                                        style = Stroke(width = 1.8.dp.toPx())
                                    )
                                    drawRect(
                                        color = if (isSelected) primaryColor else Color.Gray,
                                        topLeft = Offset(size.width * 0.58f, 0f),
                                        size = Size(size.width * 0.42f, size.height * 0.42f),
                                        style = Stroke(width = 1.8.dp.toPx())
                                    )
                                    drawRect(
                                        color = if (isSelected) primaryColor else Color.Gray,
                                        topLeft = Offset(0f, size.height * 0.58f),
                                        size = Size(size.width * 0.42f, size.height * 0.42f),
                                        style = Stroke(width = 1.8.dp.toPx())
                                    )
                                    drawRect(
                                        color = if (isSelected) primaryColor else Color.Gray,
                                        topLeft = Offset(size.width * 0.58f, size.height * 0.58f),
                                        size = Size(size.width * 0.42f, size.height * 0.42f),
                                        style = Stroke(width = 1.8.dp.toPx())
                                    )
                                }
                                2 -> { // Insights Sparkle Icon
                                    val cx = size.width / 2f
                                    val cy = size.height / 2f
                                    val path = Path().apply {
                                        moveTo(cx, 0f)
                                        quadraticTo(cx, cy, size.width, cy)
                                        quadraticTo(cx, cy, cx, size.height)
                                        quadraticTo(cx, cy, 0f, cy)
                                        quadraticTo(cx, cy, cx, 0f)
                                    }
                                    drawPath(
                                        path = path,
                                        color = if (isSelected) primaryColor else Color.Gray,
                                        style = Stroke(width = 1.8.dp.toPx())
                                    )
                                }
                                3 -> { // Profile Icon
                                    drawCircle(
                                        color = if (isSelected) primaryColor else Color.Gray,
                                        radius = size.width * 0.25f,
                                        center = Offset(size.width * 0.5f, size.height * 0.3f),
                                        style = Stroke(width = 1.8.dp.toPx())
                                    )
                                    val bodyPath = Path().apply {
                                        moveTo(0f, size.height)
                                        quadraticTo(size.width * 0.5f, size.height * 0.55f, size.width, size.height)
                                    }
                                    drawPath(
                                        path = bodyPath,
                                        color = if (isSelected) primaryColor else Color.Gray,
                                        style = Stroke(width = 1.8.dp.toPx())
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileContent(onLogoutClick: () -> Unit = {}) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Store Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Green Basket · Indiranagar, Bengaluru", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onLogoutClick) {
                Text("Log Out")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    StockSenseTheme {
        MainScreen()
    }
}