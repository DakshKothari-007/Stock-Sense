package com.simats.stocksense.feature.auth.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.stocksense.core.theme.StockSenseTheme

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
    t: Float,
    modifier: Modifier = Modifier
) {
    val upperArcPath = remember {
        PathParser().parsePathString("M14 48C34 16 62 16 82 48H70C55 31 41 31 26 48H14Z").toPath()
    }
    val lowerArcPath = remember {
        PathParser().parsePathString("M14 48C34 80 62 80 82 48H70C55 65 41 65 26 48H14Z").toPath()
    }

    Canvas(
        modifier = modifier
            .size(96.dp)
            .clip(RoundedCornerShape(32.dp))
    ) {
        val tileAlpha = seg(t, 0f, 0.06f)
        val tileScale = if (t < 0.18f) {
            lerp(0.86f, 1.025f, seg(t, 0f, 0.18f))
        } else {
            lerp(1.025f, 1f, seg(t, 0.18f, 0.275f))
        }

        val upperArcAlpha = seg(t, 0.08f, 0.16f)
        val upperArcX = if (t < 0.28f) {
            lerp(-26f, 1.5f, seg(t, 0.08f, 0.28f))
        } else {
            lerp(1.5f, 0f, seg(t, 0.28f, 0.36f))
        }

        val lowerArcAlpha = seg(t, 0.14f, 0.22f)
        val lowerArcX = if (t < 0.34f) {
            lerp(26f, -1.5f, seg(t, 0.14f, 0.34f))
        } else {
            lerp(-1.5f, 0f, seg(t, 0.34f, 0.42f))
        }

        val barsAlpha = seg(t, 0.275f, 0.425f)

        scale(size.width / 96f, pivot = Offset.Zero) {
            scale(tileScale, pivot = Offset(48f, 48f)) {
                // Tile: drawRect Color(0xFF146C43)
                drawRect(
                    color = Color(0xFF146C43),
                    size = Size(96f, 96f),
                    alpha = tileAlpha
                )

                // Upper arc
                translate(left = upperArcX, top = 0f) {
                    drawPath(
                        path = upperArcPath,
                        color = Color.White,
                        alpha = upperArcAlpha
                    )
                }

                // Lower arc
                translate(left = lowerArcX, top = 0f) {
                    drawPath(
                        path = lowerArcPath,
                        color = Color(0xFF8DE4B8),
                        alpha = lowerArcAlpha
                    )
                }

                // 3 bars
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(37f, 41f),
                    size = Size(4f, 14f),
                    cornerRadius = CornerRadius(1f, 1f),
                    alpha = barsAlpha
                )
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(45f, 37f),
                    size = Size(5f, 22f),
                    cornerRadius = CornerRadius(1f, 1f),
                    alpha = barsAlpha
                )
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(54f, 41f),
                    size = Size(4f, 14f),
                    cornerRadius = CornerRadius(1f, 1f),
                    alpha = barsAlpha
                )
            }
        }
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
            animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
        )
    }
    val t = animatable.value

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
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "RETAIL INTELLIGENCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.5.sp,
                        fontSize = 10.sp
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
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    modifier = Modifier.reveal(t, 0.31f, 0.51f, 10f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Smarter Stock. Better Business.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.reveal(t, 0.40f, 0.59f, 7f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Your everyday inventory, powered by AI. Built for shops that keep India moving.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                val primaryColor = MaterialTheme.colorScheme.primary
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                        .reveal(t, 0.55f, 0.725f, 5f),
                    color = MaterialTheme.colorScheme.primaryContainer,
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
                                color = primaryColor,
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
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onGetStartedClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .reveal(t, 0.59f, 0.775f, 6f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Get Started",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Designed for supermarkets & neighbourhood stores",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))
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
