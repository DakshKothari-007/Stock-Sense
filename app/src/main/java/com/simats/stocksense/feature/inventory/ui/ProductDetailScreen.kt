package com.simats.stocksense.feature.inventory.ui

import android.widget.Toast
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
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
import com.simats.stocksense.feature.inventory.viewmodel.ProductDetailUiState
import com.simats.stocksense.feature.inventory.viewmodel.ProductDetailViewModel

@Composable
fun ProductDetailScreen(
    onBackClick: () -> Unit = {},
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
    onReorderClick: () -> Unit = {},
    onSalesAnalyticsClick: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val accentColor = Teal500

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
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
                            color = primaryColor,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "Loading product details...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Unable to load product details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = uiState.errorMessage ?: "Product details could not be loaded. Please check your connection and retry.",
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Header with Back Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                Canvas(modifier = Modifier.size(16.dp)) {
                                    val path = Path().apply {
                                        moveTo(size.width * 0.7f, size.height * 0.2f)
                                        lineTo(size.width * 0.3f, size.height * 0.5f)
                                        lineTo(size.width * 0.7f, size.height * 0.8f)
                                    }
                                    drawPath(
                                        path = path,
                                        color = primaryColor,
                                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "Product details",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "${uiState.product.sku} · ${uiState.categoryName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // 2. Product Summary Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.size(26.dp)) {
                                        when (uiState.product.iconType) {
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

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = uiState.product.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 20.sp
                                    )
                                    Text(
                                        text = uiState.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Amber50
                                    ) {
                                        Text(
                                            text = uiState.product.status ?: "In stock",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Amber700,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Metric Cards (Available Stock & Selling Price)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Available Stock Card
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Available stock",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${uiState.product.units} units",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Minimum: ${uiState.minimumStockThreshold} units",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Selling Price Card
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Selling price",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = uiState.product.price,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Purchase cost: ${uiState.costPrice}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // 4. Sales History Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onSalesAnalyticsClick() },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp)
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
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Past 7 days",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "${uiState.product.units} units in stock",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                if (!uiState.has7DaySales || uiState.salesHistory.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(85.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No sales in the last 7 days",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 13.sp
                                        )
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(85.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        uiState.salesHistory.forEachIndexed { index, point ->
                                            val isLast = point.isToday || index == uiState.salesHistory.lastIndex
                                            val ratio = point.heightRatio.coerceIn(0.18f, 1.0f)
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
                                                    text = point.dayNumber.ifEmpty { (index + 1).toString() },
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Daily units sold · Tap for detailed analytics",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.8.dp)

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Stock cover pace",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = uiState.stockCoverDaysText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // 5. Suggested Reorder Box
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp)),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(14.dp)
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
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface),
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
                                                color = primaryColor,
                                                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = if (uiState.onOrderUnits > 0 && uiState.unitsToReorder <= 0) {
                                                "${uiState.onOrderUnits} units already on order"
                                            } else {
                                                "Suggested reorder: ${uiState.unitsToReorder} units"
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = if (uiState.onOrderUnits > 0 && uiState.unitsToReorder <= 0) {
                                                "Awaiting delivery from placed purchase order"
                                            } else {
                                                "Estimated purchase: ${uiState.estimatedCost}"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                val currentStockUnits = uiState.product.rawUnits
                                if (uiState.onOrderUnits > 0 && uiState.unitsToReorder <= 0) {
                                    val totalWithOnOrder = currentStockUnits + uiState.onOrderUnits
                                    Text(
                                        text = "Your $currentStockUnits units cover ${uiState.stockCoverDaysText}. ${uiState.onOrderUnits} units on order will bring stock to $totalWithOnOrder units.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                } else {
                                    val targetStock = currentStockUnits + uiState.unitsToReorder
                                    Text(
                                        text = "Your $currentStockUnits units cover ${uiState.stockCoverDaysText}. Adding ${uiState.unitsToReorder} brings stock to $targetStock units.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 7. Bottom Reorder Button
                    if (uiState.onOrderUnits > 0 && uiState.unitsToReorder <= 0) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${uiState.onOrderUnits} units already on order",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = onReorderClick,
                            enabled = !uiState.isOrdering && uiState.unitsToReorder > 0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            if (uiState.isOrdering) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (uiState.unitsToReorder > 0) {
                                        "Reorder ${uiState.unitsToReorder} units · ${uiState.estimatedCost}"
                                    } else {
                                        "Stock is sufficient"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductDetailScreenPreview() {
    StockSenseTheme {
        ProductDetailScreenContent(
            uiState = ProductDetailUiState()
        )
    }
}
