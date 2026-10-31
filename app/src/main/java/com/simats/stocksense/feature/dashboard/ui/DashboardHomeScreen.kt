
package com.simats.stocksense.feature.dashboard.ui

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.stocksense.core.theme.*
import com.simats.stocksense.core.util.NumberFormatUtil
import com.simats.stocksense.data.model.DashboardAiPriority
import com.simats.stocksense.data.model.DashboardSummaryData
import com.simats.stocksense.feature.dashboard.viewmodel.DashboardUiState
import com.simats.stocksense.feature.dashboard.viewmodel.DashboardViewModel

/**
 * Stateful DashboardHomeScreen that collects StateFlow from DashboardViewModel.
 */
@Composable
fun DashboardHomeScreen(
    viewModel: DashboardViewModel = viewModel(),
    onNotificationsClick: () -> Unit = {},
    onLowStockClick: () -> Unit = {},
    onSlowMovingClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onInsightsClick: () -> Unit = {},
    onReorderMilkClick: () -> Unit = {},
    onFreeUpStockClick: () -> Unit = {},
    onImportClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Auto-refresh when screen resumes
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

    DashboardHomeScreenContent(
        uiState = uiState,
        onRefresh = { viewModel.refresh() },
        onNotificationsClick = onNotificationsClick,
        onLowStockClick = onLowStockClick,
        onSlowMovingClick = onSlowMovingClick,
        onAnalyticsClick = onAnalyticsClick,
        onInsightsClick = onInsightsClick,
        onReorderMilkClick = onReorderMilkClick,
        onFreeUpStockClick = onFreeUpStockClick,
        onImportClick = onImportClick
    )
}

/**
 * Renders the dashboard screen state (Loading, Error with Retry, or Real Dashboard Content).
 */
@Composable
fun DashboardHomeScreenContent(
    uiState: DashboardUiState,
    onRefresh: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onLowStockClick: () -> Unit = {},
    onSlowMovingClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onInsightsClick: () -> Unit = {},
    onReorderMilkClick: () -> Unit = {},
    onFreeUpStockClick: () -> Unit = {},
    onImportClick: () -> Unit = {}
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
                            text = "Loading dashboard...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            uiState.errorMessage != null && uiState.dashboardData == null -> {
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
                            text = "Unable to load dashboard",
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
                            onClick = onRefresh,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            uiState.dashboardData != null -> {
                DashboardHomeContent(
                    data = uiState.dashboardData,
                    unreadCount = uiState.unreadCount,
                    lastUpdatedText = uiState.lastUpdatedText,
                    onNotificationsClick = onNotificationsClick,
                    onLowStockClick = onLowStockClick,
                    onSlowMovingClick = onSlowMovingClick,
                    onAnalyticsClick = onAnalyticsClick,
                    onInsightsClick = onInsightsClick,
                    onReorderMilkClick = onReorderMilkClick,
                    onFreeUpStockClick = onFreeUpStockClick,
                    onImportClick = onImportClick
                )
            }
        }
    }
}

/**
 * Main dashboard content populated with real backend data from GET /dashboard/summary.php.
 */
