package com.simats.stocksense.feature.reorder.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.stocksense.core.components.BoldButton
import com.simats.stocksense.core.components.BoldCard
import com.simats.stocksense.core.theme.*
import com.simats.stocksense.core.util.NumberFormatUtil
import com.simats.stocksense.core.util.ResponsiveContainer
import com.simats.stocksense.data.model.PurchaseOrderDto
import com.simats.stocksense.feature.reorder.viewmodel.ReorderViewModel

@Composable
fun ReorderPlacedScreen(
    onDoneClick: () -> Unit = {},
    onViewOrdersClick: () -> Unit = {},
    placedOrder: PurchaseOrderDto? = null,
    viewModel: ReorderViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val order = placedOrder ?: uiState.lastPlacedOrder

    val statusBadgeText = if (!order?.orderNo.isNullOrBlank()) {
        "ORDER ${order?.orderNo}"
    } else {
        "ORDER PLACED"
    }

    val item = order?.items?.firstOrNull()
    val detailsText = if (item != null) {
        val costText = NumberFormatUtil.formatIndianRupees(item.costPrice ?: 0.0)
        val totalText = NumberFormatUtil.formatIndianRupees(order?.totalEstimatedCost ?: 0.0)
        "${item.productName ?: "Product"} · ${order?.totalUnits ?: item.quantity} units × $costText = $totalText"
    } else if (order != null && (order.totalUnits ?: 0) > 0) {
        val totalText = NumberFormatUtil.formatIndianRupees(order.totalEstimatedCost ?: 0.0)
        "${order.totalUnits} units · Total estimated $totalText"
    } else {
        "Reorder request recorded with supplier."
    }

    val descText = if (!order?.orderNo.isNullOrBlank()) {
        "Purchase order ${order.orderNo} was recorded successfully. Status: ${order.status ?: "placed"}."
    } else {
        "A purchase order was created and recorded into your store inventory system."
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CreamBackground
    ) {
        ResponsiveContainer(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            maxWidth = 520.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BoldCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = CardWhite,
                    borderColor = InkDark,
                    borderWidth = 2.5.dp,
                    shadowOffset = 4.dp,
                    contentPadding = PaddingValues(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Checkmark Circle Icon Box
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SoftLavender)
                                .border(2.dp, InkDark, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(26.dp)) {
                                drawCircle(
                                    color = InkDark,
                                    radius = size.width / 2f,
                                    style = Stroke(width = 1.8.dp.toPx())
                                )
                                val path = Path().apply {
                                    moveTo(size.width * 0.25f, size.height * 0.5f)
                                    lineTo(size.width * 0.45f, size.height * 0.7f)
                                    lineTo(size.width * 0.75f, size.height * 0.35f)
                                }
                                drawPath(
                                    path = path,
                                    color = InkDark,
                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }

                        // Title
                        Text(
                            text = "Reorder recorded",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center
                        )

                        // Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PaleBlue,
                            border = BorderStroke(1.5.dp, InkDark)
                        ) {
                            Text(
                                text = statusBadgeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MainBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        // Details
                        Text(
                            text = detailsText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = InkDark,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )

                        Text(
                            text = descText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Buttons: Back to Home & View orders
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                BoldButton(
                                    text = "Back to Home",
                                    onClick = onDoneClick,
                                    isPrimary = false
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                BoldButton(
                                    text = "View orders",
                                    onClick = onViewOrdersClick,
                                    isPrimary = true
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
fun ReorderPlacedScreenPreview() {
    StockSenseTheme {
        ReorderPlacedScreen()
    }
}
