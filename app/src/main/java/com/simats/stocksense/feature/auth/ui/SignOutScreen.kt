package com.simats.stocksense.feature.auth.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.stocksense.core.components.*
import com.simats.stocksense.core.theme.*

@Composable
fun SignOutScreen(
    onBackToLoginClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CreamBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // 1. Top Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = "StockSense",
                    style = MaterialTheme.typography.titleLarge,
                    color = InkDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "SMARTER STOCK. BETTER BUSINESS.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedInk,
                    letterSpacing = 1.2.sp,
                    fontSize = 10.sp
                )
            }

            // 2. Centered Card
            BoldCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = CardWhite,
                borderColor = InkDark,
                borderWidth = 2.5.dp,
                shadowOffset = 4.dp,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Checkmark Circle Icon Box
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(SoftLightBlue)
                            .border(2.dp, InkDark, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(26.dp)) {
                            drawCircle(
                                color = OnPastelIcon,
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
                                color = OnPastelIcon,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }

                    // Headline
                    Text(
                        text = "Signed out successfully",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = InkDark,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )

                    // Subtitle
                    Text(
                        text = "You're signed out of StockSense. Log in again whenever you're ready.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Back to Login Button
                    BoldButton(
                        onClick = onBackToLoginClick,
                        modifier = Modifier.fillMaxWidth(),
                        isPrimary = true
                    ) {
                        Text(
                            text = "Back to Login",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // 3. Footer Note
            Text(
                text = "StockSense v1.0 · Smarter Stock. Better Business.",
                style = MaterialTheme.typography.bodySmall,
                color = MutedInk,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignOutScreenPreview() {
    StockSenseTheme {
        SignOutScreen()
    }
}