@Composable
fun DashboardHomeContent(
    data: DashboardSummaryData,
    unreadCount: Int = 0,
    lastUpdatedText: String = "",
    onNotificationsClick: () -> Unit = {},
    onLowStockClick: () -> Unit = {},
    onSlowMovingClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onInsightsClick: () -> Unit = {},
    onReorderMilkClick: () -> Unit = {},
    onFreeUpStockClick: () -> Unit = {},
    onImportClick: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val accentColor = Teal500

    val storeName = data.store?.name?.takeIf { it.isNotBlank() } ?: "—"
    val storeLocation = data.store?.location?.takeIf { it.isNotBlank() } ?: "—"
    val todayHeaderDate = NumberFormatUtil.formatCurrentHeaderDate()

    val totalActiveProducts = data.inventory?.totalActiveProducts ?: 0
    val isNewEmptyStore = totalActiveProducts == 0 && (data.sales?.todaySalesAmount ?: 0.0) == 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // 1. Header Row (Shop name, location, and notification bell)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = storeName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    maxLines = 1
                )
                Text(
                    text = if (todayHeaderDate.isNotEmpty()) "$storeLocation · $todayHeaderDate" else storeLocation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Bell Notification Icon Box
            val bellIconColor = MaterialTheme.colorScheme.onSurface
            val bellBadgeColor = primaryColor
            val bellSurfaceColor = MaterialTheme.colorScheme.surface

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(bellSurfaceColor)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .clickable { onNotificationsClick() },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(22.dp)) {
                    val w = size.width
                    val h = size.height

                    // Top stem
                    drawLine(
                        color = bellIconColor,
                        start = Offset(w * 0.5f, h * 0.10f),
                        end = Offset(w * 0.5f, h * 0.20f),
                        strokeWidth = 1.8.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Bell body
                    val bellPath = Path().apply {
                        moveTo(w * 0.5f, h * 0.20f)
                        cubicTo(w * 0.32f, h * 0.20f, w * 0.22f, h * 0.40f, w * 0.22f, h * 0.60f)
                        lineTo(w * 0.12f, h * 0.76f)
                        lineTo(w * 0.88f, h * 0.76f)
                        lineTo(w * 0.78f, h * 0.60f)
                        cubicTo(w * 0.78f, h * 0.40f, w * 0.68f, h * 0.20f, w * 0.5f, h * 0.20f)
                        close()
                    }
                    drawPath(
                        path = bellPath,
                        color = bellIconColor,
                        style = Stroke(
                            width = 1.8.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Bell clapper
                    val clapperPath = Path().apply {
                        moveTo(w * 0.40f, h * 0.82f)
                        cubicTo(w * 0.40f, h * 0.94f, w * 0.60f, h * 0.94f, w * 0.60f, h * 0.82f)
                    }
                    drawPath(
                        path = clapperPath,
                        color = bellIconColor,
                        style = Stroke(
                            width = 1.8.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Notification badge dot (Shown ONLY when unreadCount > 0)
                    if (unreadCount > 0) {
                        drawCircle(
                            color = bellBadgeColor,
                            radius = w * 0.15f,
                            center = Offset(w * 0.78f, h * 0.20f)
                        )
                        drawCircle(
                            color = bellSurfaceColor,
                            radius = w * 0.15f,
                            center = Offset(w * 0.78f, h * 0.20f),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
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
                        text = NumberFormatUtil.formatIndianRupees(data.sales?.todaySalesAmount),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val bills = data.sales?.todayBillsCount ?: 0
                    Text(
                        text = "${NumberFormatUtil.formatIndianNumber(bills)} bills today",
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
                        text = NumberFormatUtil.formatIndianRupees(data.inventory?.totalStockValue),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${NumberFormatUtil.formatIndianNumber(totalActiveProducts)} active products",
                        style = MaterialTheme.typography.bodySmall,
                        color = primaryColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Empty Store State Banner for newly registered shops
        if (isNewEmptyStore) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome to StockSense!",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Import your stock to get started with inventory tracking, low-stock alerts, and AI insights.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onImportClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Import Stock", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Alerts Row (Low Stock & Slow Moving)
        val lowStockCount = data.inventory?.lowStockCount ?: 0
        val topLowItem = data.alerts?.lowStockItems?.firstOrNull()

        val slowMovingMetadata = data.alerts?.slowMovingMetadata
        val hasSalesHistory = slowMovingMetadata?.hasEnoughSalesHistory ?: false
        val slowMovingCount = data.inventory?.slowMovingCount ?: 0
        val topSlowItem = data.alerts?.slowMovingItems?.firstOrNull()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Low Stock Alert Card
            val isLowStockActive = lowStockCount > 0 && topLowItem != null
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onLowStockClick() },
                colors = CardDefaults.cardColors(
                    containerColor = if (isLowStockActive) Amber50 else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "LOW STOCK",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isLowStockActive) Amber700 else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (isLowStockActive) {
                        val lowName = topLowItem?.name?.takeIf { it.isNotBlank() } ?: "Item"
                        val lowUnits = topLowItem?.units ?: 0
                        val lowMin = topLowItem?.minStockThreshold ?: 0
                        Text(
                            text = "$lowName · $lowUnits left ›",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Below $lowMin-unit minimum",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    } else {
                        Text(
                            text = "Nothing needs attention",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "All stock levels healthy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            // Slow Moving Alert Card
            val isSlowMovingActive = hasSalesHistory && slowMovingCount > 0 && topSlowItem != null
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSlowMovingClick() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SLOW MOVING",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSlowMovingActive) Amber700 else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    when {
                        !hasSalesHistory -> {
                            Text(
                                text = "Sales history needed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = slowMovingMetadata?.message ?: "Requires 30 days history",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                maxLines = 2
                            )
                        }
                        isSlowMovingActive -> {
                            val slowName = topSlowItem?.name?.takeIf { it.isNotBlank() } ?: "Item"
                            val slowUnits = topSlowItem?.units ?: 0
                            val slowSold = topSlowItem?.soldLast30Days ?: 0
                            Text(
                                text = "$slowName ›",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$slowUnits left · $slowSold sold / 30 days",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                        else -> {
                            Text(
                                text = "Nothing needs attention",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "No slow-moving inventory",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // 4. Sales This Week Card (Chart)
        val rawPoints = data.sales?.dailySalesPoints ?: emptyList()
        // Take last 7 days points
        val chartPoints = if (rawPoints.size > 7) rawPoints.takeLast(7) else rawPoints
        val maxAmount = chartPoints.maxOfOrNull { it.amount ?: 0.0 } ?: 0.0

        val firstLabel = chartPoints.firstOrNull()?.dayLabel ?: ""
        val lastLabel = chartPoints.lastOrNull()?.dayLabel ?: ""
        val weekDateRangeTag = if (firstLabel.isNotEmpty() && lastLabel.isNotEmpty()) {
            val firstDay = firstLabel.split(" ").firstOrNull() ?: firstLabel
            "$firstDay–$lastLabel"
        } else {
            "Past 7 days"
        }

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
                                text = weekDateRangeTag,
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
                    text = NumberFormatUtil.formatIndianRupees(data.sales?.weeklySalesTotal),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bar Chart dynamically rendered from dailySalesPoints
                if (chartPoints.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(85.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        chartPoints.forEach { point ->
                            val amount = point.amount ?: 0.0
                            val ratio = if (maxAmount > 0.0) {
                                (amount / maxAmount).toFloat().coerceIn(0.08f, 1.0f)
                            } else {
                                0.08f
                            }
                            val isCurrent = point.isCurrentDay == true
                            val dayNumber = point.dayLabel?.split(" ")?.firstOrNull()
                                ?: point.date?.split("-")?.lastOrNull()
                                ?: ""

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
                                        .background(if (isCurrent) primaryColor else accentColor)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = dayNumber,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No sales recorded this week",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$weekDateRangeTag · Daily sales in ₹",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }

        // 5. Your AI Priorities Section
        val aiPriorities = data.aiPriorities ?: emptyList()
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
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { onInsightsClick() }
                )
            }

            if (aiPriorities.isNotEmpty()) {
                aiPriorities.forEach { priority ->
                    AiPriorityCard(
                        priority = priority,
                        primaryColor = primaryColor,
                        onClick = {
                            if (priority.type.equals("reorder", ignoreCase = true)) {
                                onReorderMilkClick()
                            } else {
                                onFreeUpStockClick()
                            }
                        }
                    )
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onInsightsClick() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SparkleIcon(primaryColor = primaryColor)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "All systems optimal",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "AI will alert you when reordering or clearance is recommended",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 6. Footer (Live store data & real timestamp)
        val footerText = if (lastUpdatedText.isNotBlank()) {
            "Live store data · Updated $lastUpdatedText"
        } else {
            "Live store data"
        }

        Text(
            text = footerText,
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

/**
 * Reusable AI Priority Card displaying reorder/clearance recommendations.
 */
@Composable
private fun AiPriorityCard(
    priority: DashboardAiPriority,
    primaryColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
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
                SparkleIcon(primaryColor = primaryColor)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = priority.title ?: "AI Recommendation",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                    if (!priority.subtitle.isNullOrBlank()) {
                        Text(
                            text = priority.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            // Right Chevron
            Canvas(modifier = Modifier.size(12.dp)) {
                val path = Path().apply {
                    moveTo(size.width * 0.2f, size.height * 0.1f)
                    lineTo(size.width * 0.8f, size.height * 0.5f)
                    lineTo(size.width * 0.2f, size.height * 0.9f)
                }
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}

/**
 * AI Sparkle decorative icon.
 */
@Composable
private fun SparkleIcon(primaryColor: Color) {
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
}

@Preview(showBackground = true)
@Composable
fun DashboardHomeScreenPreview() {
    StockSenseTheme {
        DashboardHomeContent(
            data = DashboardSummaryData(),
            unreadCount = 2,
            lastUpdatedText = "30 Oct, 1:30 pm"
        )
    }
}
