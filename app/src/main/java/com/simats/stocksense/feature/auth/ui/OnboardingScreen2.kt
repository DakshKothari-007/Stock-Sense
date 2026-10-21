package com.simats.stocksense.feature.auth.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.simats.stocksense.core.theme.*

@Composable
fun OnboardingScreen2(
    onNextClick: () -> Unit = {},
    onSkipClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "StockSense",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "02 / INTELLIGENCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.5.sp,
                        fontSize = 10.sp
                    )
                }
            }

            // 2. Illustration Card Container
            val primaryColor = MaterialTheme.colorScheme.primary
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "YOUR AI STOCK PARTNER",
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontSize = 10.sp
                    )

                    // Reorder AI Suggestion Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                    // Sparkle / AI Star Icon
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.size(20.dp)) {
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
                                                style = Stroke(
                                                    width = 2.dp.toPx(),
                                                    cap = StrokeCap.Round,
                                                    join = StrokeJoin.Round
                                                )
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Reorder before you run out",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Amul Milk · 500 ml",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = primaryColor,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                // Chevron Right >
                                Canvas(modifier = Modifier.size(14.dp)) {
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

                            Text(
                                text = "Only 8 units left. Add 60 units to cover the next five days.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // Bottom Checkmark feature row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    ) {
                        Canvas(modifier = Modifier.size(18.dp)) {
                            val path = Path().apply {
                                moveTo(size.width * 0.1f, size.height * 0.55f)
                                lineTo(size.width * 0.35f, size.height * 0.8f)
                                lineTo(size.width * 0.75f, size.height * 0.25f)
                            }
                            drawPath(
                                path = path,
                                color = primaryColor,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Grounded in your actual sales patterns",
                            style = MaterialTheme.typography.bodySmall,
                            color = primaryColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // 3. Text Content & Bottom Actions
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Less guessing. Smarter decisions.",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Turn daily sales into timely reorders, smarter bundles and fewer products sitting idle.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Page Indicator Dots (3 dots, 2nd active)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(primaryColor)
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Next Button
                Button(
                    onClick = onNextClick,
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
                        text = "Next",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Skip Text Button
                TextButton(onClick = onSkipClick) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.bodyMedium,
                        color = primaryColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreen2Preview() {
    StockSenseTheme {
        OnboardingScreen2()
    }
}
