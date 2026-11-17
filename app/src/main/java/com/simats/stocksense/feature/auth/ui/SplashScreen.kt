package com.simats.stocksense.feature.auth.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.stocksense.R
import com.simats.stocksense.core.components.BoldButton
import com.simats.stocksense.core.theme.*
import com.simats.stocksense.core.util.ResponsiveContainer
import com.simats.stocksense.core.util.rememberWindowSizeInfo

private fun seg(t: Float, a: Float, b: Float): Float {
    if (b <= a) return if (t >= a) 1f else 0f
    val fraction = ((t - a) / (b - a)).coerceIn(0f, 1f)
    return EaseOut.transform(fraction)
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}

private fun Modifier.reveal(t: Float, a: Float, b: Float, dy: Float): Modifier = this.graphicsLayer {
    val p = seg(t, a, b)
    alpha = p
    translationY = (1f - p) * dy * density
}

@Composable
fun AnimatedSplashLogo(
    modifier: Modifier = Modifier,
    t: Float = 1f,
) {
    val scale = if (t < 0.2f) {
        lerp(0.85f, 1.05f, seg(t, 0f, 0.2f))
    } else {
        lerp(1.05f, 1f, seg(t, 0.2f, 0.35f))
    }
    val alpha = seg(t, 0f, 0.15f)

    Box(
        modifier = modifier
            .size(100.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.stocksense_symbol_inkblue),
            contentDescription = "StockSense Logo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun SplashScreen(
    onGetStartedClick: () -> Unit = {}
) {
    val animatable = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2800, easing = LinearEasing)
        )
    }
    val t = animatable.value

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CreamBackground
    ) {
        val windowInfo = rememberWindowSizeInfo()

        ResponsiveContainer(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            maxWidth = 480.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = windowInfo.horizontalPadding, vertical = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header
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
                            color = InkDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "RETAIL INTELLIGENCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedInk,
                            letterSpacing = 1.5.sp,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Center Content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    // Logo
                    AnimatedSplashLogo(t = t)

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "StockSense",
                        style = MaterialTheme.typography.headlineMedium,
                        color = InkDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        modifier = Modifier.reveal(t, 0.31f, 0.51f, 10f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Smarter Stock. Better Business.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = InkDark,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.reveal(t, 0.40f, 0.59f, 7f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Your everyday inventory, powered by AI. Built for shops that keep India moving.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedInk,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .reveal(t, 0.47f, 0.65f, 6f)
                    )
                }

                // Bottom Section (Badge + Button + Footer)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(2.dp, InkDark, RoundedCornerShape(14.dp))
                            .reveal(t, 0.55f, 0.725f, 5f),
                        color = SoftLightBlue,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Canvas(modifier = Modifier.size(20.dp)) {
                                val path = Path().apply {
                                    moveTo(size.width * 0.2f, size.height * 0.52f)
                                    lineTo(size.width * 0.42f, size.height * 0.74f)
                                    lineTo(size.width * 0.8f, size.height * 0.26f)
                                }
                                drawPath(
                                    path = path,
                                    color = InkDark,
                                    style = Stroke(
                                        width = 3.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "Clarity for every shelf. Confidence for every day.",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    BoldButton(
                        text = "Get Started",
                        onClick = onGetStartedClick,
                        isPrimary = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .reveal(t, 0.59f, 0.775f, 6f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Designed for supermarkets & neighbourhood stores",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    StockSenseTheme {
        SplashScreen()
    }
}
