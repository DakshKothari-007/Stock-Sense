package com.simats.stocksense.feature.reorder.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.simats.stocksense.core.components.BoldButton
import com.simats.stocksense.core.theme.*
import com.simats.stocksense.core.util.ResponsiveContainer

@Composable
fun AcceptedOverlayScreen(
    suggestedPrice: Double? = null,
    onApplySuggestedPriceClick: (() -> Unit)? = null,
    isBundleOffer: Boolean = false,
    onViewOffersClick: (() -> Unit)? = null,
    onDoneClick: () -> Unit = {}
) {
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
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(SoftLavender)
                            .border(2.5.dp, InkDark, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(40.dp)) {
                            val path = Path().apply {
                                moveTo(size.width * 0.2f, size.height * 0.52f)
                                lineTo(size.width * 0.42f, size.height * 0.74f)
                                lineTo(size.width * 0.8f, size.height * 0.26f)
                            }
                            drawPath(
                                path = path,
                                color = OnPastelIcon,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }

                    Text(
                        text = "Recommendation Accepted",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = InkDark,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Reorder decision has been saved into your AI insights record.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedInk,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }

                val canApplySuggestedPrice = onApplySuggestedPriceClick != null &&
                    suggestedPrice != null &&
                    suggestedPrice > 0.0
                val canViewOffers = isBundleOffer && onViewOffersClick != null

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (canApplySuggestedPrice) {
                        BoldButton(
                            text = "Apply suggested price",
                            onClick = { onApplySuggestedPriceClick?.invoke() },
                            modifier = Modifier.fillMaxWidth(),
                            isPrimary = true
                        )
                    } else if (canViewOffers) {
                        BoldButton(
                            text = "View offers",
                            onClick = { onViewOffersClick?.invoke() },
                            modifier = Modifier.fillMaxWidth(),
                            isPrimary = true
                        )
                    }

                    BoldButton(
                        text = "Done",
                        onClick = onDoneClick,
                        modifier = Modifier.fillMaxWidth(),
                        isPrimary = !canApplySuggestedPrice && !canViewOffers
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AcceptedOverlayScreenPreview() {
    StockSenseTheme {
        AcceptedOverlayScreen()
    }
}
