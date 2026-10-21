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
import androidx.compose.runtime.Composable
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
