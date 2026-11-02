package com.simats.stocksense.feature.dashboard.ui

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.stocksense.core.util.NumberFormatUtil
import com.simats.stocksense.feature.reorder.viewmodel.InsightsViewModel
import java.util.Locale

@Composable
fun InsightsContent(
    onReorderMilkClick: () -> Unit = {},
    onMoveBiscuitsClick: () -> Unit = {},
    onTeaPairClick: () -> Unit = {},
    viewModel: InsightsViewModel = viewModel()
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val uiState by viewModel.uiState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
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
                text = "Green Basket - ${NumberFormatUtil.formatCurrentHeaderDate()}",
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
                    val count = uiState.totalOpportunities
                    val oppText = if (count == 1) "1 opportunity for your shop" else "$count opportunities for your shop"
                    Text(
                        text = oppText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Based on stock levels and recent sales.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (uiState.isLoading && uiState.slowMovingData == null && uiState.productPairsData == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = primaryColor)
            }
        } else if (uiState.errorMessage != null && uiState.totalOpportunities == 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Failed to load insights.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { viewModel.refresh() },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Retry", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (!uiState.hasEnoughSlowMovingHistory && !uiState.hasEnoughPairsHistory && uiState.reorderItems.isEmpty()) {
            // Backend returns has_enough_sales_history == false
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Sales history needed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = uiState.slowMovingHistoryMessage ?: uiState.productPairsHistoryMessage
                            ?: "Need at least 30 days of sales history to calculate insights accurately.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    )
                }
            }
        } else if (uiState.totalOpportunities == 0) {
            // Empty state
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No insights yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Nothing slow-moving right now. Check back as new sales are recorded.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            // Recommendation Cards List
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Recommendation Card 1: Reorder Recommendations
                if (uiState.reorderItems.isNotEmpty()) {
                    val reorder = uiState.reorderItems.first()
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onReorderMilkClick() },
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
                                            text = "Reorder ${reorder.product?.name ?: "Stock"}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${(reorder.priority ?: "HIGH").uppercase(Locale.ENGLISH)} PRIORITY · REORDER",
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

                            val stockText = "${reorder.currentStock ?: 0} units left, ${reorder.dailySalesRate ?: 0.0} sold per day. Add ${reorder.suggestedQuantity ?: 0} units for ${NumberFormatUtil.formatIndianRupees(reorder.estimatedCost)} to avoid an empty shelf."
                            Text(
                                text = stockText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                // Recommendation Card 2: Slow-Moving Recommendations
                if (!uiState.hasEnoughSlowMovingHistory && uiState.slowMovingHistoryMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "SLOW-MOVING INVENTORY",
                                style = MaterialTheme.typography.labelSmall,
                                color = primaryColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = uiState.slowMovingHistoryMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    uiState.slowMovingItems.forEachIndexed { index, item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    viewModel.selectSlowMovingIndex(index)
                                    onMoveBiscuitsClick()
                                },
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
                                                text = "Move ${item.name ?: "stock"} faster",
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

                                val slowText = "${item.effectiveStock} packs left; only ${item.unitsSold30Days ?: 0} sold in 30 days. Try ${item.effectiveDiscountPct}% off or a clearance offer."
                                Text(
                                    text = slowText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }

                // Recommendation Card 3: Frequently Bought Together
                if (!uiState.hasEnoughPairsHistory && uiState.productPairsHistoryMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "FREQUENTLY BOUGHT TOGETHER",
                                style = MaterialTheme.typography.labelSmall,
                                color = primaryColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = uiState.productPairsHistoryMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    uiState.productPairItems.forEachIndexed { index, pair ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    viewModel.selectPairIndex(index)
                                    onTeaPairClick()
                                },
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
                                            val pairTitle = "${pair.product1?.name ?: "Product 1"} + ${pair.product2?.name ?: "Product 2"}"
                                            Text(
                                                text = pairTitle,
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

                                val supportPct = ((pair.effectiveSupport) * 100).toInt()
                                val bundlePrice = pair.suggestedAction?.bundlePrice ?: 0.0
                                val pairDetail = "${pair.product1?.name ?: "Product 1"} + ${pair.product2?.name ?: "Product 2"} appear together in $supportPct% of baskets. Offer the pair at ${NumberFormatUtil.formatIndianRupees(bundlePrice)}."
                                Text(
                                    text = pairDetail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
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
