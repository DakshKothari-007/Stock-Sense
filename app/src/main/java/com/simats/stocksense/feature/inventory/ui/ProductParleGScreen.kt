package com.simats.stocksense.feature.inventory.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.stocksense.core.components.*
import com.simats.stocksense.core.theme.*

@Composable
fun ProductParleGScreen(
    onBackClick: () -> Unit = {},
    onReorderClick: () -> Unit = {},
    onSalesAnalyticsClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CreamBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Header Row
            // 1. Shared BoldTopBar
            BoldTopBar(
                title = "Product details",
                subtitle = "SNK-002 · Snacks",
                onBackClick = onBackClick
            )

            // 2. Product Identity Card
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
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SoftLightBlue)
                            .border(2.dp, InkDark, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(26.dp)) {
                            drawCircle(
                                color = InkDark,
                                radius = size.width * 0.45f,
                                center = Offset(size.width / 2f, size.height / 2f),
                                style = Stroke(width = 2.dp.toPx())
                            )
                            drawCircle(color = InkDark, radius = size.width * 0.08f, center = Offset(size.width * 0.35f, size.height * 0.35f))
                            drawCircle(color = InkDark, radius = size.width * 0.08f, center = Offset(size.width * 0.65f, size.height * 0.45f))
                            drawCircle(color = InkDark, radius = size.width * 0.08f, center = Offset(size.width * 0.45f, size.height * 0.65f))
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Parle-G · 80 g",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "80 g · Glucose biscuits",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        BoldStatusChip(status = "Fast moving · Daily essential")
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
                            color = InkDark,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "120 units",
                            style = MaterialTheme.typography.titleLarge,
                            color = InkDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Minimum: 30 units",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            fontSize = 11.sp
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
                            color = InkDark,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹10",
                            style = MaterialTheme.typography.titleLarge,
                            color = InkDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Purchase cost: ₹8",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // 4. Sales History Card
            BoldCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sales history",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Analytics ›",
                            style = MaterialTheme.typography.bodySmall,
                            color = MainBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "9–15 Oct · 7 days",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "110 units · ₹1,100",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bar Chart Visualization
                    val barData = listOf(
                        "9" to 0.70f,
                        "10" to 0.85f,
                        "11" to 0.65f,
                        "12" to 0.90f,
                        "13" to 0.95f,
                        "14" to 0.75f,
                        "15" to 0.80f
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
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(if (isLast) MainBlue else SoftLightBlue)
                                        .border(1.dp, InkDark, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedInk,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "9–15 Oct 2026 · Units sold per day",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedInk,
                        fontSize = 10.sp
                    )
                }
            }

            // 5. Suggested Reorder Box
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

                        Column {
                            Text(
                                text = "Suggested reorder: 100 units",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkDark,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Estimated purchase: ₹800",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text(
                        text = "Parle-G sells steadily at ~15-18 packs/day. 100 units reorder keeps inventory healthy for 7+ days.",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkDark,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 6. Action Button
            BoldButton(
                text = "Reorder 100 units · ₹800",
                onClick = onReorderClick,
                modifier = Modifier.fillMaxWidth(),
                isPrimary = true
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductParleGScreenPreview() {
    StockSenseTheme {
        ProductParleGScreen()
    }
}