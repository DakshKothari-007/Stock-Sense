package com.simats.stocksense.feature.inventory.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.simats.stocksense.feature.inventory.icon.ProductAvatar
import com.simats.stocksense.feature.inventory.viewmodel.ProductDetailUiState
import com.simats.stocksense.feature.inventory.viewmodel.ProductDetailViewModel
import java.util.Locale

@Composable
fun ProductDetailScreen(
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onReorderClick: () -> Unit = {},
    onSalesAnalyticsClick: () -> Unit = {},
    viewModel: ProductDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Auto-refresh when returning to screen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadProduct()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val context = LocalContext.current

    ProductDetailScreenContent(
        uiState = uiState,
        onRetryClick = { viewModel.loadProduct() },
        onBackClick = onBackClick,
        onEditClick = onEditClick,
        onReorderClick = {
            viewModel.createReorder(
                onSuccess = { onReorderClick() },
                onError = { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            )
        },
        onSalesAnalyticsClick = onSalesAnalyticsClick
    )
}

@Composable
fun ProductDetailScreenContent(
    uiState: ProductDetailUiState,
    onRetryClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onReorderClick: () -> Unit = {},
    onSalesAnalyticsClick: () -> Unit = {}
) {
    val primaryColor = MainYellow
    val accentColor = SoftLightBlue

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CreamBackground
    ) {
        when {
            uiState.isLoading -> {
                // Loading Spinner
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
                            text = "Loading product details...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedInk
                        )
                    }
                }
            }

            uiState.errorMessage != null || uiState.product.name.isBlank() || uiState.product.name == "—" -> {
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
                                text = "Unable to load product details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkDark
                            )
                            Text(
                                text = uiState.errorMessage ?: "Product details could not be loaded. Please check your connection and retry.",
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
            val windowInfo = rememberWindowSizeInfo()

            ResponsiveContainer(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding(),
                maxWidth = 600.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = windowInfo.horizontalPadding, vertical = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Header with Back Button, Title in middle, Edit small icon button on right
                        BoldTopBar(
                            title = "Product details",
                            subtitle = "${uiState.product.sku} · ${uiState.categoryName}",
                            onBackClick = onBackClick,
                            actions = {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = onEditClick
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .offset(x = 2.dp, y = 2.dp)
                                            .background(ShadowColor, RoundedCornerShape(12.dp))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(CardWhite, RoundedCornerShape(12.dp))
                                            .border(2.dp, InkDark, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "✎",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = InkDark
                                        )
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // 2. Product Summary Card
                        BoldCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = CardWhite,
                            borderColor = InkDark,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProductAvatar(
                                    name = uiState.product.name,
                                    category = uiState.product.category,
                                    iconKey = uiState.product.iconKey,
                                    size = 56.dp
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = uiState.product.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = InkDark,
                                        fontSize = 20.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = uiState.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedInk,
                                        fontSize = 13.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (uiState.product.sku.isNotBlank() && uiState.product.sku != "—") {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "SKU: ${uiState.product.sku}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedInk,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val statusText = uiState.product.status?.takeIf { it.isNotBlank() } ?: "In stock"
                                    BoldStatusChip(status = statusText)
                                }
                            }
                        }

                        // 3. Metric Cards (Available Stock & Selling Price)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Available Stock Card
                            BoldCard(
                                modifier = Modifier.weight(1f),
                                backgroundColor = SoftLightBlue,
                                borderColor = InkDark,
                                borderWidth = 2.dp,
                                shadowOffset = 3.dp,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Available stock",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnPastelInk,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${uiState.product.units} units",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = OnPastelInk,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Minimum: ${uiState.minimumStockThreshold} units",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnPastelMuted,
                                        fontSize = 11.sp,
                                        maxLines = 2,
                                        lineHeight = 15.sp,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Selling Price Card
                            BoldCard(
                                modifier = Modifier.weight(1f),
                                backgroundColor = SoftPeach,
                                borderColor = InkDark,
                                borderWidth = 2.dp,
                                shadowOffset = 3.dp,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Selling price",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnPastelInk,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = uiState.product.price,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = OnPastelInk,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val costFormatted = uiState.costPrice.replace(".00", "")
                                    Text(
                                        text = "Cost $costFormatted",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnPastelMuted,
                                        fontSize = 11.sp,
                                        maxLines = 2,
                                        lineHeight = 15.sp,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // 4. Sales History Card
                        BoldCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = CardWhite,
                            borderColor = InkDark,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            shape = RoundedCornerShape(16.dp),
                            onClick = { onSalesAnalyticsClick() }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Sales history",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark,
                                    fontSize = 16.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Past 7 days",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedInk,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "${uiState.product.units} units in stock",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = InkDark,
                                        fontSize = 12.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                val chartPoints = if (uiState.salesHistory.size > 7) {
                                    uiState.salesHistory.takeLast(7)
                                } else {
                                    uiState.salesHistory
                                }

                                val hasSalesData = uiState.has7DaySales && chartPoints.isNotEmpty() && chartPoints.any { it.unitsSold > 0 || it.revenue > 0.0 }

                                if (!hasSalesData) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No sales yet",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MutedInk,
                                            fontSize = 13.sp
                                        )
                                    }
                                } else {
                                    val unitPrice = uiState.product.rawPrice.takeIf { it > 0.0 } ?: 1.0
                                    val maxDailyRevenue = chartPoints.maxOfOrNull {
                                        if (it.revenue > 0.0) it.revenue else (it.unitsSold * unitPrice)
                                    } ?: 0.0

                                    val ceilingY = calculateChartYCeiling(maxDailyRevenue)
                                    val topYLabel = NumberFormatUtil.formatCompactRupees(ceilingY)
                                    val midYLabel = NumberFormatUtil.formatCompactRupees(ceilingY / 2.0)
                                    val zeroYLabel = "₹0"

                                    val formattedLabels = remember(chartPoints) {
                                        chartPoints.map { point ->
                                            formatChartDayLabel(point.dayLabel, point.date)
                                        }
                                    }

                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        // Y-Axis + Bars
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(96.dp)
                                        ) {
                                            // Y-Axis Labels Column
                                            Column(
                                                modifier = Modifier
                                                    .width(42.dp)
                                                    .fillMaxHeight(),
                                                verticalArrangement = Arrangement.SpaceBetween,
                                                horizontalAlignment = Alignment.End
                                            ) {
                                                Text(
                                                    text = topYLabel,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MutedInk,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    textAlign = TextAlign.End
                                                )
                                                Text(
                                                    text = midYLabel,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MutedInk,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    textAlign = TextAlign.End
                                                )
                                                Text(
                                                    text = zeroYLabel,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MutedInk,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    textAlign = TextAlign.End
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            // Bars with horizontal gridlines
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                            ) {
                                                val gridColor = InkDark.copy(alpha = 0.15f)
                                                Canvas(modifier = Modifier.fillMaxSize()) {
                                                    val strokeW = 1.dp.toPx()
                                                    // Top line
                                                    drawLine(
                                                        color = gridColor,
                                                        start = Offset(0f, 6.dp.toPx()),
                                                        end = Offset(size.width, 6.dp.toPx()),
                                                        strokeWidth = strokeW
                                                    )
                                                    // Mid line
                                                    drawLine(
                                                        color = gridColor,
                                                        start = Offset(0f, size.height / 2f),
                                                        end = Offset(size.width, size.height / 2f),
                                                        strokeWidth = strokeW
                                                    )
                                                    // Baseline
                                                    drawLine(
                                                        color = InkDark,
                                                        start = Offset(0f, size.height - 1.dp.toPx()),
                                                        end = Offset(size.width, size.height - 1.dp.toPx()),
                                                        strokeWidth = 1.5.dp.toPx()
                                                    )
                                                }

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(bottom = 2.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.Bottom
                                                ) {
                                                    chartPoints.forEachIndexed { index, point ->
                                                        val isLast = point.isToday || index == chartPoints.lastIndex
                                                        val dayRev = if (point.revenue > 0.0) point.revenue else (point.unitsSold * unitPrice)
                                                        val ratio = if (ceilingY > 0.0) {
                                                            (dayRev / ceilingY).toFloat().coerceIn(if (dayRev > 0.0) 0.08f else 0.02f, 1.0f)
                                                        } else {
                                                            0.02f
                                                        }

                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .fillMaxHeight(),
                                                            contentAlignment = Alignment.BottomCenter
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxWidth(0.48f)
                                                                    .widthIn(min = 6.dp, max = 18.dp)
                                                                    .fillMaxHeight(ratio)
                                                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                                    .background(if (isLast) MainBlue else SoftLightBlue)
                                                                    .border(1.dp, InkDark, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // X-Axis Labels Row (Aligned with bars)
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Spacer(modifier = Modifier.width(50.dp)) // 42.dp y-axis + 8.dp spacer

                                            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                                                val availableWidth = maxWidth
                                                val pointCount = chartPoints.size
                                                val columnWidth = if (pointCount > 0) availableWidth / pointCount else availableWidth

                                                // Dynamic overlap prevention: 320dp screens and large font scales
                                                val fontScale = LocalDensity.current.fontScale
                                                val hasLongLabels = formattedLabels.any { it.length > 3 }
                                                val isNarrow = columnWidth < 40.dp
                                                val isVeryNarrow = columnWidth < 28.dp || fontScale >= 1.3f
                                                val shouldSkipLabels = (columnWidth < 45.dp && hasLongLabels) || isNarrow || fontScale > 1.05f || pointCount > 7

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    chartPoints.forEachIndexed { index, point ->
                                                        val rawAbbr = formattedLabels.getOrElse(index) { "" }
                                                        val displayLabel = if (isVeryNarrow) {
                                                            if (index == 0 || index == pointCount / 2 || index == pointCount - 1) rawAbbr else ""
                                                        } else if (shouldSkipLabels) {
                                                            val step = if (pointCount > 7) ((pointCount + 5) / 6).coerceAtLeast(2) else 2
                                                            if (index % step == 0 || index == pointCount - 1) rawAbbr else ""
                                                        } else {
                                                            rawAbbr
                                                        }

                                                        Box(
                                                            modifier = Modifier.weight(1f),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = displayLabel,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MutedInk,
                                                                fontSize = 10.sp,
                                                                maxLines = 1,
                                                                softWrap = false,
                                                                textAlign = TextAlign.Center
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Daily sales · Tap for detailed analytics",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedInk,
                                    fontSize = 10.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                HorizontalDivider(color = InkDark.copy(alpha = 0.15f), thickness = 1.dp)

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = uiState.stockCoverLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedInk,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = uiState.stockCoverDaysText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = InkDark,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // 5. Suggested Reorder Box
                        if (uiState.unitsToReorder <= 0 && uiState.onOrderUnits <= 0) {
                            BoldCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = CardWhite,
                                borderColor = InkDark,
                                borderWidth = 2.dp,
                                shadowOffset = 3.dp,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(SoftLavender)
                                            .border(2.dp, InkDark, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = InkDark,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "No reorder needed right now",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = InkDark,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Stock is sufficient based on current sales pace",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedInk,
                                            fontSize = 12.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        } else {
                            BoldCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = SoftLavender,
                                borderColor = InkDark,
                                borderWidth = 2.dp,
                                shadowOffset = 3.dp,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(CardWhite)
                                                .border(2.dp, InkDark, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Canvas(modifier = Modifier.size(16.dp)) {
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
                                                    color = MainBlue,
                                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (uiState.onOrderUnits > 0 && uiState.unitsToReorder <= 0) {
                                                    "${uiState.onOrderUnits} units already on order"
                                                } else {
                                                    "Suggested reorder: ${uiState.unitsToReorder} units"
                                                },
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = OnPastelInk,
                                                fontSize = 14.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = if (uiState.onOrderUnits > 0 && uiState.unitsToReorder <= 0) {
                                                    "Awaiting delivery from placed purchase order"
                                                } else {
                                                    "Estimated purchase: ${uiState.estimatedCost}"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = OnPastelMuted,
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    val currentStockUnits = uiState.product.rawUnits
                                    if (uiState.onOrderUnits > 0 && uiState.unitsToReorder <= 0) {
                                        val totalWithOnOrder = currentStockUnits + uiState.onOrderUnits
                                        val coverDesc = if (uiState.stockCoverDaysText == "—") {
                                            "Your $currentStockUnits units are in stock. ${uiState.onOrderUnits} units on order will bring stock to $totalWithOnOrder units."
                                        } else {
                                            "Your $currentStockUnits units cover ${uiState.stockCoverDaysText}. ${uiState.onOrderUnits} units on order will bring stock to $totalWithOnOrder units."
                                        }
                                        Text(
                                            text = coverDesc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = InkDark,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    } else {
                                        val targetStock = currentStockUnits + uiState.unitsToReorder
                                        val coverDesc = if (uiState.stockCoverDaysText == "—") {
                                            "Your $currentStockUnits units are in stock. Adding ${uiState.unitsToReorder} brings stock to $targetStock units."
                                        } else {
                                            "Your $currentStockUnits units cover ${uiState.stockCoverDaysText}. Adding ${uiState.unitsToReorder} brings stock to $targetStock units."
                                        }
                                        Text(
                                            text = coverDesc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = InkDark,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }

                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 7. Bottom Reorder Button
                    if (uiState.onOrderUnits > 0 && uiState.unitsToReorder <= 0) {
                        BoldCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = CardWhite,
                            borderColor = InkDark,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${uiState.onOrderUnits} units already on order",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        BoldButton(
                            text = if (uiState.unitsToReorder > 0) {
                                "Reorder ${uiState.unitsToReorder} units · ${uiState.estimatedCost}"
                            } else {
                                "Stock is sufficient"
                            },
                            onClick = onReorderClick,
                            enabled = !uiState.isOrdering && uiState.unitsToReorder > 0,
                            modifier = Modifier.fillMaxWidth(),
                            isPrimary = true
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 8. Footer Date (Formatted as "Mon, 9 Nov 2026", device timezone / IST, never hardcoded)
                    val footerDate = uiState.lastUpdatedDateText.ifBlank {
                        NumberFormatUtil.formatProductDetailLastUpdated(null)
                    }
                    Text(
                        text = "Last updated · $footerDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    )
                }
            }
        }
    }
}
}

/**
 * Calculates a clean, rounded ceiling value for chart y-axis (e.g. 50, 100, 500, 1000, 2000, 5000).
 */
private fun calculateChartYCeiling(maxVal: Double): Double {
    if (maxVal <= 0.0) return 100.0
    return when {
        maxVal <= 20.0 -> 20.0
        maxVal <= 50.0 -> 50.0
        maxVal <= 100.0 -> 100.0
        maxVal <= 200.0 -> 200.0
        maxVal <= 500.0 -> 500.0
        maxVal <= 1000.0 -> 1000.0
        maxVal <= 2000.0 -> 2000.0
        maxVal <= 3000.0 -> 3000.0
        maxVal <= 5000.0 -> 5000.0
        maxVal <= 10000.0 -> 10000.0
        maxVal <= 20000.0 -> 20000.0
        maxVal <= 50000.0 -> 50000.0
        else -> {
            val magnitude = Math.pow(10.0, Math.floor(Math.log10(maxVal)))
            val normalized = maxVal / magnitude
            val roundedNormalized = when {
                normalized <= 1.0 -> 1.0
                normalized <= 2.0 -> 2.0
                normalized <= 5.0 -> 5.0
                else -> 10.0
            }
            roundedNormalized * magnitude
        }
    }
}

/**
 * Abbreviates chart labels from backend data dates into short format (e.g. "5 Nov").
 */
private fun formatChartDayLabel(rawLabel: String?, rawDate: String?): String {
    val text = rawLabel?.trim().orEmpty()
    if (text.isNotEmpty()) {
        val monthMap = mapOf(
            "january" to "Jan", "february" to "Feb", "march" to "Mar", "april" to "Apr",
            "may" to "May", "june" to "Jun", "july" to "Jul", "august" to "Aug",
            "september" to "Sep", "october" to "Oct", "november" to "Nov", "december" to "Dec",
            "jan" to "Jan", "feb" to "Feb", "mar" to "Mar", "apr" to "Apr",
            "jun" to "Jun", "jul" to "Jul", "aug" to "Aug", "sep" to "Sep",
            "oct" to "Oct", "nov" to "Nov", "dec" to "Dec"
        )

        val parts = text.split("\\s+".toRegex())
        if (parts.size >= 2) {
            val p0Num = parts[0].toIntOrNull()
            val p1Num = parts[1].toIntOrNull()
            val m0 = monthMap[parts[0].lowercase(Locale.ENGLISH)]
            val m1 = monthMap[parts[1].lowercase(Locale.ENGLISH)]

            if (p0Num != null && m1 != null) {
                return "$p0Num $m1"
            } else if (p1Num != null && m0 != null) {
                return "$p1Num $m0"
            }
        }
        if (text.matches(Regex("\\d+\\s+[A-Za-z]+"))) {
            return text
        }
    }

    if (!rawDate.isNullOrBlank()) {
        try {
            val parts = rawDate.trim().split("-")
            if (parts.size == 3) {
                val day = parts[2].toIntOrNull()
                val monthIdx = parts[1].toIntOrNull()
                val months = arrayOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                if (day != null && monthIdx != null && monthIdx in 1..12) {
                    return "$day ${months[monthIdx]}"
                }
            }
        } catch (_: Exception) {}
    }

    return text.take(6)
}

@Preview(showBackground = true)
@Composable
fun ProductDetailScreenPreview() {
    StockSenseTheme {
        ProductDetailScreenContent(
            uiState = ProductDetailUiState(
                lastUpdatedDateText = "Mon, 9 Nov 2026"
            )
        )
    }
}
