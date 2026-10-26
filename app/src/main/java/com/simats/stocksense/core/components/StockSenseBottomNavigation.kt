package com.simats.stocksense.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StockSenseBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val containerColor = MaterialTheme.colorScheme.primaryContainer
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 10.dp,
        tonalElevation = 4.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(vertical = 6.dp, horizontal = 4.dp)
        ) {
            val tabCount = 4
            val tabWidth = maxWidth / tabCount

            // Animated Sliding Pill Background
            val animatedPillOffset by animateDpAsState(
                targetValue = tabWidth * selectedTab,
                animationSpec = spring(
                    dampingRatio = 0.72f,
                    stiffness = Spring.StiffnessLow
                ),
                label = "pillOffset"
            )

            // Background indicator pill sliding under selected tab
            Box(
                modifier = Modifier
                    .offset(x = animatedPillOffset)
                    .width(tabWidth)
                    .height(56.dp)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(containerColor.copy(alpha = 0.85f))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabs = listOf("Home", "Inventory", "Insights", "Profile")

                tabs.forEachIndexed { index, label ->
                    val isSelected = selectedTab == index

                    // Icon and label animations
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.14f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = 0.55f,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "scale"
                    )

                    val translationY by animateFloatAsState(
                        targetValue = if (isSelected) (-2f) else 0f,
                        animationSpec = spring(
                            dampingRatio = 0.7f,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "translationY"
                    )

                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) primaryColor else inactiveColor,
                        animationSpec = tween(durationMillis = 250),
                        label = "iconColor"
                    )

                    val dotAlpha by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0f,
                        animationSpec = tween(durationMillis = 250),
                        label = "dotAlpha"
                    )

                    val dotScale by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0f,
                        animationSpec = spring(
                            dampingRatio = 0.6f,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "dotScale"
                    )

                    Box(
                        modifier = Modifier
                            .width(tabWidth)
                            .height(56.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onTabSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.translationY = translationY * density
                            }
                        ) {
                            // Animated Top Accent Dot
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .graphicsLayer {
                                        scaleX = dotScale
                                        scaleY = dotScale
                                        alpha = dotAlpha
                                    }
                                    .clip(CircleShape)
                                    .background(primaryColor)
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            // Icon Canvas
                            Canvas(modifier = Modifier.size(20.dp)) {
                                when (index) {
                                    0 -> { // Home Icon
                                        val path = Path().apply {
                                            moveTo(size.width * 0.5f, 0f)
                                            lineTo(size.width, size.height * 0.45f)
                                            lineTo(size.width * 0.85f, size.height * 0.45f)
                                            lineTo(size.width * 0.85f, size.height)
                                            lineTo(size.width * 0.15f, size.height)
                                            lineTo(size.width * 0.15f, size.height * 0.45f)
                                            lineTo(0f, size.height * 0.45f)
                                            close()
                                        }
                                        if (isSelected) {
                                            drawPath(
                                                path = path,
                                                color = iconColor.copy(alpha = 0.25f),
                                                style = Fill
                                            )
                                        }
                                        drawPath(
                                            path = path,
                                            color = iconColor,
                                            style = Stroke(
                                                width = if (isSelected) 2.2.dp.toPx() else 1.8.dp.toPx(),
                                                cap = StrokeCap.Round,
                                                join = StrokeJoin.Round
                                            )
                                        )
                                    }
                                    1 -> { // Inventory Boxes Icon
                                        val strokeWidth = if (isSelected) 2.2.dp.toPx() else 1.8.dp.toPx()
                                        val boxes = listOf(
                                            Offset(0f, 0f),
                                            Offset(size.width * 0.56f, 0f),
                                            Offset(0f, size.height * 0.56f),
                                            Offset(size.width * 0.56f, size.height * 0.56f)
                                        )
                                        val boxSize = Size(size.width * 0.44f, size.height * 0.44f)

                                        boxes.forEachIndexed { i, offset ->
                                            if (isSelected && i == 0) {
                                                drawRect(
                                                    color = iconColor,
                                                    topLeft = offset,
                                                    size = boxSize
                                                )
                                            } else {
                                                drawRect(
                                                    color = iconColor,
                                                    topLeft = offset,
                                                    size = boxSize,
                                                    style = Stroke(width = strokeWidth)
                                                )
                                            }
                                        }
                                    }
                                    2 -> { // Insights Sparkle Icon
                                        val cx = size.width / 2f
                                        val cy = size.height / 2f
                                        val path = Path().apply {
                                            moveTo(cx, 0f)
                                            quadraticTo(cx, cy, size.width, cy)
                                            quadraticTo(cx, cy, cx, size.height)
                                            quadraticTo(cx, cy, 0f, cy)
                                            quadraticTo(cx, cy, cx, 0f)
                                        }
                                        if (isSelected) {
                                            drawPath(
                                                path = path,
                                                color = iconColor.copy(alpha = 0.3f),
                                                style = Fill
                                            )
                                        }
                                        drawPath(
                                            path = path,
                                            color = iconColor,
                                            style = Stroke(
                                                width = if (isSelected) 2.2.dp.toPx() else 1.8.dp.toPx(),
                                                cap = StrokeCap.Round,
                                                join = StrokeJoin.Round
                                            )
                                        )
                                    }
                                    3 -> { // Profile Icon
                                        val strokeWidth = if (isSelected) 2.2.dp.toPx() else 1.8.dp.toPx()
                                        if (isSelected) {
                                            drawCircle(
                                                color = iconColor.copy(alpha = 0.3f),
                                                radius = size.width * 0.25f,
                                                center = Offset(size.width * 0.5f, size.height * 0.3f)
                                            )
                                        }
                                        drawCircle(
                                            color = iconColor,
                                            radius = size.width * 0.25f,
                                            center = Offset(size.width * 0.5f, size.height * 0.3f),
                                            style = Stroke(width = strokeWidth)
                                        )
                                        val bodyPath = Path().apply {
                                            moveTo(0f, size.height)
                                            quadraticTo(size.width * 0.5f, size.height * 0.55f, size.width, size.height)
                                        }
                                        drawPath(
                                            path = bodyPath,
                                            color = iconColor,
                                            style = Stroke(
                                                width = strokeWidth,
                                                cap = StrokeCap.Round
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                color = iconColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
