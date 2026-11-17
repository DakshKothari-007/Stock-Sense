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
                                color = InkDark,
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

                BoldButton(
                    text = "Done",
                    onClick = onDoneClick,
                    isPrimary = true
                )
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
